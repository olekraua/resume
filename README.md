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

## SPA (Angular)

Build the Angular SPA into `frontend/dist/frontend`:

```
cd frontend
npm install
npm run build
```

### Separate frontend/backend (REST)

- Backend: start Spring Boot and allow the SPA origin via `app.cors.allowed-origins`.
- Frontend: set `frontend/src/environments/environment.ts` → `apiBaseUrl` (default `http://localhost:8080`) and run `npm start`.
