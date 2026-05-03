param(
  [string]$BaseUrl = "http://127.0.0.1:18080",
  [string]$Namespace = "fiscal-saas-preprod"
)

$ErrorActionPreference = "Stop"

function Assert-HttpOk {
  param(
    [string]$Url
  )

  $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 15
  if ($response.StatusCode -ne 200) {
    throw "$Url returned HTTP $($response.StatusCode)"
  }
}

kubectl -n $Namespace get pods
if ($LASTEXITCODE -ne 0) {
  throw "Unable to read Kubernetes pods."
}

Assert-HttpOk "$BaseUrl/healthz"
Assert-HttpOk "$BaseUrl/api/health"
Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/api/me" -Headers @{ "X-User-Email" = "ana.admin@fiscalsaas.local" } -TimeoutSec 15 | Out-Null
Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/api/tenants/10000000-0000-0000-0000-000000000001/companies" -Headers @{
  "X-User-Email" = "ana.admin@fiscalsaas.local"
  "X-Tenant-Id" = "10000000-0000-0000-0000-000000000001"
} -TimeoutSec 15 | Out-Null
Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/api/tenants/10000000-0000-0000-0000-000000000001/business-relationships" -Headers @{
  "X-User-Email" = "ana.admin@fiscalsaas.local"
  "X-Tenant-Id" = "10000000-0000-0000-0000-000000000001"
} -TimeoutSec 15 | Out-Null
Assert-HttpOk "$BaseUrl/"
