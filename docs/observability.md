# Observability

Status: PREPROD_FOUNDATION

The backend exposes health and Prometheus metrics through Spring Boot Actuator.

## Endpoints

| Endpoint | Purpose |
|---|---|
| `/actuator/health` | General health. |
| `/actuator/health/readiness` | Kubernetes readiness probe. |
| `/actuator/health/liveness` | Kubernetes liveness probe. |
| `/actuator/prometheus` | Prometheus metrics scrape endpoint. |

The Prometheus endpoint is unauthenticated for local scraping. In production it must be protected by network policy, private ingress, service mesh authorization or an equivalent control.

## Local Prometheus

Prometheus local runtime is defined in:

```text
infra/observability/docker-compose.observability.yml
```

It reads:

```text
infra/observability/prometheus.yml
```

Start it after the preproduction backend is running:

```powershell
docker compose -f infra\observability\docker-compose.observability.yml up -d
```

Prometheus is exposed at:

```text
http://127.0.0.1:19090
```

## Remaining Production Work

- Central log aggregation.
- Alerting rules.
- Long-term metrics retention.
- Error tracking.
- Dashboards reviewed by operations.
