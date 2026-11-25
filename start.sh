#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"

# Load .env
export $(grep -v '^#' "$ROOT/.env" | xargs)

echo "==> Starting MySQL..."
docker-compose -f "$ROOT/docker-compose.yml" up -d

echo "==> Waiting for MySQL to be healthy..."
until docker inspect --format='{{.State.Health.Status}}' cdp-mysql 2>/dev/null | grep -q "healthy"; do
  sleep 2
done
echo "    MySQL is ready."

echo "==> Starting backend..."
cd "$ROOT/backend"
mvn spring-boot:run &
BACKEND_PID=$!

echo "==> Starting frontend..."
cd "$ROOT/frontend"
npm run dev &
FRONTEND_PID=$!

echo ""
echo "    Backend  PID: $BACKEND_PID"
echo "    Frontend PID: $FRONTEND_PID"
echo ""
echo "    Frontend : http://localhost:5173"
echo "    Backend  : http://localhost:8080"
echo ""
echo "    Press Ctrl+C to stop all services."

trap "echo 'Stopping...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; docker-compose -f '$ROOT/docker-compose.yml' stop" EXIT INT TERM

wait $BACKEND_PID $FRONTEND_PID
