# Local Environment Status

Date: 2026-05-03

## Tool Check

| Tool | Status |
|---|---|
| Docker CLI | Installed, version 28.5.2. |
| Docker daemon | Running, server version 28.5.2. |
| Docker Compose | Installed, version 2.40.3. |
| kubectl | Installed, client version 1.34.2. Local kind cluster validated. |
| Helm | Installed, version 4.0.0. |
| kind | Installed, version 0.31.0. |
| minikube | Not installed. Not required while kind is the selected local cluster provider. |
| Java | Installed, Java 21. |
| Node.js | Installed. |
| npm | Installed. |
| Git | Installed. |
| GitHub CLI | Not installed. |

## Current Blockers

- `BLOCKED_BY_GITHUB_PERMISSIONS`: PR creation through the available integration returned forbidden permissions.

## Validated Runtime

- Docker Desktop recovered after disabling the broken Desktop Kubernetes startup path and rewriting Docker's `settings-store.json` as UTF-8 without BOM.
- `docker compose -f infra/docker/docker-compose.preprod.yml config` passed.
- `docker compose -f infra/docker/docker-compose.preprod.yml up -d --build --force-recreate` passed.
- Preprod containers for MySQL, backend, frontend and Nginx reached healthy state.
- `kind` cluster `fiscal-saas-preprod` was created with Kubernetes v1.34.3.
- `kubectl get nodes` passed with the kind control plane in `Ready` state.
- Kubernetes namespace `fiscal-saas-preprod` was created.
- Kubernetes preprod workloads for MySQL, backend, frontend and Nginx reached `Running` and ready state.
- `scripts/k8s-smoke-preprod.ps1` passed against `http://127.0.0.1:18080`.

