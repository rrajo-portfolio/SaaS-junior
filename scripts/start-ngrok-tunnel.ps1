param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$Domain = $env:NGROK_DOMAIN,
  [string]$Authtoken = $env:NGROK_AUTHTOKEN,
  [string]$BasicAuthUser = $env:PUBLIC_DEMO_BASIC_AUTH_USER,
  [string]$BasicAuthPassword = $env:PUBLIC_DEMO_BASIC_AUTH_PASSWORD
)

$ErrorActionPreference = "Stop"

$ngrok = Get-Command ngrok -ErrorAction SilentlyContinue
if (-not $ngrok) {
  Write-Output "BLOCKED_BY_LOCAL_ENVIRONMENT ngrok_not_installed"
  exit 1
}

if ([string]::IsNullOrWhiteSpace($Authtoken)) {
  Write-Output "BLOCKED_BY_NGROK_AUTH missing=NGROK_AUTHTOKEN"
  Write-Output "Create or open your ngrok account, copy the authtoken, then set it only in your local shell."
  exit 1
}

$arguments = @("http", $BaseUrl, "--authtoken", $Authtoken, "--log=stdout")

if (-not [string]::IsNullOrWhiteSpace($Domain)) {
  $arguments += "--url=$Domain"
}

if (-not [string]::IsNullOrWhiteSpace($BasicAuthUser) -and -not [string]::IsNullOrWhiteSpace($BasicAuthPassword)) {
  $arguments += "--basic-auth=$BasicAuthUser`:$BasicAuthPassword"
}

Write-Output "STARTING_NGROK_TUNNEL base_url=$BaseUrl domain=$Domain"
Write-Output "Keep this process open while the reviewer uses the public URL."
& $ngrok.Source @arguments
