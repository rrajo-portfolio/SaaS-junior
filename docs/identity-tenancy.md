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

## Authentication Modes

The backend supports two explicit authentication modes.

| Mode | Purpose |
|---|---|
| `demo` | Local and automated test mode using database-backed development headers. |
| `oidc` | Keycloak/OIDC resource-server mode using bearer JWTs. |

The demo mode uses these headers:

| Header | Purpose |
|---|---|
| `X-User-Email` | Resolves an active user. |
| `X-Tenant-Id` | Selects the requested tenant for tenant-scoped endpoints. |

OIDC mode disables demo user headers for authentication and maps the JWT `email` claim to an active `AppUser` row. The frontend supports OIDC authorization code flow with PKCE through the public `fiscal-saas-frontend` client in the local Keycloak realm.

No password, token or client secret is stored in the repository. Local Keycloak bootstrap credentials must be provided outside Git.

## Tenant Guard

Tenant-scoped endpoints require:

- an authenticated active user;
- a matching `X-Tenant-Id` header;
- an active membership for the requested tenant.

Cross-tenant access returns `403 tenant_access_denied`.

Suspended or cancelled tenants are blocked from tenant-scoped endpoints even when the user still has an active membership.

## SaaS Lifecycle

Phase 14 adds platform administration primitives:

| Feature | Purpose |
|---|---|
| Subscription plans | Defines SaaS commercial limits and feature availability. |
| Tenant status | Tracks `ACTIVE`, `SUSPENDED` and `CANCELLED` tenants. |
| Subscription status | Tracks `trialing`, `active`, `suspended` and `cancelled` lifecycle state. |
| Lifecycle events | Records platform-admin actions such as tenant creation, plan changes and suspension. |

Platform endpoints live under `/api/platform` and require `platform_admin` role except `/api/platform/plans`, which is available to authenticated users for plan visibility.
