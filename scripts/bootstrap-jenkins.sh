#!/usr/bin/env sh
set -eu

COMPOSE_FILE="${COMPOSE_FILE:-infra/jenkins/docker-compose.jenkins.yml}"
URL="${JENKINS_LOGIN_URL:-http://127.0.0.1:8085/login}"

if [ -z "${JENKINS_ADMIN_ID:-}" ]; then
  echo "Set JENKINS_ADMIN_ID in the local shell or a non-versioned .env file." >&2
  exit 1
fi

if [ -z "${JENKINS_ADMIN_PASSWORD:-}" ]; then
  echo "Set JENKINS_ADMIN_PASSWORD in the local shell or a non-versioned .env file." >&2
  exit 1
fi

docker compose -f "$COMPOSE_FILE" config --quiet
docker compose -f "$COMPOSE_FILE" up --build -d

attempt=1
while [ "$attempt" -le 40 ]; do
  if curl -fsS "$URL" >/dev/null; then
    echo "jenkins_ready url=$URL"
    exit 0
  fi
  attempt=$((attempt + 1))
  sleep 5
done

echo "Jenkins did not become ready at $URL" >&2
exit 1
