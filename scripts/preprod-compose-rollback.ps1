param(
  [string]$ComposeFile = "infra/docker/docker-compose.preprod.yml",
  [string]$ProjectName = "fiscal-saas-preprod",
  [Parameter(Mandatory = $true)]
  [string]$RollbackRef,
  [string]$SmokeBaseUrl = "http://127.0.0.1:8080",
  [switch]$ConfirmRollback
)

$ErrorActionPreference = "Stop"

if (-not $ConfirmRollback) {
  Write-Output "BLOCKED_BY_OPERATOR_CONFIRMATION rollback_requires_ConfirmRollback"
  exit 1
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$composePath = Join-Path $repoRoot $ComposeFile
$services = @("backend", "frontend", "nginx")

foreach ($service in $services) {
  $snapshotImage = "fiscal-saas/$service`:$RollbackRef"
  $composeImage = "$ProjectName-$service"
  docker image inspect $snapshotImage | Out-Null
  if ($LASTEXITCODE -ne 0) {
    Write-Output "BLOCKED_BY_LOCAL_ENVIRONMENT rollback_image_missing=$snapshotImage"
    exit 1
  }
  docker tag $snapshotImage $composeImage
  if ($LASTEXITCODE -ne 0) {
    Write-Output "FIX_REQUIRED rollback_tag_failed=$snapshotImage"
    exit 1
  }
}

docker compose -f $composePath -p $ProjectName up -d --no-build backend frontend nginx
if ($LASTEXITCODE -ne 0) {
  Write-Output "FIX_REQUIRED docker_compose_rollback_failed"
  exit 1
}

& (Join-Path $repoRoot "scripts/preprod-smoke.ps1") -BaseUrl $SmokeBaseUrl -SkipPlaywright
if ($LASTEXITCODE -ne 0) {
  exit $LASTEXITCODE
}

Write-Output "PREPROD_ROLLBACK_READY rollback_ref=$RollbackRef"
