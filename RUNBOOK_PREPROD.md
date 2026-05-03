# Preproduction Runbook

Environment: local kind preproduction.

## Runtime

| Component | Value |
|---|---|
| Cluster | `fiscal-saas-preprod` |
| Namespace | `fiscal-saas-preprod` |
| Public URL | `http://127.0.0.1:18080` |
| Jenkins URL | `http://127.0.0.1:8085/login` |

## Bootstrap

```powershell
.\scripts\k8s-create-kind-cluster.ps1
.\scripts\k8s-deploy-preprod.ps1
```

`scripts/k8s-deploy-preprod.ps1` builds images, loads them into kind, creates the runtime Secret from local `.env`, applies manifests and runs smoke checks.

## Health

```powershell
.\scripts\preprod-health-report.ps1
.\scripts\k8s-smoke-preprod.ps1
```

Expected HTTP health:

- `GET /healthz` returns 200.
- `GET /api/health` returns 200.

## Frontend E2E

```powershell
$env:PLAYWRIGHT_BASE_URL='http://127.0.0.1:18080'
npm run e2e:preprod
```

Run from `frontend/`.

## Backup

```powershell
.\scripts\backup-mysql-preprod.ps1
```

Backups are written under `backups/preprod/`, which is ignored by git. No database dump is committed.

## Logs

```powershell
.\scripts\collect-preprod-logs.ps1
```

Logs are written under `logs/preprod/`, which is ignored by git.

## Jenkins

```powershell
$env:JENKINS_ADMIN_ID='<local-admin-user>'
$env:JENKINS_ADMIN_PASSWORD='<local-admin-password>'
.\scripts\bootstrap-jenkins.ps1
```

The generated job is `fiscal-saas/main`. The local controller mounts the Docker socket and is not a production CI design.

## Cache Checks

Expected proxy headers:

- API: `Cache-Control: no-store`
- Versioned asset: `Cache-Control: public, max-age=31536000, immutable`
- Warm asset request: `X-Cache-Status: HIT`
