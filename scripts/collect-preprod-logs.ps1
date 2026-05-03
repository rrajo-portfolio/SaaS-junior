param(
  [string]$Namespace = "fiscal-saas-preprod",
  [string]$OutputDirectory = "logs/preprod"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$outputRoot = Join-Path $repoRoot $OutputDirectory
New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$pods = kubectl -n $Namespace get pods -o name
if ($LASTEXITCODE -ne 0) {
  throw "Unable to list preproduction pods."
}

foreach ($pod in $pods) {
  if ([string]::IsNullOrWhiteSpace($pod)) {
    continue
  }
  $safePod = $pod.Trim() -replace '^pod/', ''
  kubectl -n $Namespace logs $safePod --all-containers --tail=500 > (Join-Path $outputRoot "$timestamp-$safePod.log")
}

Write-Output "logs_directory=$outputRoot"
