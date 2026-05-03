$ErrorActionPreference = "Stop"

$composeFile = "infra/keycloak/docker-compose.keycloak.yml"
$discoveryUrl = "http://127.0.0.1:18081/realms/fiscal-saas/.well-known/openid-configuration"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Output "BLOCKED_BY_LOCAL_ENVIRONMENT docker_not_installed"
    exit 1
}

$dockerInfo = docker info 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Output "BLOCKED_BY_LOCAL_ENVIRONMENT docker_daemon_unavailable"
    exit 1
}

docker compose -f $composeFile up -d

$ready = $false
for ($i = 0; $i -lt 36; $i++) {
    Start-Sleep -Seconds 5
    try {
        $response = Invoke-WebRequest -Uri $discoveryUrl -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200 -and $response.Content -match "issuer") {
            $ready = $true
            break
        }
    }
    catch {
        # Keep polling until Keycloak imports the realm and opens HTTP.
    }
}

if (-not $ready) {
    Write-Output "BLOCKED_BY_LOCAL_ENVIRONMENT keycloak_not_ready"
    docker compose -f $composeFile ps
    exit 1
}

Write-Output "keycloak_ready url=http://127.0.0.1:18081 admin=http://127.0.0.1:18081/admin"
