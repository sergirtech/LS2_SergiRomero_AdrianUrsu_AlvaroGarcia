# Design decisions

Short records of decisions the team must be able to justify. Newest last. Status: **Accepted** (implemented or
agreed), **Proposed** (planned, not implemented yet), **Open** (needs a team decision).

## D1 — Email + password as the baseline login (Accepted)
US-01 literally asks for "an email and password". The Google OAuth2 properties in `application.properties` come from
the course template and were initially unused; Google OAuth was later added alongside this flow in D22.

## D2 — Server session (`JSESSIONID`) instead of JWT (Accepted, step B5)
Native to Spring Security, real logout (the server invalidates the session) and expiry for free through
`server.servlet.session.timeout` (30 min default), which covers US-02 AC3. JWT would make logout and expiry harder
to do right for no MVP benefit.

## D3 — BCrypt password hashes (Accepted)
Slow by design and salted per password; the salt is stored inside the hash, so one `password_hash` column is enough.
The `PasswordEncoder` interface is injected so tests can mock it and the algorithm can change in one place.
Since B5 it comes from `spring-boot-starter-security` (B3 used `spring-security-crypto` alone).

## D4 — Normalize emails (trim + lowercase, `Locale.ROOT`) (Accepted)
Otherwise `Ana@x.com` and `ana@x.com` would be two accounts, breaking US-01 AC2. The same helper
(`User.normalizeEmail`, in the domain since D20) must be used at login. `Locale.ROOT` avoids locale-dependent lowercasing.

## D5 — Duplicate protection in two layers (Accepted)
`AuthService` checks `existsByEmail` / `existsByUsername` to return a clear message; the `UNIQUE` constraints on
`app_user` stop the race where two simultaneous registrations both pass the check.

## D6 — Table name `app_user` (Accepted)
`user` is a reserved word in H2 and PostgreSQL.

## D7 — `username` as the required profile field (Accepted, confirm with the team)
US-01 mentions "required profile fields" without listing them. Username is unique, 3–30 characters.

## D8 — Password policy 8–72 characters (Accepted, step B4)
8 is the usual minimum length; 72 because BCrypt ignores bytes beyond 72.

## D9 — DTO records instead of exposing entities (Accepted, step B4)
Input DTOs prevent mass assignment (`id`, `passwordHash`); `UserResponse` has no hash field, so it can't leak.

## D10 — Validation on both sides (Proposed, B4 + F3)
Client-side for immediate feedback; Bean Validation on the server is the source of truth (US-01 AC3).

## D11 — HTTP errors as JSON from one `@RestControllerAdvice` (Accepted, B4/B5)
400 invalid fields `{message, errors}`, 409 duplicate `{message}`, 401 bad credentials with a generic "Incorrect email
or password" so login doesn't reveal which emails exist (registration does, because US-01 AC2 requires it).

## D12 — 401 instead of redirect for unauthenticated API calls (Accepted, step B5)
Spring Security's default redirects to an HTML login form the SPA doesn't have.

## D13 — CSRF disabled for the MVP (Accepted, step B5, known risk)
With cookie sessions CSRF is a real risk. Options: disable it for the MVP and document the risk, or keep it with
`CookieCsrfTokenRepository` and send `X-XSRF-TOKEN` from the frontend. Decision: disabled for the MVP to keep the
frontend simple; documented as a known risk to revisit before production.

## D14 — CORS with credentials for dev (Accepted, step B7)
The session cookie only travels from `:5173` to `:8080` with `allowCredentials(true)` and an explicit origin pattern
(`http://localhost:[*]`), not `*`. In prod the SPA is served by the backend (same origin). The unused `/auth/**` mapping (OAuth leftover) was removed.

## D15 — Add `spring-boot-starter-security` together with the filter chain (Accepted)
Adding it alone protects every route with a generated password and breaks `GET /api/videos`; each commit must leave
the app working.

## D16 — `react-router` for `/login` and `/register` (Accepted, step F1)
Real URLs and back-button support; also needed later for the video detail page (US-05). Version 7: v8 requires
React ≥ 19.2.7 and Node ≥ 22.22 (the project uses React 19.1 and Node 22).

## D17 — `fetch` in `useAuth` (Accepted, step F2)
Frontend rules require mocking HTTP with `jest-fetch-mock`, and `credentials: 'include'` is explicit. `useAuth`
reads a React context (`AuthProvider` in `main.tsx`) so the pages and the `Navbar` share one signed-in user; on load
it calls `GET /api/auth/me` to restore the session after a reload.

## D18 — Registration starts a session (Accepted, step B6)
After a successful registration the user is already logged in, through the same `AuthenticationManager` as login,
so there is only one way to create a session. Cost: BCrypt runs twice on registration (~100 ms).

## D19 — New session id on login (Accepted, step B6)
The login is done in `AuthController`, not by `formLogin`, so the session fixation protection is applied by hand
with `request.changeSessionId()` before saving the `SecurityContext` in the session.

## D20 — Hexagonal architecture (ports and adapters) in the backend (Proposed, confirm with the team)
The business rules (`domain/`, `application/`) no longer depend on JPA, Spring Security or HTTP: the domain `User`
is a plain object, and the application talks to the outside only through ports (`UserRepository`,
`PasswordHasher`). Benefits: the move to PostgreSQL (NFR-01) or another store only touches
`infrastructure/persistence`; services are unit-tested with mocked ports and no Spring context; each new epic has an
obvious place for each class. Done early (only Epic 1 existed) because the cost grows with every feature.
Trade-offs accepted to keep it simple: `@Service` stays on the application services instead of wiring them in a
`@Configuration` class; ports live in `application/port` (not in `domain`); a JPA entity + mapper duplicates the
`User` fields. `UserNotFoundException` replaces Spring's `UsernameNotFoundException` in the application;
`AppUserDetailsService` translates it, and `ApiExceptionHandler` answers 401 for it, so the API behaves as before.

## D21 — Store uploaded MP4 bytes in PostgreSQL (Accepted for the video CRUD phase)
The video upload requirement explicitly asks for the MP4 itself to be persisted in the database, so metadata lives in
`video` and the binary payload in a related `video_content` row. The payload uses a `VARBINARY` mapping (PostgreSQL
`bytea`) without JPA `@Lob`; separating it ensures catalog/detail queries do not read large files. Playback reads the
bytes only when requested. This is straightforward and matches the MVP requirement, but increases database size and
backup cost; an object store remains a possible later migration. Video metadata is paginated with a descending-ID
cursor to avoid offset shifts when new uploads arrive. The initial request returns 20 items, and later requests
default to 10.

## D22 — Google OAuth2 alongside email/password authentication (Accepted)
Keep the existing local registration/login for backward compatibility and add Google sign-in using Spring Security's
OAuth2 client. On first Google sign-in, require a verified email, link to an existing local account only when the
verified email matches, and persist the stable Google subject for future logins. New OAuth-only accounts receive a
unique slug from the Google display name and have no password hash; local password authentication rejects those
accounts with the same generic 401 as other bad credentials. The OAuth principal name is the local normalized email,
so existing profile and video ownership use cases work for either authentication method. Client credentials stay in
`ENV_PROTUBE_GOOGLE_CLIENT_ID` and `ENV_PROTUBE_GOOGLE_CLIENT_SECRET`.
