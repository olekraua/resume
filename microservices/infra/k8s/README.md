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
- OIDC redirect URLs у `config-common.yaml` потрібно під ваш домен.
- Для продакшну бажано винести секрети у Secret Manager.
- У `dev/` додано RabbitMQ для outbox‑relay (локальна індексація ES).
- У `dev/` запускаються два outbox‑relay інстанси: profile → search та auth → notification.
