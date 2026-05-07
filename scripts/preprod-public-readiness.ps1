param(
    [string]$BaseUrl = $env:PREPROD_PUBLIC_ORIGIN,
    [switch]$SkipHttpChecks
)

$ErrorActionPreference = "Stop"

function Require-Env {
    param([string]$Name)
    $value = [Environment]::GetEnvironmentVariable($Name)
    if ([string]::IsNullOrWhiteSpace($value)) {
        return $false
    }
    return $true
}

$required = @(
    "PREPROD_PUBLIC_ORIGIN",
    "PREPROD_TLS_MODE",
    "PREPROD_AUTH_MODE",
    "PREPROD_BACKUP_MODE",
    "PREPROD_ROLLBACK_REF",
    "PREPROD_SECRET_SOURCE",
    "PREPROD_DEMO_DATA_MODE",
    "PREPROD_SMOKE_TEST_MODE"
)

$missing = @()
foreach ($name in $required) {
    if (-not (Require-Env $name)) {
        $missing += $name
    }
}

if ($missing.Count -gt 0) {
    Write-Output "BLOCKED_BY_EXTERNAL_INFRASTRUCTURE missing=$($missing -join ',')"
    exit 1
}

if ($env:PREPROD_AUTH_MODE -ne "oidc") {
    Write-Output "FIX_REQUIRED preprod_auth_mode_must_be_oidc"
    exit 1
}

if ($env:PREPROD_TLS_MODE -notin @("managed", "reverse-proxy", "ingress", "tunnel")) {
    Write-Output "FIX_REQUIRED unsupported_tls_mode=$env:PREPROD_TLS_MODE"
    exit 1
}

if ($env:PREPROD_BACKUP_MODE -notin @("compose-script", "kubernetes-script", "managed-volume")) {
    Write-Output "FIX_REQUIRED unsupported_backup_mode=$env:PREPROD_BACKUP_MODE"
    exit 1
}

if ($env:PREPROD_SECRET_SOURCE -notin @("platform-secrets", "jenkins-credentials", "kubernetes-secrets", "external-secret-manager")) {
    Write-Output "FIX_REQUIRED unsupported_secret_source=$env:PREPROD_SECRET_SOURCE"
    exit 1
}

if ($env:PREPROD_DEMO_DATA_MODE -ne "demo-only") {
    Write-Output "FIX_REQUIRED preprod_demo_data_mode_must_be_demo-only"
    exit 1
}

if ($env:PREPROD_SMOKE_TEST_MODE -notin @("playwright", "http-only-plus-manual")) {
    Write-Output "FIX_REQUIRED unsupported_smoke_test_mode=$env:PREPROD_SMOKE_TEST_MODE"
    exit 1
}

if (-not $env:PREPROD_PUBLIC_ORIGIN.StartsWith("https://")) {
    Write-Output "FIX_REQUIRED preprod_public_origin_must_be_https"
    exit 1
}

if ($env:PREPROD_PUBLIC_ORIGIN -match "localhost|127\.0\.0\.1|0\.0\.0\.0") {
    Write-Output "FIX_REQUIRED preprod_public_origin_must_not_be_localhost"
    exit 1
}

if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
    Write-Output "BLOCKED_BY_EXTERNAL_INFRASTRUCTURE base_url_missing"
    exit 1
}

if (-not $SkipHttpChecks) {
    try {
        $health = Invoke-WebRequest -UseBasicParsing -Uri "$($BaseUrl.TrimEnd('/'))/healthz" -TimeoutSec 15
        if ($health.StatusCode -ne 200) {
            Write-Output "FIX_REQUIRED healthz_status=$($health.StatusCode)"
            exit 1
        }
        $apiHealth = Invoke-WebRequest -UseBasicParsing -Uri "$($BaseUrl.TrimEnd('/'))/api/health" -TimeoutSec 15
        if ($apiHealth.StatusCode -ne 200) {
            Write-Output "FIX_REQUIRED api_health_status=$($apiHealth.StatusCode)"
            exit 1
        }
    }
    catch {
        Write-Output "BLOCKED_BY_EXTERNAL_INFRASTRUCTURE public_health_unreachable"
        exit 1
    }
}

Write-Output "READY_FOR_PUBLIC_PREPROD origin=$env:PREPROD_PUBLIC_ORIGIN tls=$env:PREPROD_TLS_MODE auth=$env:PREPROD_AUTH_MODE"
