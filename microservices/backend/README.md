# Backend microservices (safe parallel setup)

This is an **additive** microservice layout that reuses the existing domain modules while keeping the monolith intact.
Each service is a small Spring Boot runner with **explicit component scanning** and **explicit controller imports**.
Nothing in the current build is modified.

## Services and default ports
- auth-service (8081) — auth, session, account, csrf
- profile-service (8082) — profile read/edit, connections, media uploads
- search-service (8083) — search + suggest (Elasticsearch on)
- staticdata-service (8084) — static data for UI
- notification-service (8085) — mail templates + outbound mail worker (placeholder for event bus)

## How to run (local)
1) Build the root project once (installs shared modules to local Maven repo):
   `mvn -DskipTests install`
2) Run a service directly:
   - `mvn -f microservices/backend/services/auth-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/profile-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/search-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/staticdata-service/pom.xml spring-boot:run`
   - `mvn -f microservices/backend/services/notification-service/pom.xml spring-boot:run`

## Configuration notes
- Each service has its own `application.yml` with a dedicated port and `spring.application.name`.
- Auth + Profile services set `app.search.elasticsearch.enabled=false` to avoid a hard ES dependency.
- Search service keeps Elasticsearch enabled (default).
- For services that do not need security, `SecurityAutoConfiguration` is disabled in their `application.yml`.

## Gateway
A minimal Nginx gateway config is provided at `microservices/backend/gateway/nginx.conf` to route API paths.
It is optional and does not affect the monolith.

## Future extraction (recommended modern path)
- Replace in-process Spring events with an outbox + message broker (Kafka/Redpanda/NATS).
- Split shared DB into per-service databases (start with auth + search).
- Replace session/CSRF with JWT/OIDC at the gateway and in services.

This setup is intentionally conservative to keep the system safe while giving you a real microservices runway.
