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

function Assert-AuthenticatedHttpOk {
  param(
    [string]$Url,
    [hashtable]$Headers
  )

  $lastError = $null
  foreach ($attempt in 1..6) {
    try {
      $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -Headers $Headers -TimeoutSec 15
      if ($response.StatusCode -eq 200) {
        return
      }
      $lastError = "$Url returned HTTP $($response.StatusCode)"
    } catch {
      $lastError = $_.Exception.Message
    }
    Start-Sleep -Seconds 2
  }

  throw "Authenticated smoke check failed for $Url. Last error: $lastError"
}

kubectl -n $Namespace get pods
if ($LASTEXITCODE -ne 0) {
  throw "Unable to read Kubernetes pods."
}

Assert-HttpOk "$BaseUrl/healthz"
Assert-HttpOk "$BaseUrl/api/health"
$userHeaders = @{ "X-User-Email" = "ana.admin@fiscalsaas.local" }
$tenantHeaders = @{
  "X-User-Email" = "ana.admin@fiscalsaas.local"
  "X-Tenant-Id" = "10000000-0000-0000-0000-000000000001"
}
Assert-AuthenticatedHttpOk "$BaseUrl/api/me" $userHeaders
Assert-AuthenticatedHttpOk "$BaseUrl/api/tenants/10000000-0000-0000-0000-000000000001/companies" $tenantHeaders
Assert-AuthenticatedHttpOk "$BaseUrl/api/tenants/10000000-0000-0000-0000-000000000001/business-relationships" $tenantHeaders
Assert-AuthenticatedHttpOk "$BaseUrl/api/tenants/10000000-0000-0000-0000-000000000001/documents" $tenantHeaders
Assert-AuthenticatedHttpOk "$BaseUrl/api/tenants/10000000-0000-0000-0000-000000000001/invoices" $tenantHeaders
Assert-AuthenticatedHttpOk "$BaseUrl/api/tenants/10000000-0000-0000-0000-000000000001/verifactu/records" $tenantHeaders
Assert-AuthenticatedHttpOk "$BaseUrl/api/tenants/10000000-0000-0000-0000-000000000001/verifactu/records/verify" $tenantHeaders
Assert-HttpOk "$BaseUrl/"
