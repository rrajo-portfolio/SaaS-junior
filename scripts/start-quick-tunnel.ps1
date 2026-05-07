param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$ToolDirectory = "$env:LOCALAPPDATA\FiscalSaasTools"
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($ToolDirectory)) {
  Write-Output "BLOCKED_BY_LOCAL_ENVIRONMENT local_appdata_missing"
  exit 1
}

New-Item -ItemType Directory -Force -Path $ToolDirectory | Out-Null
$cloudflared = Get-Command cloudflared -ErrorAction SilentlyContinue
$cloudflaredPath = if ($cloudflared) { $cloudflared.Source } else { Join-Path $ToolDirectory "cloudflared.exe" }

if (-not (Test-Path -LiteralPath $cloudflaredPath)) {
  $downloadUrl = "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe"
  Invoke-WebRequest -UseBasicParsing -Uri $downloadUrl -OutFile $cloudflaredPath
}

Write-Output "STARTING_QUICK_TUNNEL base_url=$BaseUrl"
Write-Output "Keep this process open. The public URL appears as https://*.trycloudflare.com."
& $cloudflaredPath tunnel --url $BaseUrl
