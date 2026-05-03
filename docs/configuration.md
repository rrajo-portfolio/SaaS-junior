# Configuration

Runtime configuration is injected through local environment files, shell variables, Jenkins credentials, or Kubernetes Secrets.

No `.env` file or `.env` template is stored in this repository.

## Variables

| Name | Purpose |
|---|---|
| `MYSQL_DATABASE` | MySQL database name used by local and preprod services. |
| `MYSQL_USER` | MySQL application username. |
| `MYSQL_PASSWORD` | MySQL application password. |
| `MYSQL_ROOT_PASSWORD` | MySQL root password for local/preprod bootstrap. |
| `SPRING_DATASOURCE_URL` | JDBC URL consumed by the backend. |
| `SPRING_DATASOURCE_USERNAME` | Backend datasource username. |
| `SPRING_DATASOURCE_PASSWORD` | Backend datasource password. |
| `APP_CORS_ALLOWED_ORIGINS` | Comma-separated frontend/proxy origins allowed by backend CORS. |
| `VITE_API_BASE_URL` | Frontend API base URL used at build time. |
| `VERIFACTU_MODE` | Verifactu adapter mode. Production must stay disabled until an explicit validated phase. |
| `AEAT_PRODUCTION_ENABLED` | Guard flag for AEAT production usage. |

## Local Setup

Create a local `.env` file outside version control with the variables needed by the service you are running. Use non-production credentials only.

For Kubernetes, create Secrets with `kubectl create secret` or a sealed-secret workflow. Do not commit generated secret manifests containing real values.

