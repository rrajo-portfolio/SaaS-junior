# Keycloak And OIDC

Status: IMPLEMENTED_FOR_LOCAL_PREPROD

The project now supports Keycloak/OIDC without committing secrets.

## Runtime

Local Keycloak is defined in:

- `infra/keycloak/docker-compose.keycloak.yml`
- `infra/keycloak/realm-export.json`

The realm is:

```text
fiscal-saas
```

The frontend public client is:

```text
fiscal-saas-frontend
```

## Backend Mode

Use `APP_SECURITY_AUTH_MODE=oidc` to make the backend require bearer JWTs. In this mode, demo headers are not used for authentication.

The backend reads the issuer from:

```text
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI
```

The JWT `email` claim must match an active `app_users.email` row.

## Frontend Mode

Use `VITE_LOGIN_MODE=oidc` to enable browser login with authorization code flow and PKCE.

The frontend reads:

- `VITE_OIDC_AUTHORITY`
- `VITE_OIDC_CLIENT_ID`
- `VITE_OIDC_REDIRECT_URI`
- `VITE_OIDC_POST_LOGOUT_REDIRECT_URI`
- `VITE_OIDC_SCOPE`

## Local Startup

Provide local Keycloak bootstrap credentials through shell variables or a local `.env` file that is not committed, then run:

```powershell
.\scripts\bootstrap-keycloak.ps1
```

The script starts Keycloak and checks the realm discovery endpoint:

```text
http://127.0.0.1:18081/realms/fiscal-saas/.well-known/openid-configuration
```

## User Mapping

Create local Keycloak users manually for validation. Each user's email must match an active `AppUser` in MySQL. Roles are still authorized by the SaaS database membership model, not by trusting frontend-provided roles.

## Security Notes

- The committed realm contains roles and a public client only.
- No admin password, user password, token or client secret is committed.
- Production must use TLS and a non-dev Keycloak database.
