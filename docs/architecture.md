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
| Domain              | `domain/model`, `domain/exception` | `User` (plain Java, immutable, `normalizeEmail`), `UserAlreadyExistsException`, `UserNotFoundException` |
| Input ports         | `application/port/in`          | `RegisterUserUseCase`, `FindUserUseCase`, `ListVideosUseCase`          |
| Output ports        | `application/port/out`         | `UserRepository`, `PasswordHasher`                                     |
| Use cases           | `application/service`          | `AuthService` (register, `getByEmail`), `VideoService` (placeholder list) |
| Web adapter         | `infrastructure/web`           | `AuthController` (`/api/auth/register`, `/login`, `/me`), `ApiExceptionHandler` (400/401/409 JSON), DTOs in `dto/`, `VideosController` (`GET /api/videos`), `TestController` (`/api/hello`), `IndexController` (Thymeleaf leftovers) |
| Persistence adapter | `infrastructure/persistence`   | `UserJpaEntity` → table `app_user` (`id`, unique `email`, unique `username`, `password_hash`), `UserJpaRepository` (Spring Data, package-private), `UserPersistenceAdapter` |
| Security adapters   | `infrastructure/security`      | `BCryptPasswordHasher`, `AppUserDetailsService` (Spring Security user loading via `FindUserUseCase`) |
| Configuration       | `infrastructure/config`        | `MvcConfig` (`/media/**`, CORS), `SecurityConfig` (filter chain: public routes, 401, logout; BCrypt `PasswordEncoder`; `AuthenticationManager`) |

Rules: dependencies only point inwards (`infrastructure` → `application` → `domain`); `domain` has no framework
imports; controllers depend on input ports, services on output ports; HTTP bodies are DTO records in
`infrastructure/web/dto/`, never domain objects or JPA entities.

Profiles: `dev` (default) uses in-memory H2 with `ddl-auto=update` and the console at `/h2-console`
(`jdbc:h2:mem:mydb`, user `sa`). `prod` points to PostgreSQL through `ENV_PROTUBE_DB_*` variables and defines no
`ddl-auto`, so the schema strategy for prod is still open.

## Frontend (`frontend/`)

* `main.tsx` wraps `App` in a `BrowserRouter`; `App.tsx` composes `Navbar` + `Sidebar` and routes the main area:
  `/` videos, `/login` `LoginPage`, `/register` `RegisterPage`.
* Hooks (`src/use*.ts`) call the API; components are presentational and receive data as props.
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
   GET  /api/auth/me        → current user from the session, 401 without one
   POST /api/auth/logout    → Spring Security invalidates the session → 204
Errors (ApiExceptionHandler): 400 {message, errors{field: msg}} · 409 {message} · 401 {message}
```

Only the parts listed in the backend table above exist today; see [`project-status.md`](project-status.md).
