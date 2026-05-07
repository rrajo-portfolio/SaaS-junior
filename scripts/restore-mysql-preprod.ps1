param(
  [string]$Namespace = "fiscal-saas-preprod",
  [Parameter(Mandatory = $true)]
  [string]$InputPath,
  [switch]$ConfirmRestore
)

$ErrorActionPreference = "Stop"

if (-not $ConfirmRestore) {
  Write-Output "BLOCKED_BY_OPERATOR_CONFIRMATION restore_requires_ConfirmRestore"
  exit 1
}

$resolvedInput = Resolve-Path -LiteralPath $InputPath
kubectl -n $Namespace get pod mysql-0 | Out-Null
if ($LASTEXITCODE -ne 0) {
  Write-Output "BLOCKED_BY_LOCAL_ENVIRONMENT mysql_pod_not_found"
  exit 1
}

Get-Content -LiteralPath $resolvedInput -Raw | kubectl -n $Namespace exec -i mysql-0 -- sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"'
if ($LASTEXITCODE -ne 0) {
  Write-Output "FIX_REQUIRED mysql_restore_failed"
  exit 1
}

Write-Output "PREPROD_MYSQL_RESTORE_READY input=$resolvedInput"
