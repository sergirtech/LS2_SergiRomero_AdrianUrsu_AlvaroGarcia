# Project status

Update this file when a step is finished. Last update: 2026-11-25.

## Epic 1 — Authentication (US-01, US-02) · merged to `main` (#2)

Prompt: [`prompts/02-auth-register-login.md`](../prompts/02-auth-register-login.md). Decisions:
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
| C2   | Prompt approved, PR reviewed, squash merge                                 | DoD        | done (#2)        |

Known follow-ups:
* Commits so far on the branch don't follow Conventional Commits; the PR title must
  (`feat(auth): user registration and login (US-01, US-02)`), since it becomes the squash commit.
* NFR-01: `prod` persists in PostgreSQL 16 (schema via `ddl-auto=update`). Checked by hand: a user registered in
  `prod` can still log in after a backend restart. No migrations (Flyway) yet. `dev` still uses in-memory H2.
* NFR-03: done. `docker compose up --build` starts `db` (PostgreSQL) and `app` (the `prod` jar with the SPA inside)
  on http://localhost:8080 with no `.env` or manual steps (demo defaults, `docs/decisions.md` D21, pending team
  confirmation). Checked on a clean volume: SPA, `/api/videos`, `/media`, register, restart, login.
* In prod (SPA inside the jar) a reload on `/login` or `/register` probably returns 404, because the backend
  doesn't forward those routes to `index.html`. Confirmed when running in Docker. Fix in a deployment PR.

## Epic 3 — Video catalog (US-04) · branch `feature/video-catalog` · in progress

Prompt: [`prompts/04-video-catalog.md`](../prompts/04-video-catalog.md). Decision: D21 in
[`decisions.md`](decisions.md).

| Step | Task                                                                                     | Status |
|------|------------------------------------------------------------------------------------------|--------|
| B1   | Domain `Video`, `PageResult`, `VideoNotFoundException`; ports `ListVideosUseCase`, `GetVideoUseCase`, `ImportVideosUseCase`, `VideoRepository`, `VideoMetadataSource` | done |
| B2   | `VideoService` (clamped pagination, get by id, idempotent import)                        | done   |
| B3   | JPA adapter (`video` table) and store adapter `JsonVideoMetadataSource`                  | done   |
| B4   | `AppStartupRunner` import (also in `prod`), `GET /api/videos?page=&size=`, `GET /api/videos/{id}`, 404 JSON | done |
| B5   | Backend tests (67 tests, JaCoCo thresholds met)                                          | done   |
| F1   | `videosApi.ts`, `useVideos` (pages of 12, load more, retry); home with skeleton, error and "Load more" | done |
| F2   | `WatchPage` loads the video by id from the API, not-found and error states               | done   |
| F3   | Frontend tests (`testUtils/mockApi` answers by URL; 81 tests)                            | done   |
| F4   | `Sidebar` fix: every item is a real link with the active state of the current page; Upload (`/upload`) and My videos (`/my-videos`) show a `ComingSoonPage`, History goes to the existing `/profile/history` placeholder | done |
| C1   | Manual check: 60 grabber videos, load more until the end, open a video from page 3, `prod` restart without duplicates | next |
| C2   | Prompt approved, PR reviewed, squash merge                                               | pending |

Known limits, left for later stories:
* Search filters only the pages already loaded (US-08 moves it to the backend).
* `WatchPage` shows title and channel; description, category and tags arrived with US-06/07.
* Profile lists use a fixed sample of 5 videos stored in `localStorage` (US-03).

## Epic 4 — Video upload and management (US-06, US-07) · branch `feature/video-upload` · in progress

Prompt: [`prompts/10-video-upload-management.md`](../prompts/10-video-upload-management.md). Decision: D22 in
[`decisions.md`](decisions.md). Ported from the `US_division` branch onto the catalog model of `main` (D21).

| Step | Task                                                                                     | Status |
|------|------------------------------------------------------------------------------------------|--------|
| B1   | Domain: `Video` with owner, tags and category; `VideoMetadata` rules; `InvalidVideoException`, `VideoAccessDeniedException`, `VideoStorageException` | done |
| B2   | Ports `UploadVideoUseCase`, `EditVideoUseCase`, `DeleteVideoUseCase`, `ListMyVideosUseCase`, `VideoFileStore`; `VideoService` (MP4 checks, file removed if the insert fails, owner-only edit/delete) | done |
| B3   | `video` table: `owner_id`, `category`, `video_tag`; `FileSystemVideoFileStore` (`.part` file renamed when complete) | done |
| B4   | `POST /api/videos` (multipart), `PUT` / `DELETE /api/videos/{id}`, `GET /api/videos/mine` (session); 400/403/413/500 JSON | done |
| B5   | Backend tests, including HTTP tests with the real security chain and a temporary store (115 tests, JaCoCo met) | done |
| F1   | `videosApi.ts`: `uploadVideo`, `updateVideo`, `deleteVideo`, `fetchMyVideoPage`, `VideoApiError` | done |
| F2   | `UploadPage` (`/upload`) and `MyVideosPage` (`/my-videos`: edit inline, delete with confirmation); visitors go to `/login` | done |
| F3   | `WatchPage` shows description, category and tags, "Edit video" link for the owner; no comments error for uploads without `.json` | done |
| F4   | Frontend tests (99 tests, coverage thresholds met)                                       | done   |
| C1   | Manual API check on a running backend (Tomcat): 37 MB upload, `/media` playback with range requests, owner edit, 403 for another user, fake MP4 400, 105 MB file 413, delete removes the file | done |
| C2   | Manual check of the pages in the browser (upload form, My videos, Edit video link)        | next   |
| C3   | Prompt approved, PR reviewed, squash merge                                               | pending |

Known limits:
* Uploads have no thumbnail, duration or resolution (no ffmpeg in the backend yet).
* A new upload appears at the end of the catalog, which is ordered by `id` (D21); to discuss with the team.
* Deleting a video will also have to delete its playlist entries when that branch is merged (comments: done, D23).
* After a login triggered by `/upload`, the user lands on `/`, not back on `/upload`.

## Epic 6 — Comments (US-09) · branch `feature/video-comment` · in progress

Prompt: [`prompts/11-video-comments.md`](../prompts/11-video-comments.md). Decision: D23 in
[`decisions.md`](decisions.md). Idea taken from the `US_division` branch, adapted to the model of `main` (D21, D22).

| Step | Task                                                                                     | Status |
|------|------------------------------------------------------------------------------------------|--------|
| B1   | Domain `Comment`, `InvalidCommentException`; ports `ListCommentsUseCase`, `AddCommentUseCase`, `ImportCommentsUseCase`, `CommentRepository`, `CommentSource`; `CommentService` | done |
| B2   | `video_comment` table (deleted with its video), `JsonCommentSource`, import on startup after the videos | done |
| B3   | `GET` (public) and `POST` (session) `/api/videos/{id}/comments`; 400/401/404             | done   |
| B4   | Backend tests (160 tests, JaCoCo met)                                                    | done   |
| F1   | `videosApi.ts`: `fetchComments`, `addComment`; `useComments`                             | done   |
| F2   | `CommentsSection` in `WatchPage`: list, form, errors, sign-in prompt                     | done   |
| F3   | Frontend tests (109 tests, coverage thresholds met)                                      | done   |
| C1   | Check in the browser on `docker compose` (PostgreSQL): visitor prompt, comment after registering and after logging in, expired session, comments kept after a backend restart | done |
| C2   | Prompt approved, PR reviewed, squash merge                                               | pending |

Known limits:
* Comments have no date in the UI and the list is not paginated.
* After a login from the comment prompt, the user lands on `/`, not back on the video (same as `/upload`).

## Other epics

Done on `main`: base profile page (#6, frontend only: profile data and lists live in `localStorage`, no profile API
yet), PostgreSQL setup for `prod` (#5).

Not started: profile API (US-03), search on the backend (US-08), replies and likes (US-10), analytics (US-11), E2E tests
(NFR-02), full `docker compose` delivery (NFR-03).

## Refactor — hexagonal architecture · merged to `main` (#4)

Backend moved to ports and adapters (`decisions.md` D20, layout in `architecture.md`). No API or behaviour change:
same endpoints, status codes and JSON. Done: domain/application/infrastructure packages, ports, JPA and BCrypt
adapters, tests moved and adapted (47 tests, JaCoCo thresholds met), docs updated. Squash-merged in #4.
