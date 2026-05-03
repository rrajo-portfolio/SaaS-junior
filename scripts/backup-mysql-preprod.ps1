param(
  [string]$Namespace = "fiscal-saas-preprod",
  [string]$OutputDirectory = "backups/preprod"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$outputRoot = Join-Path $repoRoot $OutputDirectory
New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outputPath = Join-Path $outputRoot "mysql-$timestamp.sql"

kubectl -n $Namespace exec mysql-0 -- sh -c 'mysqldump --no-tablespaces -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' > $outputPath
if ($LASTEXITCODE -ne 0) {
  Remove-Item -LiteralPath $outputPath -ErrorAction SilentlyContinue
  throw "MySQL backup failed."
}

Write-Output "backup_created=$outputPath"
