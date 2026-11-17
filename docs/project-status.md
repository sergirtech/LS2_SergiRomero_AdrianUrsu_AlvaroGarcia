# Project status

Update this file when a step is finished. Last update: 2026-11-16.

## Epic 1 — Authentication (US-01, US-02) · branch `feature/db_setup` · in progress

Prompts: [`prompts/02-auth-register-login.md`](../prompts/02-auth-register-login.md) and
[`prompts/05-google-oauth-login.md`](../prompts/05-google-oauth-login.md). Decisions:
[`decisions.md`](decisions.md). Acceptance criteria: R1–R3 = US-01, L1–L3 = US-02 (see `REQUIREMENT.md`).

| Step | Task                                                                       | Criteria   | Status           |
|------|----------------------------------------------------------------------------|------------|------------------|
| B1   | Dependencies (added per step: `spring-security-crypto` in B3, validation in B4, security starter in B5) | base | done             |
| B2   | `model/User` + `repository/UserRepository` + `UserRepositoryTest`          | R1, R2     | done (committed) |
| B3   | `AuthService.register` (normalize, duplicates, BCrypt) + `SecurityConfig` `PasswordEncoder` + `AuthServiceTest` | R1, R2 | done (committed) |
| B4   | DTOs with Bean Validation (`controller/dto/`) + `ApiExceptionHandler` (400/409) + tests | R2, R3 | done (committed) |
| B5   | Spring Security: `AppUserDetailsService`, filter chain, 401, logout, session | L1–L3     | done (committed) |
| B6   | `AuthController` (`/api/auth/register`, `/login`, `/me`, `/logout`)        | all        | done (committed) |
| B7   | CORS with credentials in `MvcConfig`                                       | L1, L3     | done (committed) |
| B8   | Remaining backend tests, JaCoCo thresholds                                 | DoD        | done (committed) |
| F1   | `react-router`, routes `/login`, `/register`                               | R1, L1     | done             |
| F2   | `useAuth` hook                                                             | R1, L1–L3  | done             |
| F3   | `RegisterPage` with client-side validation                                 | R1–R3      | done             |
| F4   | `LoginPage`                                                                | L1, L2     | done             |
| F5   | `Navbar` signed-in state + sign out                                        | L1, L3     | done             |
| F6   | Frontend tests                                                             | DoD        | done             |
| C1   | Manual end-to-end check of R1–R3 and L1–L3                                 | all        | done             |
| C2   | Prompt approved, PR reviewed, squash merge                                 | DoD        | next             |
| G1   | Google OAuth2, verified-account provisioning/linking, stable subject, session | D22 | done |
| G2   | Google sign-in links on login and registration pages                       | D22        | done             |
| G3   | Manual OAuth sign-in using the team's Google client credentials             | D22        | pending          |

Known follow-ups:
* Commits so far on the branch don't follow Conventional Commits; the PR title must
  (`feat(auth): user registration and login (US-01, US-02)`), since it becomes the squash commit.
* NFR-01: `prod` persists in PostgreSQL 16 (`docker compose up -d db`, schema via `ddl-auto=update`). Checked by
  hand: a user registered in `prod` can still log in after a backend restart. No migrations (Flyway) yet, and the
  backend is not in `compose.yaml` yet.
* Google OAuth is disabled when its credentials are absent; set both `ENV_PROTUBE_GOOGLE_CLIENT_ID` and
  `ENV_PROTUBE_GOOGLE_CLIENT_SECRET` to enable it. The callback URI is `<backend-origin>/login/oauth2/code/google`.
  G3 cannot be manually verified until the team configures its Google OAuth client and the frontend redirect URL.
* In prod (SPA inside the jar) a reload on `/login` or `/register` probably returns 404, because the backend
  doesn't forward those routes to `index.html`. Not verified. Fix in a deployment PR.

## Other epics

Implemented: video catalog/playback (US-04, US-05), upload and metadata management (US-06, US-07), comments
(US-09, US-10), playlist CRUD, profile-photo CRUD, and persistent theme preference. The backend stores MP4 bytes,
metadata, comments, playlist membership, and profile photos using PostgreSQL-compatible JPA tables. Video upload,
edit/delete, playlist operations, and profile-photo operations require an authenticated owner session. Catalog
pagination returns 20 videos initially and 10 for each cursor continuation; the frontend loads additional pages as
the user scrolls. Database videos currently use the existing thumbnail placeholder because the API does not provide
thumbnail images. Theme preference is saved in `localStorage` for signed-in and anonymous visitors.

Backend `.\mvnw.cmd clean verify` passes with 107 tests and JaCoCo thresholds met. Frontend build passes and all 109
frontend tests pass; ESLint passes for changed frontend files. Repository-wide lint remains blocked by the
documented, pre-existing CRLF/Prettier errors in unrelated files.

Frontend catalog/playback integration is implemented under
[`prompts/06-frontend-video-catalog-playback.md`](../prompts/06-frontend-video-catalog-playback.md). Profile video
upload/management is implemented under [`prompts/07-profile-video-management.md`](../prompts/07-profile-video-management.md):
the profile shows the authenticated user's paginated uploads and provides validated MP4 upload, metadata edit, and
confirmed delete controls. Mutations refresh both the owned-video list and the public catalog. Persistent comments,
comments UI, and persistent theme preference are tracked in
[`prompts/08-persistent-comments-and-theme.md`](../prompts/08-persistent-comments-and-theme.md). Playlist persistence
and profile-photo CRUD are tracked in [`prompts/09-playlists-and-profile-photos.md`](../prompts/09-playlists-and-profile-photos.md).

Remaining MVP work: US-08 currently searches only the videos already loaded in the browser rather than querying and
ranking the full catalog; US-11 analytics is not implemented. NFR-02 E2E coverage is not yet in place. NFR-03
containerization currently provides PostgreSQL only; backend and frontend services are not in `compose.yaml`.
Manual Google OAuth verification (G3) requires the team's configured client credentials. Prompt approval, peer
review, and squash merge are team workflow steps still pending.

Search (US-08) currently filters the videos loaded from the API client-side.

## Refactor — hexagonal architecture · branch `feature/hexagonal-refactor` · in progress

Backend moved to ports and adapters (`decisions.md` D20, layout in `architecture.md`). No API or behaviour change:
same endpoints, status codes and JSON. Done: domain/application/infrastructure packages, ports, JPA and BCrypt
adapters, tests moved and adapted (47 tests, JaCoCo thresholds met), docs updated. Pending: team agreement on D20,
prompt approval (`prompts/03-hexagonal-refactor.md`), PR review and squash merge. Branches opened before this
refactor will get package conflicts when they rebase.
