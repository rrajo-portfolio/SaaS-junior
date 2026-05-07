param(
  [string]$BaseUrl = $env:PREPROD_PUBLIC_ORIGIN,
  [switch]$SkipPlaywright,
  [string]$BasicAuthUser = $env:PUBLIC_DEMO_BASIC_AUTH_USER,
  [string]$BasicAuthPassword = $env:PUBLIC_DEMO_BASIC_AUTH_PASSWORD
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
  $BaseUrl = "http://127.0.0.1:8080"
}

$BaseUrl = $BaseUrl.TrimEnd("/")
$repoRoot = Split-Path -Parent $PSScriptRoot
$headers = @{
  "ngrok-skip-browser-warning" = "true"
}

if (-not [string]::IsNullOrWhiteSpace($BasicAuthUser) -and -not [string]::IsNullOrWhiteSpace($BasicAuthPassword)) {
  $bytes = [System.Text.Encoding]::UTF8.GetBytes("${BasicAuthUser}:${BasicAuthPassword}")
  $headers.Authorization = "Basic $([Convert]::ToBase64String($bytes))"
}

function Test-HttpEndpoint {
  param(
    [string]$Name,
    [string]$Url
  )

  try {
    $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -Headers $headers -TimeoutSec 20
    if ($response.StatusCode -lt 200 -or $response.StatusCode -ge 300) {
      Write-Output "FIX_REQUIRED $Name status=$($response.StatusCode)"
      exit 1
    }
    Write-Output "$Name=$($response.StatusCode)"
  }
  catch {
    Write-Output "BLOCKED_BY_LOCAL_ENVIRONMENT $Name unreachable"
    exit 1
  }
}

Test-HttpEndpoint -Name "proxy_health" -Url "$BaseUrl/healthz"
Test-HttpEndpoint -Name "api_health" -Url "$BaseUrl/api/health"

if (-not $SkipPlaywright) {
  Push-Location (Join-Path $repoRoot "frontend")
  try {
    $env:PLAYWRIGHT_BASE_URL = $BaseUrl
    npm run e2e:preprod
    if ($LASTEXITCODE -ne 0) {
      Write-Output "FIX_REQUIRED playwright_preprod_failed"
      exit 1
    }
  }
  finally {
    Pop-Location
  }
}

Write-Output "READY_FOR_PREPROD_SMOKE base_url=$BaseUrl"
