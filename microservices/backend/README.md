# Backend microservices (Kafka + OIDC + DB per service)

Це повноцінний microservices‑режим, який живе паралельно до моноліту.
Можна запускати по сервісах або через Kubernetes.

## Services and ports
- auth-service (8081) — OIDC Authorization Server, login/register/restore, account
- profile-service (8082) — profile read/edit, connections, media uploads
- search-service (8083) — Elasticsearch‑only search/suggest
- staticdata-service (8084) — static data for UI
- notification-service (8085) — mail worker (Kafka consumer)
- gateway (8080) — Nginx API gateway

## Data & messaging
- PostgreSQL per service: `resume_auth`, `resume_profile`, `resume_staticdata`
- Elasticsearch: search service only
- Kafka topics:
  - `profile.indexing`
  - `profile.removed`
  - `profile.password.changed`
  - `profile.media.cleanup`
  - `auth.restore.mail.requested`

## Security
- `auth-service` — Spring Authorization Server (OIDC)
- Other services — JWT Resource Server
- Profile internal API uses `X-Internal-Token`

## How to run (local)
1) Start infra (Postgres/Kafka/Elasticsearch):
   `docker compose -f docker-compose.infra.yml up -d`
2) Build once:
   `mvn -DskipTests install`
3) Run services:
   - `mvn -f microservices/backend/services/auth-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/profile-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/search-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/staticdata-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/notification-service/pom.xml spring-boot:run`
4) Run local gateway (recommended):
   `docker run --rm --name resume-gateway -p 8080:8080 -v "$PWD/microservices/backend/gateway/nginx.local.conf:/etc/nginx/nginx.conf:ro" nginx:1.27-alpine`

Note: on Linux add `--add-host=host.docker.internal:host-gateway` to the gateway command.

Required: gateway mode expects X-Forwarded headers + `server.forward-headers-strategy=framework` enabled in services
(already set in configs) and `AUTH_ISSUER_URI=http://localhost:8080`.

## Key config knobs
- `KAFKA_BOOTSTRAP_SERVERS`
- `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`
- `PROFILE_INTERNAL_TOKEN`
- `PROFILE_SERVICE_URL`
- `AUTH_ISSUER_URI`, `AUTH_CLIENT_ID`, `AUTH_REDIRECT_URI`, `AUTH_POST_LOGOUT_REDIRECT_URI`
- `APP_CORS_ALLOWED_ORIGINS`
- `ELASTICSEARCH_URL`

## Gateway
Nginx config:
- Local dev (services on host): `microservices/backend/gateway/nginx.local.conf`
- K8s/containers: `microservices/backend/gateway/nginx.conf`
Routes `/api/*`, `/oauth2/*`, `/.well-known/*`, `/login`, `/logout` to the correct services.

## Kubernetes
See `microservices/infra/k8s/README.md` for Kustomize and Helm‑based installs.

## Cutover checklist
1) Frontend switches to `authMode: 'oidc'`.
2) Gateway becomes the only public backend entrypoint.
3) Disable monolith deployment / DNS.
