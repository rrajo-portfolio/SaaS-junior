# Tooling Bootstrap

This project starts from zero infrastructure. Each tool must be installed, checked and validated before a phase can depend on it.

## Docker Desktop

Required for Docker Compose, local MySQL, local preproduction, Jenkins bootstrap, SonarQube bootstrap and Keycloak bootstrap.

Validation:

```powershell
docker version
docker info
docker compose version
```

If `docker info` returns an HTTP 500 from the Docker Desktop Linux engine, restart Docker Desktop. If the error persists, run Docker Desktop repair or reinstall it, then retry the validation commands.

If Docker Desktop reports `invalid character 'ï' looking for beginning of value` while reading `settings-store.json`, the settings file is not valid JSON for Docker's parser. The file must be valid JSON encoded as UTF-8 without BOM before Docker Desktop can start.

## Kubernetes Local Cluster

No Kubernetes cluster is assumed. Install either kind or minikube before any Kubernetes preproduction phase.

kind validation:

```powershell
kind version
kubectl version --client
```

minikube validation:

```powershell
minikube version
kubectl version --client
```

Cluster runtime validation:

```powershell
kubectl get nodes
```

Do not mark Kubernetes as passing until a cluster exists and `kubectl get nodes` succeeds.

## Helm

Validation:

```powershell
helm version
```

Helm charts can only be used after Kubernetes runtime validation passes.

## GitHub CLI

Git can push branches without GitHub CLI. Install GitHub CLI only if local PR management is required.

Validation:

```powershell
gh auth status
```

If GitHub CLI is not installed or authenticated, use normal git remote operations and mark PR automation as blocked by local tooling or permissions.

