# Identity And Tenancy

Phase 2 introduces the minimum domain required for tenant isolation.

## Domain

| Entity | Purpose |
|---|---|
| `Tenant` | Isolates data by workspace. |
| `AppUser` | Represents an application user known by email. |
| `Membership` | Connects users to tenants with one fiscal role. |
| `Company` | Stores tenant-scoped companies and B2B relationships. |

## Roles

The backend supports these role values:

| Role |
|---|
| `platform_admin` |
| `tenant_admin` |
| `fiscal_manager` |
| `accountant` |
| `client_user` |
| `auditor` |
| `readonly` |

## Local Authentication

The current phase uses database-backed development authentication headers:

| Header | Purpose |
|---|---|
| `X-User-Email` | Resolves an active user. |
| `X-Tenant-Id` | Selects the requested tenant for tenant-scoped endpoints. |

No password, token or client secret is stored in the repository. Keycloak/OIDC integration remains a later infrastructure phase and must replace this development authentication before production-like environments.

## Tenant Guard

Tenant-scoped endpoints require:

- an authenticated active user;
- a matching `X-Tenant-Id` header;
- an active membership for the requested tenant.

Cross-tenant access returns `403 tenant_access_denied`.
