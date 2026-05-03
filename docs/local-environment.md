# Local Environment Status

Date: 2026-05-03

## Tool Check

| Tool | Status |
|---|---|
| Docker CLI | Installed, version 28.5.2. |
| Docker daemon | Running, server version 28.5.2. |
| Docker Compose | Installed, version 2.40.3. |
| kubectl | Installed, client only. No cluster was validated. |
| Helm | Installed. |
| kind | Not installed. |
| minikube | Not installed. |
| Java | Installed, Java 21. |
| Node.js | Installed. |
| npm | Installed. |
| Git | Installed. |
| GitHub CLI | Not installed. |

## Current Blockers

- `BLOCKED_BY_LOCAL_ENVIRONMENT`: Kubernetes runtime is not validated because neither kind nor minikube is installed and no local cluster exists.
- `BLOCKED_BY_GITHUB_PERMISSIONS`: PR creation through the available integration returned forbidden permissions.

## Validated Runtime

- Docker Desktop recovered after disabling the broken Desktop Kubernetes startup path and rewriting Docker's `settings-store.json` as UTF-8 without BOM.
- `docker compose -f infra/docker/docker-compose.preprod.yml config` passed.
- `docker compose -f infra/docker/docker-compose.preprod.yml up -d --build --force-recreate` passed.
- Preprod containers for MySQL, backend, frontend and Nginx reached healthy state.

