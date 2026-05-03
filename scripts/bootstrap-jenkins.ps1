param(
  [string]$ComposeFile = "infra/jenkins/docker-compose.jenkins.yml",
  [string]$Url = "http://127.0.0.1:8085/login"
)

$ErrorActionPreference = "Stop"

if (-not $env:JENKINS_ADMIN_ID) {
  throw "Set JENKINS_ADMIN_ID in the local shell or a non-versioned .env file."
}

if (-not $env:JENKINS_ADMIN_PASSWORD) {
  throw "Set JENKINS_ADMIN_PASSWORD in the local shell or a non-versioned .env file."
}

docker compose -f $ComposeFile config --quiet
docker compose -f $ComposeFile up --build -d

$lastError = $null
foreach ($attempt in 1..40) {
  try {
    $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
      "jenkins_ready url=$Url"
      exit 0
    }
    $lastError = "$Url returned HTTP $($response.StatusCode)"
  } catch {
    $lastError = $_.Exception.Message
  }
  Start-Sleep -Seconds 5
}

throw "Jenkins did not become ready. Last error: $lastError"
