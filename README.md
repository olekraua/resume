# Resume

## Local SMTP setup

Create a `.env` file with your SMTP credentials:

```
SMTP_USER=you@gmail.com
SMTP_PASS=app_password
```

Export the variables before running the app:

```
export SMTP_USER="you@gmail.com"
export SMTP_PASS="app_password"
```

The app reads them via `spring.mail.username=${SMTP_USER}` and `spring.mail.password=${SMTP_PASS}`.

## Frontend (Angular)

The SPA lives in a separate repo (local path: `/Users/oleksandrkravchenko/Desktop/resume-frontend`).

Run it from there:

```
cd /Users/oleksandrkravchenko/Desktop/resume-frontend
npm install
npm start
```

Production build:

```
npm run build
```

### Separate frontend/backend (REST)

- Backend: start Spring Boot and allow the SPA origin via `app.cors.allowed-origins`.
- Frontend: set `src/environments/environment.ts` → `apiBaseUrl` (default `http://localhost:8080`).

## Microservices + Kubernetes
- Backend services: `microservices/backend`
- Micro‑frontends: see `/Users/oleksandrkravchenko/Desktop/resume-frontend`
- K8s manifests: `microservices/infra/k8s`

Start here:
```
cat microservices/backend/README.md
cat microservices/infra/k8s/README.md
```

## Database migrations (Flyway)

Monolith Flyway migrations live in `app/src/main/resources/db/migration/app` (see
`spring.flyway.locations` in `app/src/main/resources/application.properties`) and must follow
`V<version>__<description>.sql`. Schema dump snapshots are kept in `db/schema-dumps/` so
they are not executed as migrations.
