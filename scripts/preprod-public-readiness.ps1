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
    "PREPROD_ROLLBACK_REF"
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
    }
    catch {
        Write-Output "BLOCKED_BY_EXTERNAL_INFRASTRUCTURE public_health_unreachable"
        exit 1
    }
}

Write-Output "READY_FOR_PUBLIC_PREPROD origin=$env:PREPROD_PUBLIC_ORIGIN tls=$env:PREPROD_TLS_MODE auth=$env:PREPROD_AUTH_MODE"
