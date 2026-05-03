# Public Preproduction

Status: BLOCKED_BY_EXTERNAL_INFRASTRUCTURE

The current preproduction environment is local. It is valid for local review and automated tests, but it is not a controlled public environment.

## Current Local URLs

| URL | Purpose |
|---|---|
| `http://127.0.0.1:18080` | Local Kubernetes preproduction through Nginx. |
| `http://127.0.0.1:18081` | Local Keycloak when bootstrapped. |
| `http://127.0.0.1:8085` | Local Jenkins. |
| `http://127.0.0.1:19090` | Local Prometheus when bootstrapped. |

## Required For External Reviewers

A safe public preproduction needs:

- DNS name.
- TLS certificate automation.
- OIDC authentication enforced.
- Firewall rules.
- Hosting target such as VPS, cloud Kubernetes or a controlled tunnel.
- No default or shared credentials.
- Logs and metrics visible to the operator.

## Gate Rule

Do not expose the local machine directly to the internet as production-like preproduction. Create a dedicated hosted environment or an approved temporary tunnel with OIDC and TLS.
