# Jenkins Bootstrap

Phase 11 creates a local/preproduction Jenkins controller from scratch.

Jenkins is managed with Docker Compose and Jenkins Configuration as Code. No Jenkins secrets, tokens or default passwords are stored in git.

## Files

- `infra/jenkins/docker-compose.jenkins.yml`
- `infra/jenkins/controller/Dockerfile`
- `infra/jenkins/controller/plugins.txt`
- `infra/jenkins/casc/jenkins.yaml`
- `infra/jenkins/Jenkinsfile`
- `scripts/bootstrap-jenkins.ps1`
- `scripts/bootstrap-jenkins.sh`

## Required Local Variables

Set these in your shell or in a local `.env` file that is not committed:

| Name | Purpose |
|---|---|
| `JENKINS_ADMIN_ID` | Local Jenkins administrator username. |
| `JENKINS_ADMIN_PASSWORD` | Local Jenkins administrator password. |
| `JENKINS_REPO_URL` | Git repository URL used by the generated pipeline job. |
| `JENKINS_REPO_BRANCH` | Branch used by the generated pipeline job. |
| `JENKINS_URL` | Public URL shown by Jenkins for this local controller. |

## Start

PowerShell:

```powershell
.\scripts\bootstrap-jenkins.ps1
```

POSIX shell:

```sh
./scripts/bootstrap-jenkins.sh
```

Jenkins listens on `http://127.0.0.1:8085/login` by default.

## Pipeline

The generated job is `fiscal-saas/main` and uses `infra/jenkins/Jenkinsfile`.

Main stages:

- backend Maven verify
- frontend lint, unit tests and build
- Playwright local shell
- Docker Compose configuration validation
- runtime image builds
- optional Kubernetes preproduction deploy and smoke
- optional Playwright preproduction critical flows

## Security Notes

The Jenkins Docker Compose file mounts the Docker socket and adds the controller process to socket group `0` for Docker Desktop compatibility. This is local-only and high risk because a Jenkins job can control the host Docker daemon. Do not expose this controller publicly and do not reuse it as a production CI controller without TLS, hardened authorization and isolated agents.

The default CI stages run in sibling Docker containers using `--volumes-from "$HOSTNAME"` so they can access the Dockerized Jenkins workspace. Backend tests also mount the Docker socket because the Testcontainers suite starts MySQL containers. Optional Kubernetes stages require PowerShell, kubectl, access to the local preproduction kubeconfig and the Docker daemon from the controller environment.

The Codex MCP endpoint must not be considered usable until Jenkins is running, authenticated and the MCP plugin or endpoint has been explicitly installed and verified. Candidate local endpoint:

```text
http://localhost:8085/mcp-server/mcp
```
