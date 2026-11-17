# Architecture

How the parts of ProTube fit together. Rules and commands are in [`AGENTS.md`](../AGENTS.md); the reasons behind the
choices are in [`decisions.md`](decisions.md).

## Overview

```
Browser ──► React SPA (Vite dev server :5173, or bundled into the backend jar in prod)
              │  fetch / axios to ${VITE_API_DOMAIN}/api and ${VITE_MEDIA_DOMAIN}/media  (src/utils/Env.ts)
              ▼
            Spring Boot (:8080)
              ├── /api/**    web adapter → input port → service → output port → JPA adapter → H2 (dev) / PostgreSQL (prod)
              └── /media/**  static files from the video store folder (pro_tube.store.dir = ENV_PROTUBE_STORE_DIR)
                                ▲
            tooling/videoGrabber writes .mp4 / .webp / .json triples into the store folder
```

## Backend (`backend/`, package `com.tecnocampus.LS2.protube_back`)

Hexagonal architecture (ports and adapters), see `decisions.md` D20:

```
        infrastructure/web ──► application/port/in ◄── application/service ──► application/port/out ◄── infrastructure/persistence
        (driving adapter)       (use cases)               (implements in,         (UserRepository,          (JPA adapter)
                                                           uses out)               PasswordHasher)   ◄── infrastructure/security
                                         all of them use domain/ (User, exceptions) — domain depends on nothing
```

| Hexagon part        | Package                        | Contents today                                                         |
|---------------------|--------------------------------|------------------------------------------------------------------------|
| Domain              | `domain/model`, `domain/exception` | `User`, `Video`, `VideoComment`, `Playlist`, `ProfilePhoto`, metadata-only `VideoSummary`, `VideoFile`, `VideoPage`; user/video/comment/playlist/photo exceptions |
| Input ports         | `application/port/in`          | Auth, video, comment, playlist, and profile-photo use cases |
| Output ports        | `application/port/out`         | `UserRepository`, `PasswordHasher`, `VideoRepository`, `CommentRepository`, `PlaylistRepository`, `ProfilePhotoRepository` |
| Use cases           | `application/service`          | `AuthService`, `VideoService`, `CommentService`, `PlaylistService`, `ProfilePhotoService` |
| Web adapter         | `infrastructure/web`           | Auth/video/comment APIs, authenticated playlist and own-profile-photo APIs; DTOs in `dto/`, exception handler |
| Persistence adapter | `infrastructure/persistence`   | `app_user`, `video`, `video_content`, `video_comment`, `comment_like`, `playlist`, `playlist_video`, `user_profile_photo` tables; JPA repositories and adapters |
| Security adapters   | `infrastructure/security`      | `BCryptPasswordHasher`, `AppUserDetailsService`, `GoogleOAuth2UserService` (verified Google identity provisioning) |
| Configuration       | `infrastructure/config`        | `MvcConfig` (`/media/**`, CORS), `SecurityConfig` (filter chain: public routes, 401, logout; BCrypt `PasswordEncoder`; `AuthenticationManager`) |

Rules: dependencies only point inwards (`infrastructure` → `application` → `domain`); `domain` has no framework
imports; controllers depend on input ports, services on output ports; HTTP bodies are DTO records in
`infrastructure/web/dto/`, never domain objects or JPA entities.

Video metadata and MP4 bytes are stored in separate tables. Catalog queries only select `video` metadata, so
listing never loads the binary payload. PostgreSQL stores the `VARBINARY` content column as `bytea`. The public
catalog uses a descending-ID cursor: the initial page defaults to 20 items and subsequent cursor requests default
to 10; `nextCursor: null` marks the end. `GET /api/videos/{id}/file` returns the MP4 inline for playback, and
`GET /api/videos/mine` returns the authenticated user's own paginated uploads. Upload, metadata edit and delete
require a session and are restricted to the owning user.

Comments are stored in `video_comment`, with optional self-referencing parent rows for replies and a unique
`comment_like` row per (comment, user). `GET /api/videos/{id}/comments` is public; creation, replies, edits, deletion,
and like toggles require a session. Editing/deletion are restricted to the author; deleting a parent removes its
reply subtree. `GET /api/comments/mine` supports the profile comment manager.

Playlists belong to one user and persist their ordered video membership in `playlist` and `playlist_video`.
`/api/playlists` supports listing and creation; playlist routes support rename, add/remove video, and deletion.
Every operation is session-authenticated and scoped to the owner; deleting a video removes it from saved playlists.
Profile photos are stored in `user_profile_photo` as PostgreSQL-compatible binary content. Authenticated
`/api/users/me/photo` supports retrieval, upload, and removal. Uploads are limited to 5 MB and PNG/JPG/JPEG/SVG;
the server verifies raster signatures and sanitizes SVG XML before persistence. Removing a photo restores the
frontend's default avatar.

Profiles: `dev` (default) uses in-memory H2 with `ddl-auto=update` and the console at `/h2-console`
(`jdbc:h2:mem:mydb`, user `sa`). `prod` points to PostgreSQL 16 (`compose.yaml`, service `db`) through `ENV_PROTUBE_DB_*`
variables; the global `ddl-auto=update` creates/updates the schema there too (no migrations yet).

## Frontend (`frontend/`)

* `main.tsx` wraps `App` in a `BrowserRouter`; `App.tsx` composes `Navbar` + `Sidebar` and routes the main area:
  `/` videos, `/login` `LoginPage`, `/register` `RegisterPage`.
* `useAllVideos` loads `/api/videos` (20 initially, then cursor pages of 10); `videoApi.ts` validates and maps API
  metadata. The home grid requests more pages through an `IntersectionObserver` sentinel, with a manual load-more
  fallback.
* `WatchPage` loads `/api/videos/{id}` and plays `/api/videos/{id}/file`; it reports not-found and API failures
  instead of rendering a broken player. Components otherwise receive data as props.
* `useProfileLists` uses the session-backed playlist API; list create/rename/delete and video membership changes
  persist on the backend. `useProfilePhoto` loads the authenticated user's photo and manages upload/removal.
* `useAuth` (`src/useAuth.tsx`) holds the signed-in user in a context provided by `AuthProvider` in `main.tsx`;
  it throws `AuthError` (`status`, `message`, `fieldErrors`) when the API answers 400 / 401 / 409 or is unreachable.
* "Sign in" in `Navbar` links to `/login`; `RegisterPage` and `LoginPage` hold the forms
  (F3, F4). `App` reads `useAuth` and passes the user and sign out to `Navbar`, which shows the username and
  "Sign out" when signed in (F5).

## Authentication flow (target design, Epic 1 — in progress)

```
RegisterPage / LoginPage ──► useAuth (fetch, credentials: 'include')
   POST /api/auth/register  → validate DTO → AuthService.register (normalize email, check duplicates, BCrypt)
                              → start session → 201 UserResponse
   POST /api/auth/login     → AuthenticationManager → AppUserDetailsService → session cookie JSESSIONID → 200
   /oauth2/authorization/google → Google → verify email → GoogleOAuth2UserService → provision/link local user
                              → principal name = normalized local email → session cookie → frontend
   GET  /api/auth/me        → current user from the session, 401 without one
   POST /api/auth/logout    → Spring Security invalidates the session → 204
Errors (ApiExceptionHandler): 400 {message, errors{field: msg}} · 409 {message} · 401 {message}
```

Authentication, video, comments, playlists, and profile-photo endpoints listed in the backend table are implemented;
remaining product follow-ups are tracked in [`project-status.md`](project-status.md).
