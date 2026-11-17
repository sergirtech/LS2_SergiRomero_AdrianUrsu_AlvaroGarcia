# Prompt 09 — Persistent playlists and profile-photo management

## Approved scope

Implement the remaining profile-management features from the ProTube request:

- Persist each user's playlists and video membership in the database through the backend's hexagonal ports/adapters.
- Add authenticated playlist list/create/rename/delete and video add/remove operations, enforce playlist ownership,
  and preserve memberships consistently when a referenced video is deleted.
- Replace browser-local playlist persistence with authenticated API calls and surface loading and error states in the
  profile interface.
- Persist an authenticated user's PNG, JPG, JPEG, or SVG profile photo, support replacement and removal, and use the
  default avatar when no custom photo is stored.
- Validate file size/type on both client and server, sanitize SVG before serving it, add focused tests, and update
  architecture and project-status documentation.

## Project constraints

- Read `AGENTS.md`, `backend/AGENTS.md`, `frontend/AGENTS.md`, `REQUIREMENT.md`, and the applicable
  `.github/instructions/` files before implementation.
- Keep backend dependencies directed inward (`infrastructure` → `application` → `domain`); controllers use DTOs and
  input ports, services use output ports, and persistence adapters own JPA.
- Use the authenticated session identity for ownership; do not expose another user's private profile-photo mutation
  operations.
- Store binary content in PostgreSQL-compatible JPA storage and never commit credentials or generated media.
- Add regression tests, run backend `clean verify` and frontend build/tests/lint, and update the status and
  architecture documentation.
