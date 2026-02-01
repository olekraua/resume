# Microservices (non-invasive)

This folder adds a **parallel**, opt-in microservices layout that does **not** change or break the existing monolith.
The current Spring Boot app in `app/` remains the default and still works as-is.

Contents:
- `backend/` — service slices + gateway config + local run notes.
- `frontend/` — micro-frontend architecture blueprint (keeps the existing SPA untouched).

If you want to keep the monolith only, you can ignore this folder entirely.
