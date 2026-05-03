$ErrorActionPreference = "Stop"

$required = @(
    "PREPROD_PUBLIC_HOST",
    "PREPROD_TLS_MODE",
    "PREPROD_AUTH_MODE"
)

$missing = @()
foreach ($name in $required) {
    if (-not [Environment]::GetEnvironmentVariable($name)) {
        $missing += $name
    }
}

if ($missing.Count -gt 0) {
    Write-Output "BLOCKED_BY_EXTERNAL_INFRASTRUCTURE missing=$($missing -join ',')"
    exit 1
}

if ([Environment]::GetEnvironmentVariable("PREPROD_AUTH_MODE") -ne "oidc") {
    Write-Output "FIX_REQUIRED preprod_auth_mode_must_be_oidc"
    exit 1
}

Write-Output "public_preprod_inputs_present host=$([Environment]::GetEnvironmentVariable("PREPROD_PUBLIC_HOST"))"
