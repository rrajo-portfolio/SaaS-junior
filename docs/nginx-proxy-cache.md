# Nginx Proxy and Cache

Preproduction routes all browser and API traffic through the project Nginx reverse proxy.

## Routing

| Route | Upstream | Cache |
|---|---|---|
| `/api/auth/` | backend | never |
| `/api/documents/` | backend | never |
| `/api/fiscal/` | backend | never |
| `/api/verifactu/` | backend | never |
| `/api/einvoice/` | backend | never |
| `/api/` | backend | never |
| `/assets/*-[hash].{js,css,...}` | frontend | public immutable |
| `/assets/` fallback | frontend | no-cache |
| `/` | frontend | no-cache |

Authorization-bearing requests bypass and skip the static cache. Tenant-specific API routes are covered by the `/api/` no-store rule.

## Security

The proxy sets no-sniff, frame denial, referrer policy, content security policy and permissions policy headers. Upload size is capped at 25 MB for document workflows. Long API calls use extended proxy timeouts, but private API responses remain uncached.

## Kubernetes Ingress

`infra/k8s/preprod/ingress.yml` defines an optional Ingress for `fiscal-saas-preprod.local` targeting the in-cluster Nginx Service. The kind preproduction runtime continues to expose the app through NodePort `30080` mapped to `http://127.0.0.1:18080`.

Do not mark Ingress as operational until an ingress controller exists and the host name resolves locally.
