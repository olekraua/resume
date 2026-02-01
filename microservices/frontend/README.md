# Frontend micro-frontends (safe blueprint)

This is a **blueprint** for migrating the current Angular 17 SPA into micro-frontends without breaking the existing app.
It does not modify `/Users/oleksandrkravchenko/Desktop/resume-frontend`.

## Target layout (Module Federation)
- shell (host) — layout, routing, auth shell, shared UI shell
- mf-auth — login/register/restore/account
- mf-profile — profile view/edit
- mf-search — search + suggest
- mf-staticdata — cached static lookups
- mf-admin (optional)

## Recommended stack (Angular 17)
- `@angular-architects/module-federation` (Webpack Module Federation)
- Shared design system library (Angular library project)
- Feature-contracts as TypeScript interfaces per micro-frontend

## Safe migration steps
1) Keep existing SPA as the **shell** app.
2) Create new remote apps for each domain (auth/profile/search/staticdata).
3) Expose each remote’s main routes via Module Federation.
4) Gradually move routes/components from shell into the remotes.
5) Keep a compatibility layer so current routing continues to work.

## Backend alignment
- Use the gateway to route `/api/*` to the correct backend service.
- Keep `apiBaseUrl` pointing to the gateway so the frontend doesn’t need per-service URLs.

When you are ready, I can scaffold the micro-frontend workspace in the frontend repo.
