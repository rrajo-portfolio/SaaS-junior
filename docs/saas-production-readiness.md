# SaaS Production Readiness Roadmap

Date: 2026-05-04

Status: READY_FOR_GATEKEEPER

This document tracks the work needed to evolve the preproduction release candidate into a production-ready SaaS.

## Phase 14 Scope

| Area | Status | Evidence |
|---|---|---|
| OIDC backend mode | IMPLEMENTED | Backend can run in `demo` or `oidc` mode. OIDC maps JWT email claims to active application users. |
| Keycloak local runtime | IMPLEMENTED | `infra/keycloak/docker-compose.keycloak.yml` and `infra/keycloak/realm-export.json` create a local realm and public frontend client without committed secrets. |
| Frontend OIDC mode | IMPLEMENTED | Frontend supports `demo` and `oidc` modes. OIDC uses authorization code flow with PKCE. |
| Tenant lifecycle | IMPLEMENTED | `/api/platform/tenants` endpoints create tenants, change plans, change status and expose lifecycle events. |
| SaaS plans | IMPLEMENTED | Flyway V9 creates `starter`, `professional` and `business` plans with limits and feature flags. |
| Observability foundation | IMPLEMENTED | Backend exposes Prometheus metrics through Actuator. Centralized metrics storage remains a later deployment concern. |
| Billing provider | BLOCKED_BY_EXTERNAL_PROVIDER | Stripe or another payment provider requires external account, credentials, webhook endpoint and legal/commercial decisions. |
| Public preproduction | BLOCKED_BY_EXTERNAL_INFRASTRUCTURE | Requires public DNS, TLS, hosting or tunnel policy, firewall rules and authentication sign-off. |
| Production Verifactu certification | BLOCKED_BY_LEGAL_REVIEW | Requires formal legal/certification review and real AEAT production credentials. |

## Recommended Next Gates

1. Validate Keycloak login locally with a manually created user whose email matches an active `AppUser`.
2. Decide public preproduction hosting provider and DNS name.
3. Add TLS automation for the chosen public environment.
4. Choose billing provider and define plan/payment lifecycle rules.
5. Add centralized logs, metrics storage and alerting.
6. Add off-host encrypted backups and a restore drill.
7. Run formal Verifactu/legal review before any production fiscal claims.

## Non-Goals In This Phase

- No payment provider credentials are committed.
- No public DNS or TLS certificate is created automatically.
- No production AEAT endpoint is enabled.
- No Keycloak client secret is committed; the frontend client is public PKCE.
