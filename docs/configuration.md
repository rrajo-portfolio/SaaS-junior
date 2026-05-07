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
| `APP_SECURITY_AUTH_MODE` | Backend authentication mode. Use `demo` for local header auth and `oidc` for Keycloak/OIDC resource-server mode. |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | OIDC issuer URI consumed by the backend when `APP_SECURITY_AUTH_MODE` is `oidc`. |
| `VITE_API_BASE_URL` | Frontend API base URL used at build time. |
| `VITE_LOGIN_MODE` | Frontend authentication mode used at build time. Use `demo` or `oidc`. |
| `VITE_OIDC_AUTHORITY` | OIDC authority URL used by the frontend. |
| `VITE_OIDC_CLIENT_ID` | Public OIDC frontend client identifier. |
| `VITE_OIDC_REDIRECT_URI` | Frontend redirect URI for OIDC authorization code flow with PKCE. |
| `VITE_OIDC_POST_LOGOUT_REDIRECT_URI` | Frontend URI used after OIDC logout. |
| `VITE_OIDC_SCOPE` | OIDC scopes requested by the frontend. |
| `VITE_APP_ENV` | Build-time label shown in the frontend system-status panel. |
| `VITE_APP_VERSION` | Build-time version or image tag shown in the frontend system-status panel. |
| `VITE_LAST_SMOKE_TEST_AT` | Build-time timestamp or marker for the latest preproduction smoke test. |
| `VERIFACTU_MODE` | Verifactu adapter mode. Production must stay disabled until an explicit validated phase. |
| `AEAT_PRODUCTION_ENABLED` | Guard flag for AEAT production usage. |
| `APP_DOCUMENT_STORAGE_PATH` | Backend filesystem path for preproduction document binaries. |
| `APP_DOCUMENT_MAX_BYTES` | Backend document binary size limit used by the storage validator. |
| `APP_DOCUMENT_MAX_FILE_SIZE` | Maximum accepted single document upload size. |
| `APP_DOCUMENT_MAX_REQUEST_SIZE` | Maximum accepted multipart request size. |
| `JENKINS_ADMIN_ID` | Local Jenkins bootstrap administrator username. |
| `JENKINS_ADMIN_PASSWORD` | Local Jenkins bootstrap administrator password. |
| `JENKINS_REPO_URL` | Git repository URL used by Jenkins Configuration as Code. |
| `JENKINS_REPO_BRANCH` | Git branch used by the generated Jenkins pipeline job. |
| `JENKINS_URL` | Local URL advertised by the Jenkins controller. |
| `KEYCLOAK_BOOTSTRAP_ADMIN_USERNAME` | Local Keycloak bootstrap administrator username. |
| `KEYCLOAK_BOOTSTRAP_ADMIN_PASSWORD` | Local Keycloak bootstrap administrator password. |
| `PREPROD_PUBLIC_ORIGIN` | HTTPS origin used for shared preproduction reviewer access. |
| `PREPROD_TLS_MODE` | TLS mode for shared preproduction, for example managed edge TLS or controlled tunnel TLS. |
| `PREPROD_AUTH_MODE` | Authentication gate for shared preproduction. Must be `oidc` before sharing externally. |
| `PREPROD_BACKUP_MODE` | Backup evidence mode used before sharing preproduction. |
| `PREPROD_ROLLBACK_REF` | Git commit, tag or image reference used as rollback target for shared preproduction. |
| `PREPROD_SECRET_SOURCE` | External secret source used for shared preproduction. |
| `PREPROD_DEMO_DATA_MODE` | Data policy for shared preproduction. Must indicate demo-only data. |
| `PREPROD_SMOKE_TEST_MODE` | Smoke-test evidence mode for shared preproduction. |
| `PUBLIC_DEMO_BASIC_AUTH_USER` | Temporary public-demo perimeter-auth username injected outside Git. |
| `PUBLIC_DEMO_BASIC_AUTH_PASSWORD` | Temporary public-demo perimeter-auth password injected outside Git. |

## Local Setup

Create a local `.env` file outside version control with the variables needed by the service you are running. Use non-production credentials only.

For Kubernetes, create Secrets with `kubectl create secret` or a sealed-secret workflow. Do not commit generated secret manifests containing real values.
