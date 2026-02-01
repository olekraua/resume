# Frontend micro‑frontends (Module Federation)

Мікро‑фронтенди вже підготовлені у репозиторії `resume-frontend`.
Поточна SPA залишається стабільною, а micro‑frontends доступні під `/mf/*`.

## Remotes
- mf-auth (4201)
- mf-profile (4202)
- mf-search (4203)
- mf-staticdata (4204)

## Host
- frontend (4200)

## Quick start
```
cd /Users/oleksandrkravchenko/Desktop/resume-frontend
npm install
npm run run:all
```

Host app:
- http://localhost:4200/
- micro‑frontends: `/mf/auth`, `/mf/profile`, `/mf/search`, `/mf/staticdata`

## Migration path
1) Переносити маршрути по доменах у відповідні remotes.
2) Виносити shared‑код у спільні бібліотеки.
3) Використовувати gateway для `/api/*`.
