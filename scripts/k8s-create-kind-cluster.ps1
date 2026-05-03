param(
  [string]$ClusterName = "fiscal-saas-preprod"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$kindConfig = Join-Path $repoRoot "infra\k8s\kind\cluster.yml"

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

  throw "kind is not installed. Install kind before creating the Kubernetes cluster."
}

$kind = Get-KindPath
Invoke-Checked docker @("info", "--format", "{{.ServerVersion}}")
Invoke-Checked $kind @("version")

$clusters = & $kind get clusters
if ($LASTEXITCODE -ne 0) {
  throw "Unable to list kind clusters."
}

if ($clusters -notcontains $ClusterName) {
  Invoke-Checked $kind @("create", "cluster", "--name", $ClusterName, "--config", $kindConfig, "--wait", "180s")
}

Invoke-Checked kubectl @("config", "use-context", "kind-$ClusterName")
Invoke-Checked kubectl @("get", "nodes")
Invoke-Checked kubectl @("apply", "-f", (Join-Path $repoRoot "infra\k8s\preprod\namespace.yml"))
