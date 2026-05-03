# Kubernetes Preproduction

This repository includes a local Kubernetes preproduction runtime based on kind.

## Runtime

| Component | Value |
|---|---|
| Cluster provider | kind |
| Cluster name | `fiscal-saas-preprod` |
| Namespace | `fiscal-saas-preprod` |
| Kubernetes node image | `kindest/node:v1.34.3` pinned by digest |
| Public local URL | `http://127.0.0.1:18080` |

## Versioned Infrastructure

| Path | Purpose |
|---|---|
| `infra/k8s/kind/cluster.yml` | Local kind cluster definition and host port mapping. |
| `infra/k8s/preprod` | Namespace, ConfigMap, workloads, Services, optional Ingress and NetworkPolicies. |
| `infra/docker/mysql.Dockerfile` | Local MySQL runtime image used by kind to avoid cluster-time registry pulls. |
| `scripts/k8s-create-kind-cluster.ps1` | Local cluster bootstrap. |
| `scripts/k8s-deploy-preprod.ps1` | Image build, kind image load, Secret creation from local environment and rollout validation. |
| `scripts/k8s-smoke-preprod.ps1` | HTTP smoke checks through the Nginx NodePort. |

MySQL is built as `fiscal-saas-mysql:preprod` from `mysql:8.4` and loaded into kind with the application images. This keeps local Kubernetes validation independent from cluster-time pulls of large Docker Hub layers.

## Runtime Secret

The Kubernetes Secret is named `fiscal-saas-runtime` and is created locally from `.env`.

| Key | Purpose |
|---|---|
| `MYSQL_DATABASE` | MySQL bootstrap database. |
| `MYSQL_USER` | MySQL application user. |
| `MYSQL_PASSWORD` | MySQL application password. |
| `MYSQL_ROOT_PASSWORD` | MySQL root password. |
| `SPRING_DATASOURCE_USERNAME` | Backend datasource username. |
| `SPRING_DATASOURCE_PASSWORD` | Backend datasource password. |

No Secret manifest with real values is stored in the repository.

## Ingress

An optional Ingress manifest routes `fiscal-saas-preprod.local` to the in-cluster Nginx Service. The local kind bootstrap does not assume an ingress controller exists; the validated path remains the NodePort mapped by `infra/k8s/kind/cluster.yml`.

## Validation State

Kubernetes can be marked as passing only when all of these checks pass in the local environment:

```powershell
kubectl get nodes
kubectl -n fiscal-saas-preprod get pods
.\scripts\k8s-smoke-preprod.ps1
```
