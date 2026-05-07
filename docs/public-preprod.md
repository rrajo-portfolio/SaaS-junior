# Public Preproduction

Status: READY_FOR_EXTERNAL_INFRASTRUCTURE

The product scope is complete for local/preproduction review. Public preproduction is an infrastructure phase. It must not expose the local demo-header environment directly.

## Local Preproduction Already Covered

| URL | Purpose |
|---|---|
| `http://127.0.0.1:8080` | Docker Compose preproduction through Nginx. |
| `http://127.0.0.1:8081` | Backend direct port for local diagnostics. |
| `http://127.0.0.1:8082` | Frontend direct port for local diagnostics. |
| `http://127.0.0.1:18080` | Local Kubernetes preproduction through Nginx when deployed. |
| `http://127.0.0.1:18081` | Local Keycloak when bootstrapped. |
| `http://127.0.0.1:8085` | Local Jenkins. |
| `http://127.0.0.1:19090` | Local Prometheus when bootstrapped. |

## External Reviewer Requirements

Required before sharing a link:

- Public HTTPS origin for the app.
- TLS termination through a managed certificate, reverse proxy or approved tunnel.
- `APP_SECURITY_AUTH_MODE=oidc`.
- `VITE_LOGIN_MODE=oidc`.
- Public OIDC authority reachable by the browser.
- Keycloak client redirect URI and web origin matching the public app origin.
- No demo-header authentication in shared preproduction.
- No shared admin password distributed in chat or committed files.
- Firewall rules exposing only the public proxy entrypoint.
- Health and smoke-test evidence after deployment.
- Backup and restore procedure for MySQL volume/data.
- Rollback target: previous Git commit or image tag.
- Secret source outside Git.
- Demo-only data policy.
- Smoke-test mode selected and repeatable.

## Readiness Command

Run the public readiness gate before sharing any URL:

```powershell
.\scripts\preprod-public-readiness.ps1 -BaseUrl "https://public-preprod-host"
```

The command must return `READY_FOR_PUBLIC_PREPROD`. Any `FIX_REQUIRED`, `BLOCKED_BY_EXTERNAL_INFRASTRUCTURE` or `BLOCKED_BY_LOCAL_ENVIRONMENT` result blocks sharing the link.

Run smoke checks after every shared-preprod deploy:

```powershell
.\scripts\preprod-smoke.ps1 -BaseUrl "https://public-preprod-host"
```

For Docker Compose preproduction, create a rollback snapshot before deploy:

```powershell
.\scripts\preprod-compose-snapshot.ps1 -RollbackRef "preprod-before-change"
```

Create local/preprod backups before sharing or deploying:

```powershell
.\scripts\backup-compose-preprod.ps1
```

Rollback requires an explicit confirmation switch:

```powershell
.\scripts\preprod-compose-rollback.ps1 -RollbackRef "preprod-before-change" -ConfirmRollback
```

## Safe Publication Options

Use one of these options:

- VPS or cloud VM with Docker Compose, firewall and TLS proxy.
- Kubernetes cluster with ingress controller, TLS and OIDC configured.
- Temporary tunnel only if it provides HTTPS and OIDC is enforced before the tunnel is opened.

For a short client demo without buying a domain, a temporary Quick Tunnel can expose only the Nginx proxy:

```powershell
$env:PUBLIC_DEMO_BASIC_AUTH_USER = "reviewer"
$env:PUBLIC_DEMO_BASIC_AUTH_PASSWORD = "<set outside Git>"
docker compose -f infra/docker/docker-compose.preprod.yml -f infra/docker/docker-compose.public-demo.yml up -d --build nginx
.\scripts\start-quick-tunnel.ps1 -BaseUrl "http://localhost:8080"
```

This returns a temporary `https://*.trycloudflare.com` URL. Keep the tunnel process open while the reviewer uses it. Do not use this as production or long-lived preproduction.

Ngrok can be used instead when the machine has a verified ngrok account and local authtoken. A free ngrok account can provide a static dev domain in the ngrok dashboard.

```powershell
$env:NGROK_AUTHTOKEN = "<set outside Git>"
$env:NGROK_DOMAIN = "<optional-static-domain-from-ngrok>"
$env:PUBLIC_DEMO_BASIC_AUTH_USER = "reviewer"
$env:PUBLIC_DEMO_BASIC_AUTH_PASSWORD = "<set outside Git>"
docker compose -f infra/docker/docker-compose.preprod.yml -f infra/docker/docker-compose.public-demo.yml up -d --build nginx
.\scripts\start-ngrok-tunnel.ps1 -BaseUrl "http://localhost:8080"
```

If `NGROK_DOMAIN` is omitted, ngrok creates a dynamic URL. If it is set, ngrok requests that static dev domain.

## Do Not Do Yet

- Do not publish the current demo mode to the internet.
- Do not use HTTP for external reviewers.
- Do not commit `.env`, `.env.example`, credentials, certificates, database dumps or kubeconfig files.
- Do not claim AEAT, Verifactu or e-invoice legal production compliance.
- Do not enable real production AEAT dispatch.
- Do not expose MySQL, Jenkins, Prometheus, Keycloak admin or backend direct ports publicly.
