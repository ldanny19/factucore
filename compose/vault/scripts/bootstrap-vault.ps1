$ErrorActionPreference = "Stop"

$VaultContainer = if ($env:FACTUCORE_VAULT_CONTAINER) { $env:FACTUCORE_VAULT_CONTAINER } else { "factucore-vault" }
$ScriptDir = $PSScriptRoot
$ComposeDir = (Resolve-Path (Join-Path $ScriptDir "..\..")).Path
$FactuCoreHome = if ($env:FACTUCORE_HOME) { $env:FACTUCORE_HOME } else { Split-Path $ComposeDir -Parent }
$SecretsDir = Join-Path $FactuCoreHome "secrets"
$InitFile = Join-Path $SecretsDir "vault-init.json"
$AgentTokenFile = Join-Path $SecretsDir "vault_agent_token.txt"
$PolicyPath = Join-Path $ScriptDir "..\policy\factucore-agent.hcl"

New-Item -ItemType Directory -Force -Path $SecretsDir | Out-Null

function Get-StatusJson {
    $status = & docker exec $VaultContainer vault status -format=json 2>$null
    if ($LASTEXITCODE -notin @(0, 1, 2)) { return $null }
    return ($status | ConvertFrom-Json)
}

Write-Host "== FactuCore - Bootstrap de Vault =="
Write-Host "[1/7] Levantando Vault..."
& docker compose -f (Join-Path $ComposeDir "docker-compose.yml") up -d vault
if ($LASTEXITCODE -ne 0) { throw "No se pudo levantar Vault." }

$statusJson = $null
for ($i = 0; $i -lt 30; $i++) {
    $statusJson = Get-StatusJson
    if ($null -ne $statusJson) { break }
    Start-Sleep -Seconds 2
}
if ($null -eq $statusJson) { throw "No se pudo consultar el estado de Vault." }

if (-not $statusJson.initialized) {
    Write-Host "[1/7] Inicializando Vault por primera vez..."
    $initOutput = & docker exec $VaultContainer vault operator init -format=json
    if ($LASTEXITCODE -ne 0) { throw "No se pudo inicializar Vault." }
    [System.IO.File]::WriteAllText($InitFile, ($initOutput -join [Environment]::NewLine) + [Environment]::NewLine, [System.Text.UTF8Encoding]::new($false))
    Write-Host "Inicializacion guardada en: $InitFile"
    $statusJson = Get-StatusJson
}

if ($statusJson.sealed) {
    if (-not (Test-Path $InitFile)) { throw "Vault esta sellado y no existe $InitFile." }
    $initJson = Get-Content -Raw $InitFile | ConvertFrom-Json
    $threshold = [int]$initJson.secret_threshold
    if ($threshold -lt 1) { throw "Umbral de unseal invalido." }
    Write-Host "Ejecutando unseal..."
    foreach ($key in $initJson.unseal_keys_b64 | Select-Object -First $threshold) {
        & docker exec $VaultContainer vault operator unseal $key *> $null
        if ($LASTEXITCODE -ne 0) { throw "No se pudo ejecutar el unseal." }
    }
    $statusJson = Get-StatusJson
    if ($statusJson.sealed) { throw "Vault continua sellado." }
}

Write-Host "[2/7] Obteniendo credenciales..."
$rootToken = [Environment]::GetEnvironmentVariable("FACTUCORE_VAULT_ROOT_TOKEN")
if (-not $rootToken -and (Test-Path $InitFile)) { $rootToken = (Get-Content -Raw $InitFile | ConvertFrom-Json).root_token }
if (-not $rootToken) { $rootToken = Read-Host "FACTUCORE_VAULT_ROOT_TOKEN" }
if (-not $rootToken) { throw "Se requiere el root token." }

function Get-SecretValue([string]$Name, [string]$Prompt) {
    $value = [Environment]::GetEnvironmentVariable($Name)
    if ($value) { return $value }
    $secure = Read-Host $Prompt -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}
$postgresPassword = Get-SecretValue "FACTUCORE_POSTGRES_PASSWORD" "Contraseña de PostgreSQL"
$keycloakPassword = Get-SecretValue "FACTUCORE_KEYCLOAK_ADMIN_PASSWORD" "Contraseña del administrador de Keycloak"

$env:VAULT_TOKEN = $rootToken
Write-Host "[3/7] Habilitando/verificando KV v2..."
$mounts = (& docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault secrets list -format=json | Out-String) | ConvertFrom-Json
if (-not ($mounts.PSObject.Properties.Name -contains "secret/")) {
    & docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault secrets enable -path=secret kv-v2
    if ($LASTEXITCODE -ne 0) { throw "No se pudo habilitar KV v2." }
} elseif ($mounts."secret/".type -ne "kv" -or $mounts."secret/".options.version -ne "2") {
    throw "El mount 'secret/' ya existe pero no es KV v2."
}

Write-Host "[4/7] Cargando secretos..."
& docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault kv put secret/factucore/postgresql password=$postgresPassword
if ($LASTEXITCODE -ne 0) { throw "No se pudo guardar PostgreSQL." }
& docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault kv put secret/factucore/keycloak password=$keycloakPassword
if ($LASTEXITCODE -ne 0) { throw "No se pudo guardar Keycloak." }

Write-Host "[5/7] Aplicando policy..."
if (-not (Test-Path $PolicyPath)) { throw "No existe la policy: $PolicyPath" }
Get-Content -Raw $PolicyPath | docker exec -i -e VAULT_TOKEN=$rootToken $VaultContainer vault policy write factucore-agent -
if ($LASTEXITCODE -ne 0) { throw "No se pudo aplicar la policy." }

Write-Host "[6/7] Creando/reutilizando token..."
$agentToken = if (Test-Path $AgentTokenFile) { (Get-Content -Raw $AgentTokenFile).Trim() } else { $null }
$valid = $false
if ($agentToken) { & docker exec -e VAULT_TOKEN=$agentToken $VaultContainer vault token lookup *> $null; $valid = ($LASTEXITCODE -eq 0) }
if (-not $valid) {
    $tokenJson = (& docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault token create -policy=factucore-agent -orphan -format=json | Out-String) | ConvertFrom-Json
    $agentToken = $tokenJson.auth.client_token
    if (-not $agentToken) { throw "Vault no devolvio un client token." }
    [System.IO.File]::WriteAllText($AgentTokenFile, $agentToken + [Environment]::NewLine, [System.Text.UTF8Encoding]::new($false))
}

Write-Host "[7/7] Validando acceso..."
& docker exec -e VAULT_TOKEN=$agentToken $VaultContainer vault kv get secret/factucore/postgresql *> $null
if ($LASTEXITCODE -ne 0) { throw "El token del Vault Agent no puede leer PostgreSQL." }
& docker exec -e VAULT_TOKEN=$agentToken $VaultContainer vault kv get secret/factucore/keycloak *> $null
if ($LASTEXITCODE -ne 0) { throw "El token del Vault Agent no puede leer Keycloak." }

Write-Host ""
Write-Host "Bootstrap de Vault completado correctamente."
Write-Host "Ahora puedes levantar el stack para que Vault Agent renderice los secretos."
