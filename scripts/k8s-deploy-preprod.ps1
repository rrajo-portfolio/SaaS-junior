param(
  [string]$ClusterName = "fiscal-saas-preprod",
  [string]$Namespace = "fiscal-saas-preprod"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$envPath = Join-Path $repoRoot ".env"

function Invoke-Checked {
  param(
    [string]$File,
    [string[]]$Arguments
  )

  & $File @Arguments
  if ($LASTEXITCODE -ne 0) {
    throw "$File $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
  }
}

function Get-KindPath {
  $profileKind = Join-Path $env:USERPROFILE "bin\kind.exe"
  if (Test-Path $profileKind) {
    return $profileKind
  }

  $command = Get-Command kind -ErrorAction SilentlyContinue
  if ($command) {
    return $command.Source
  }

  throw "kind is not installed. Install kind before deploying to Kubernetes."
}

function Read-DotEnv {
  param([string]$Path)

  if (-not (Test-Path $Path)) {
    throw "Local .env file is required but was not found."
  }

  $values = @{}
  foreach ($line in Get-Content $Path) {
    $trimmed = $line.Trim()
    if ($trimmed.Length -eq 0 -or $trimmed.StartsWith("#")) {
      continue
    }

    $separator = $line.IndexOf("=")
    if ($separator -lt 1) {
      continue
    }

    $key = $line.Substring(0, $separator).Trim()
    $value = $line.Substring($separator + 1).Trim()
    if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
      $value = $value.Substring(1, $value.Length - 2)
    }

    $values[$key] = $value
  }

  return $values
}

$kind = Get-KindPath
$envValues = Read-DotEnv $envPath
$required = @("MYSQL_DATABASE", "MYSQL_USER", "MYSQL_PASSWORD", "MYSQL_ROOT_PASSWORD")
foreach ($key in $required) {
  if (-not $envValues.ContainsKey($key) -or [string]::IsNullOrWhiteSpace($envValues[$key])) {
    throw "Required local environment variable is missing: $key"
  }
}

if (-not $envValues.ContainsKey("SPRING_DATASOURCE_USERNAME") -or [string]::IsNullOrWhiteSpace($envValues["SPRING_DATASOURCE_USERNAME"])) {
  $envValues["SPRING_DATASOURCE_USERNAME"] = $envValues["MYSQL_USER"]
}

if (-not $envValues.ContainsKey("SPRING_DATASOURCE_PASSWORD") -or [string]::IsNullOrWhiteSpace($envValues["SPRING_DATASOURCE_PASSWORD"])) {
  $envValues["SPRING_DATASOURCE_PASSWORD"] = $envValues["MYSQL_PASSWORD"]
}

Invoke-Checked docker @("build", "-f", (Join-Path $repoRoot "infra\docker\backend.Dockerfile"), "-t", "fiscal-saas-backend:preprod", $repoRoot)
Invoke-Checked docker @("build", "-f", (Join-Path $repoRoot "infra\docker\frontend.Dockerfile"), "-t", "fiscal-saas-frontend:preprod", "--build-arg", "VITE_API_BASE_URL=/api", $repoRoot)
Invoke-Checked docker @("build", "-f", (Join-Path $repoRoot "infra\docker\nginx.Dockerfile"), "-t", "fiscal-saas-nginx:preprod", $repoRoot)
Invoke-Checked docker @("build", "-f", (Join-Path $repoRoot "infra\docker\mysql.Dockerfile"), "-t", "fiscal-saas-mysql:preprod", $repoRoot)

foreach ($image in @("fiscal-saas-backend:preprod", "fiscal-saas-frontend:preprod", "fiscal-saas-nginx:preprod", "fiscal-saas-mysql:preprod")) {
  Invoke-Checked $kind @("load", "docker-image", $image, "--name", $ClusterName)
}

Invoke-Checked kubectl @("config", "use-context", "kind-$ClusterName")
Invoke-Checked kubectl @("apply", "-f", (Join-Path $repoRoot "infra\k8s\preprod\namespace.yml"))

$secretArgs = @(
  "create", "secret", "generic", "fiscal-saas-runtime",
  "-n", $Namespace,
  "--dry-run=client",
  "-o", "yaml"
)

foreach ($key in @("MYSQL_DATABASE", "MYSQL_USER", "MYSQL_PASSWORD", "MYSQL_ROOT_PASSWORD", "SPRING_DATASOURCE_USERNAME", "SPRING_DATASOURCE_PASSWORD")) {
  $secretArgs += "--from-literal=$key=$($envValues[$key])"
}

$secretYaml = & kubectl @secretArgs
if ($LASTEXITCODE -ne 0) {
  throw "Unable to render Kubernetes runtime Secret."
}

$secretYaml | kubectl apply -f -
if ($LASTEXITCODE -ne 0) {
  throw "Unable to apply Kubernetes runtime Secret."
}

Invoke-Checked kubectl @("apply", "-k", (Join-Path $repoRoot "infra\k8s\preprod"))
Invoke-Checked kubectl @("-n", $Namespace, "rollout", "status", "statefulset/mysql", "--timeout=240s")
Invoke-Checked kubectl @("-n", $Namespace, "rollout", "restart", "deployment/backend")
Invoke-Checked kubectl @("-n", $Namespace, "rollout", "restart", "deployment/frontend")
Invoke-Checked kubectl @("-n", $Namespace, "rollout", "restart", "deployment/nginx")
Invoke-Checked kubectl @("-n", $Namespace, "rollout", "status", "deployment/backend", "--timeout=240s")
Invoke-Checked kubectl @("-n", $Namespace, "rollout", "status", "deployment/frontend", "--timeout=180s")
Invoke-Checked kubectl @("-n", $Namespace, "rollout", "status", "deployment/nginx", "--timeout=180s")

& (Join-Path $PSScriptRoot "k8s-smoke-preprod.ps1")
if ($LASTEXITCODE -ne 0) {
  throw "Kubernetes smoke checks failed."
}
