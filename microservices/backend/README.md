# Backend microservices (OIDC + DB per service)

Це повноцінний microservices‑режим, який живе паралельно до моноліту.
Можна запускати по сервісах або через Kubernetes.

## Services and ports
- auth-service (8081) — OIDC Authorization Server, login/register/restore, account
- profile-service (8082) — profile read/edit, connections, media uploads
- search-service (8083) — Elasticsearch‑only search/suggest
- staticdata-service (8084) — static data for UI
- notification-service (8085) — mail sender (optional; RabbitMQ async)
- messaging-service (8086) — realtime messaging
- outbox-relay-service (no HTTP) — publishes profile outbox events to RabbitMQ (optional)
- gateway (8080) — Nginx API gateway

## Data & messaging
- PostgreSQL per service: `resume_auth`, `resume_profile`, `resume_staticdata`, `resume_messaging`
- Elasticsearch: search service only (runs in Docker)
- RabbitMQ (optional): profile outbox → RabbitMQ → search indexing consumer
- RabbitMQ (optional): auth outbox → RabbitMQ → notification consumer
- Restore email: auth-service публікує подію `RestoreAccessMailRequestedEvent` і може обробляти її локально
  (включи `APP_RESTORE_MAIL_ENABLED=true` + SMTP), або асинхронно через RabbitMQ
  (outbox‑relay `auth` + notification-service; локальну відправку в такому разі вимкни).

## Security
- `auth-service` — Spring Authorization Server (OIDC)
- Other services — JWT Resource Server
- Profile internal API uses `X-Internal-Token`

## How to run (local)
1) Start Elasticsearch (Docker only):
   `docker compose -f docker-compose.elasticsearch.yml up -d`
2) Start PostgreSQL natively and create DBs:
   `createdb resume_auth`
   `createdb resume_profile`
   `createdb resume_staticdata`
   `createdb resume_messaging`
   (Adjust DB user/owner as needed.)
3) Build once:
   `mvn -DskipTests install`
4) Run services:
   - `mvn -f microservices/backend/services/auth-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/profile-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/search-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/staticdata-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/notification-service/pom.xml spring-boot:run` (optional)
   - `mvn -f microservices/backend/services/messaging-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/outbox-relay-service/pom.xml spring-boot:run` (optional, RabbitMQ)
     - For auth outbox: set `APP_OUTBOX_RELAY_MODE=auth` and point `SPRING_DATASOURCE_URL` to `resume_auth`.
5) Run local gateway (recommended):
   - Native Nginx: use `microservices/backend/gateway/nginx.local.conf`, but replace `host.docker.internal` with `127.0.0.1`.
   - Docker gateway:
   `docker run --rm --name resume-gateway -p 8080:8080 -v "$PWD/microservices/backend/gateway/nginx.local.conf:/etc/nginx/nginx.conf:ro" nginx:1.27-alpine`

Note: on Linux add `--add-host=host.docker.internal:host-gateway` to the gateway command.

Required: gateway mode expects X-Forwarded headers + `server.forward-headers-strategy=framework` enabled in services
(already set in configs) and one canonical issuer via gateway (`AUTH_ISSUER_URI`).
For local run use `AUTH_ISSUER_URI=http://localhost:8080`.
For k8s/prod use public gateway URL and keep it identical in frontend `oidc.issuer`.

## Key config knobs
- `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`
- `PROFILE_INTERNAL_TOKEN`
- `PROFILE_SERVICE_URL`
- `STATICDATA_SERVICE_URL`
- `AUTH_ISSUER_URI`, `AUTH_CLIENT_ID`, `AUTH_REDIRECT_URI`, `AUTH_POST_LOGOUT_REDIRECT_URI`
- `AUTH_ACCESS_TOKEN_TTL`, `AUTH_SIGNING_KEY_TOKEN_TTL`, `AUTH_SIGNING_KEY_CLOCK_SKEW`
- `AUTH_SIGNING_KEY_ENCRYPTION_KEY` (base64 key, 32 bytes, required for auth-service)
- Guardrail: `AUTH_ACCESS_TOKEN_TTL` must be `<= AUTH_SIGNING_KEY_TOKEN_TTL` (fail-fast on startup).
- `APP_CORS_ALLOWED_ORIGINS`
- `ELASTICSEARCH_URL`
- `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASS`
- `APP_OUTBOX_ENABLED`, `APP_OUTBOX_RELAY_*`, `APP_OUTBOX_RELAY_MODE`

## Gateway
Nginx config:
- Local dev (services on host): `microservices/backend/gateway/nginx.local.conf`
- K8s/containers: `microservices/backend/gateway/nginx.conf`
Routes `/api/*`, `/oauth2/*`, `/.well-known/*`, `/login`, `/error`, `/logout` to the correct services.

## Kubernetes
See `microservices/infra/k8s/README.md` for Kustomize and Helm‑based installs.

## Cutover checklist
1) Frontend switches to `authMode: 'oidc'`.
2) Gateway becomes the only public backend entrypoint.
3) Disable monolith deployment / DNS.
