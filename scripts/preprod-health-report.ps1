param(
  [string]$BaseUrl = "http://127.0.0.1:18080",
  [string]$Namespace = "fiscal-saas-preprod"
)

$ErrorActionPreference = "Stop"

kubectl get nodes
kubectl -n $Namespace get pods
kubectl -n $Namespace get ingress,networkpolicy

$health = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/healthz" -TimeoutSec 15
$api = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/api/health" -TimeoutSec 15

Write-Output "proxy_health=$($health.StatusCode)"
Write-Output "api_health=$($api.StatusCode)"
