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
if (-not (docker inspect $VaultContainer 2>$null)) { throw "El contenedor '$VaultContainer' no existe. Levanta Vault con Docker Compose antes de ejecutar este script." }

function Get-StatusJson {
    $status = & docker exec $VaultContainer vault status -format=json 2>$null
    if ($LASTEXITCODE -notin @(0,1,2)) { return $null }
    return ($status | ConvertFrom-Json)
}

Write-Host "== FactuCore - Bootstrap de Vault =="
$statusJson=$null
for($i=0;$i-lt 30;$i++){ $statusJson=Get-StatusJson; if($null -ne $statusJson){break}; Start-Sleep 2 }
if($null -eq $statusJson){throw "No se pudo consultar el estado de Vault."}

if(-not $statusJson.initialized){
    Write-Host "Inicializando Vault por primera vez..."
    $init=& docker exec $VaultContainer vault operator init -format=json
    if($LASTEXITCODE -ne 0){throw "No se pudo inicializar Vault."}
    [IO.File]::WriteAllText($InitFile,($init -join [Environment]::NewLine)+[Environment]::NewLine,[Text.UTF8Encoding]::new($false))
    $statusJson=Get-StatusJson
}
if($statusJson.sealed){
    if(-not(Test-Path $InitFile)){throw "Vault esta sellado y no existe $InitFile."}
    $initJson=Get-Content -Raw $InitFile|ConvertFrom-Json
    $threshold = [int]$statusJson.t
    if ($threshold -le 0 -and $initJson.PSObject.Properties.Name -contains "unseal_threshold") {
        $threshold = [int]$initJson.unseal_threshold
    }
    if ($threshold -le 0) { throw "No se pudo determinar el umbral de unseal desde el estado de Vault ni desde $InitFile." }
    foreach($key in $initJson.unseal_keys_b64|Select-Object -First $threshold){
        & docker exec $VaultContainer vault operator unseal $key *> $null
        if($LASTEXITCODE -ne 0){throw "No se pudo ejecutar el unseal."}
    }
    $statusJson=Get-StatusJson
    if($statusJson.sealed){throw "Vault continua sellado."}
}

$rootToken=[Environment]::GetEnvironmentVariable("FACTUCORE_VAULT_ROOT_TOKEN")
if(-not $rootToken -and(Test-Path $InitFile)){$rootToken=(Get-Content -Raw $InitFile|ConvertFrom-Json).root_token}
if(-not $rootToken){throw "No se encontro el root token."}
function Secret([string]$n,[string]$p){$v=[Environment]::GetEnvironmentVariable($n);if($v){return $v};$s=Read-Host $p -AsSecureString;$ptr=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($s);try{return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)}finally{[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)}}
$pg=Secret "FACTUCORE_POSTGRES_PASSWORD" "Contraseña de PostgreSQL"
$kc=Secret "FACTUCORE_KEYCLOAK_ADMIN_PASSWORD" "Contraseña del administrador de Keycloak"

$mounts=(& docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault secrets list -format=json|Out-String)|ConvertFrom-Json
if(-not($mounts.PSObject.Properties.Name -contains "secret/")){& docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault secrets enable -path=secret kv-v2;if($LASTEXITCODE-ne 0){throw "No se pudo habilitar KV v2."}}
& docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault kv put secret/factucore/postgresql password=$pg
& docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault kv put secret/factucore/keycloak password=$kc
Get-Content -Raw $PolicyPath|docker exec -i -e VAULT_TOKEN=$rootToken $VaultContainer vault policy write factucore-agent -
$agent=if(Test-Path $AgentTokenFile){(Get-Content -Raw $AgentTokenFile).Trim()}
if(-not $agent -or (& docker exec -e VAULT_TOKEN=$agent $VaultContainer vault token lookup *> $null;$LASTEXITCODE-ne 0)){
  $agent=((& docker exec -e VAULT_TOKEN=$rootToken $VaultContainer vault token create -policy=factucore-agent -orphan -format=json|Out-String)|ConvertFrom-Json).auth.client_token
  [IO.File]::WriteAllText($AgentTokenFile,$agent+[Environment]::NewLine,[Text.UTF8Encoding]::new($false))
}
& docker exec -e VAULT_TOKEN=$agent $VaultContainer vault kv get secret/factucore/postgresql *> $null
& docker exec -e VAULT_TOKEN=$agent $VaultContainer vault kv get secret/factucore/keycloak *> $null
Write-Host "Bootstrap de Vault completado correctamente. El script no levanta ni detiene contenedores."
