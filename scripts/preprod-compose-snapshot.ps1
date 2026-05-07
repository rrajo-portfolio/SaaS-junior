param(
  [string]$ProjectName = "fiscal-saas-preprod",
  [string]$RollbackRef = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($RollbackRef)) {
  $RollbackRef = Get-Date -Format "yyyyMMdd-HHmmss"
}

$services = @("backend", "frontend", "nginx")
foreach ($service in $services) {
  $sourceImage = "$ProjectName-$service"
  $targetImage = "fiscal-saas/$service`:$RollbackRef"
  docker image inspect $sourceImage | Out-Null
  if ($LASTEXITCODE -ne 0) {
    Write-Output "BLOCKED_BY_LOCAL_ENVIRONMENT image_missing=$sourceImage"
    exit 1
  }
  docker tag $sourceImage $targetImage
  if ($LASTEXITCODE -ne 0) {
    Write-Output "FIX_REQUIRED image_snapshot_failed=$sourceImage"
    exit 1
  }
}

Write-Output "PREPROD_IMAGE_SNAPSHOT_READY rollback_ref=$RollbackRef"
