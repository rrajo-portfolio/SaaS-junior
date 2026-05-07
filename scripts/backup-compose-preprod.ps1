param(
  [string]$ComposeFile = "infra/docker/docker-compose.preprod.yml",
  [string]$ProjectName = "fiscal-saas-preprod",
  [string]$OutputDirectory = "backups/preprod"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$composePath = Join-Path $repoRoot $ComposeFile
$outputRoot = Join-Path $repoRoot $OutputDirectory
New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$mysqlPath = Join-Path $outputRoot "mysql-compose-$timestamp.sql"
$artifactFile = "artifacts-compose-$timestamp.tgz"
$artifactPath = Join-Path $outputRoot $artifactFile
$documentVolume = "$ProjectName`_document-storage-preprod"

docker compose -f $composePath -p $ProjectName ps --status running | Out-Null
if ($LASTEXITCODE -ne 0) {
  Write-Output "BLOCKED_BY_LOCAL_ENVIRONMENT docker_compose_preprod_not_running"
  exit 1
}

docker compose -f $composePath -p $ProjectName exec -T mysql sh -c 'mysqldump --no-tablespaces -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' > $mysqlPath
if ($LASTEXITCODE -ne 0) {
  Remove-Item -LiteralPath $mysqlPath -ErrorAction SilentlyContinue
  Write-Output "FIX_REQUIRED mysql_backup_failed"
  exit 1
}

docker run --rm -v "${documentVolume}:/data:ro" -v "${outputRoot}:/backup" alpine:3.20 sh -c "tar -czf /backup/$artifactFile -C /data ."
if ($LASTEXITCODE -ne 0) {
  Remove-Item -LiteralPath $artifactPath -ErrorAction SilentlyContinue
  Write-Output "FIX_REQUIRED artifact_backup_failed"
  exit 1
}

Write-Output "PREPROD_BACKUP_READY mysql=$mysqlPath artifacts=$artifactPath"
