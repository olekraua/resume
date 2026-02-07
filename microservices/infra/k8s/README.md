# Kubernetes (Kustomize)

## Структура
- `base/` — сервіси + gateway + конфіги + PVC
- `dev/` — локальні залежності (Postgres, Elasticsearch)

## Dev install (local cluster)
```
kubectl apply -k microservices/infra/k8s/dev
```

## Kustomize build (dry run)
```
kustomize build microservices/infra/k8s/dev
```

## Prod install (managed services)
1) Використайте managed PostgreSQL/Elasticsearch.
2) Оновіть `base/config-common.yaml` та `base/secret-*.yaml`.
3) Застосуйте тільки базу:
```
kubectl apply -k microservices/infra/k8s/base
```

## Images
Ці маніфести очікують образи:
- `resume/auth-service:latest`
- `resume/profile-service:latest`
- `resume/search-service:latest`
- `resume/staticdata-service:latest`
- `resume/notification-service:latest`
- `resume/messaging-service:latest`
- `resume/outbox-relay-service:latest`

### Приклад build (local)
```
./mvnw -pl microservices/backend/services/auth-service -DskipTests spring-boot:build-image -Dspring-boot.build-image.imageName=resume/auth-service:latest
./mvnw -pl microservices/backend/services/profile-service -DskipTests spring-boot:build-image -Dspring-boot.build-image.imageName=resume/profile-service:latest
./mvnw -pl microservices/backend/services/search-service -DskipTests spring-boot:build-image -Dspring-boot.build-image.imageName=resume/search-service:latest
./mvnw -pl microservices/backend/services/staticdata-service -DskipTests spring-boot:build-image -Dspring-boot.build-image.imageName=resume/staticdata-service:latest
./mvnw -pl microservices/backend/services/notification-service -DskipTests spring-boot:build-image -Dspring-boot.build-image.imageName=resume/notification-service:latest
./mvnw -pl microservices/backend/services/messaging-service -DskipTests spring-boot:build-image -Dspring-boot.build-image.imageName=resume/messaging-service:latest
./mvnw -pl microservices/backend/services/outbox-relay-service -DskipTests spring-boot:build-image -Dspring-boot.build-image.imageName=resume/outbox-relay-service:latest
```

## Notes
- `resume-uploads` PVC використовується profile‑service та messaging‑service для файлів.
- `AUTH_ISSUER_URI` у `config-common.yaml` має бути canonical gateway URL для середовища.
  Той самий URL має використовуватись у frontend (`oidc.issuer`) і backend resource servers (`issuer-uri`).
- `AUTH_SIGNING_KEY_ENCRYPTION_KEY` має приходити із `resume-internal-secret` (base64, 32 bytes).
  Згенерувати: `openssl rand -base64 32`
- OIDC redirect URLs у `config-common.yaml` потрібно під ваш домен.
- Для продакшну бажано винести секрети у Secret Manager.
- У `dev/` додано RabbitMQ для outbox‑relay (локальна індексація ES).
- У `dev/` запускаються два outbox‑relay інстанси: profile → search та auth → notification.

## OIDC smoke (e2e stability)
Скрипт перевіряє сценарій:
1) OIDC discovery (`/.well-known/openid-configuration`) і контракт `issuer`/`jwks_uri`  
2) `signinRedirect` (`/oauth2/authorize`)  
3) callback redirect з `code`  
4) `token` exchange з PKCE  
5) `/api/me` повертає `authenticated=true`  
6) restart одного `auth-service` pod  
7) повторна перевірка `/api/me` через перезапущений pod  
8) `rollout restart` deployment + перевірка `/api/me` через gateway

Запуск:
```
SMOKE_USERNAME=<uid> \
SMOKE_PASSWORD=<password> \
microservices/infra/k8s/scripts/oidc-smoke.sh
```

Опційно:
- `NAMESPACE` (default: `resume`)
- `GATEWAY_SERVICE` (default: `resume-gateway`)
- `CLIENT_ID` (default: `resume-spa`)
- `REDIRECT_URI` (default: `http://localhost:4200/auth/callback`)
- `EXPECTED_ISSUER` (default: значення `AUTH_ISSUER_URI` з `resume-common-config`)
- `AUTH_DEPLOYMENT_NAME` (default: `resume-auth-service`)

## CI/CD contract smoke
- Workflow: `.github/workflows/oidc-contract-smoke.yml`
- Перевіряє OIDC контракт у kind-кластері: canonical `issuer`/`jwks_uri` через gateway, `signinRedirect -> callback -> /api/me(authenticated=true)`, restart+rollout auth deployment, повторна валідація `/api/me`.
- Після smoke запускає `jwt-error-alert-check.sh`: job падає, якщо в логах є `invalid_signature` або `unknown_kid`.
- Запускається на `pull_request`, `push` у `main` (по релевантних шляхах) та вручну через `workflow_dispatch`.

## Gateway edge OIDC error observation
- У gateway додано окремі access-log entries з міткою `edge_oidc_error=1`.
- Логується лише `404/401/5xx` для endpoint-ів `/.well-known/*`, `/userinfo`, `/connect/logout`.
- Для алертів у лог-агрегаторі фільтруйте по `edge_oidc_error=1`.

## JWT validation alerts
- Resource-server логи містять структуровані події:
  `jwt_validation_error=1 jwt_error_type=invalid_signature|unknown_kid`.
- Для лог-алертів використовуйте ці 2 типи помилок (рівень `ERROR`).
- Локальна перевірка:
  `microservices/infra/k8s/scripts/jwt-error-alert-check.sh`
