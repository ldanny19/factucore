$ErrorActionPreference = "Stop"

$VaultContainer = if ($env:FACTUCORE_VAULT_CONTAINER) { $env:FACTUCORE_VAULT_CONTAINER } else { "factucore-vault" }
$ScriptDir = $PSScriptRoot
$ComposeDir = (Resolve-Path (Join-Path $ScriptDir "..\..")).Path
$SecretsDir = if ($env:FACTUCORE_HOME) { Join-Path $env:FACTUCORE_HOME "secrets" } else { Join-Path (Split-Path $ComposeDir -Parent) "secrets" }
$AgentTokenFile = Join-Path $SecretsDir "vault_agent_token.txt"

function Invoke-Vault {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    & docker exec $VaultContainer vault @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "El comando de Vault fallo: vault $($Arguments -join ' ')"
    }
}

function Get-RequiredSecret {
    param(
        [Parameter(Mandatory = $true)]
        [string]$EnvironmentName,
        [Parameter(Mandatory = $true)]
        [string]$Prompt
    )

    $value = [Environment]::GetEnvironmentVariable($EnvironmentName)
    if ($value) {
        return $value
    }

    $secure = Read-Host $Prompt -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    }
}

Write-Host "== FactuCore - Bootstrap de Vault =="

Write-Host "[1/7] Levantando Vault..."
& docker compose -f (Join-Path $ComposeDir "docker-compose.yml") up -d vault
if ($LASTEXITCODE -ne 0) { throw "No se pudo levantar el servicio Vault." }

Write-Host "[1/7] Esperando disponibilidad de Vault..."
$status = & docker exec $VaultContainer vault status -format=json 2>$null
if ($LASTEXITCODE -notin @(0, 2)) {
    throw "No se pudo consultar el estado de Vault. Verifica que el contenedor '$VaultContainer' este levantado."
}

$statusJson = $status | ConvertFrom-Json
if (-not $statusJson.initialized) {
    throw "Vault no esta inicializado. Ejecuta 'vault operator init' una sola vez y conserva las claves fuera del repositorio."
}
if ($statusJson.sealed) {
    $threshold = [int]$statusJson.t
    if ($threshold -lt 1) { $threshold = 1 }

    Write-Host "Vault esta sellado; iniciando unseal interactivo..."
    for ($i = 1; $i -le $threshold; $i++) {
        $secureKey = Read-Host "Unseal Key $i/$threshold" -AsSecureString
        $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureKey)
        try {
            $unsealKey = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
            & docker exec $VaultContainer vault operator unseal $unsealKey *> $null
            if ($LASTEXITCODE -ne 0) {
                throw "La clave de unseal $i no fue aceptada."
            }
        }
        finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
        }
    }

    $status = & docker exec $VaultContainer vault status -format=json 2>$null
    $statusJson = $status | ConvertFrom-Json
    if ($statusJson.sealed) {
        throw "Vault continua sellado. Verifica las claves de unseal."
    }
}

Write-Host "[2/7] Obteniendo credenciales externas..."
$rootToken = [Environment]::GetEnvironmentVariable("FACTUCORE_VAULT_ROOT_TOKEN")
if (-not $rootToken) {
    $rootToken = Read-Host "FACTUCORE_VAULT_ROOT_TOKEN"
}
if (-not $rootToken) {
    throw "Se requiere FACTUCORE_VAULT_ROOT_TOKEN."
}

$postgresPassword = Get-RequiredSecret "FACTUCORE_POSTGRES_PASSWORD" "Contraseña de PostgreSQL"
$keycloakPassword = Get-RequiredSecret "FACTUCORE_KEYCLOAK_ADMIN_PASSWORD" "Contraseña del administrador de Keycloak"

Write-Host "[3/7] Habilitando/verificando KV v2..."
$env:VAULT_TOKEN = $rootToken
$mounts = & docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault secrets list -format=json
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo consultar los engines de secretos."
}
$mountsJson = $mounts | ConvertFrom-Json
if (-not ($mountsJson.PSObject.Properties.Name -contains "secret/")) {
    Invoke-Vault @("secrets", "enable", "-path=secret", "kv-v2")
} else {
    $type = $mountsJson."secret/".type
    $version = $mountsJson."secret/".options.version
    if ($type -ne "kv" -or $version -ne "2") {
        throw "El mount 'secret/' ya existe pero no es KV v2."
    }
}

Write-Host "[4/7] Cargando secretos en Vault..."
& docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault kv put secret/factucore/postgresql password=$postgresPassword
if ($LASTEXITCODE -ne 0) { throw "No se pudo guardar el secreto de PostgreSQL." }

& docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault kv put secret/factucore/keycloak password=$keycloakPassword
if ($LASTEXITCODE -ne 0) { throw "No se pudo guardar el secreto de Keycloak." }

Write-Host "[5/7] Creando/verificando policy del Vault Agent..."
$policyPath = Join-Path $ScriptDir "..\policy\factucore-agent.hcl"
if (-not (Test-Path $policyPath)) {
    throw "No existe la policy: $policyPath"
}
Get-Content -Raw $policyPath | docker exec -i -e VAULT_TOKEN=$rootToken $VaultContainer vault policy write factucore-agent -
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo crear la policy factucore-agent."
}

Write-Host "[6/7] Creando token del Vault Agent..."
New-Item -ItemType Directory -Force -Path $SecretsDir | Out-Null

$agentToken = $null
if (Test-Path $AgentTokenFile) {
    $agentToken = (Get-Content -Raw $AgentTokenFile).Trim()
}

$tokenValid = $false
if ($agentToken) {
    & docker exec -e VAULT_TOKEN=$agentToken $VaultContainer vault token lookup *> $null
    $tokenValid = ($LASTEXITCODE -eq 0)
}

if (-not $tokenValid) {
    $tokenOutput = & docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault token create -policy=factucore-agent -orphan -format=json
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo crear el token del Vault Agent."
    }

    $tokenJson = $tokenOutput | ConvertFrom-Json
    $agentToken = $tokenJson.auth.client_token
    if (-not $agentToken) {
        throw "Vault no devolvio un client token."
    }

    [System.IO.File]::WriteAllText(
        $AgentTokenFile,
        $agentToken + [Environment]::NewLine,
        [System.Text.UTF8Encoding]::new($false)
    )

    Write-Host "Token del Vault Agent creado en: $AgentTokenFile"
} else {
    Write-Host "Token del Vault Agent existente y valido."
}

Write-Host "[7/7] Validando acceso del Vault Agent..."
& docker exec -e VAULT_TOKEN=$agentToken $VaultContainer vault kv get secret/factucore/postgresql *> $null
if ($LASTEXITCODE -ne 0) {
    throw "El token del Vault Agent no puede leer PostgreSQL."
}
& docker exec -e VAULT_TOKEN=$agentToken $VaultContainer vault kv get secret/factucore/keycloak *> $null
if ($LASTEXITCODE -ne 0) {
    throw "El token del Vault Agent no puede leer Keycloak."
}

Write-Host ""
Write-Host "Bootstrap de Vault completado correctamente."
Write-Host "Ahora puedes levantar el stack para que Vault Agent renderice los secretos."
