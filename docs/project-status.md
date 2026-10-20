# Project status

Update this file when a step is finished. Last update: 2026-10-20.

## Epic 1 — Authentication (US-01, US-02) · branch `feature/auth` · in progress

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
| F2   | `useAuth` hook                                                             | R1, L1–L3  | next             |
| F3   | `RegisterPage` with client-side validation                                 | R1–R3      | pending          |
| F4   | `LoginPage`                                                                | L1, L2     | pending          |
| F5   | `Navbar` signed-in state + sign out                                        | L1, L3     | pending          |
| F6   | Frontend tests                                                             | DoD        | pending          |
| C1   | Manual end-to-end check of R1–R3 and L1–L3                                 | all        | pending          |
| C2   | Prompt approved, PR reviewed, squash merge                                 | DoD        | pending          |

Known follow-ups:
* Commits so far on the branch don't follow Conventional Commits; the PR title must
  (`feat(auth): user registration and login (US-01, US-02)`), since it becomes the squash commit.
* `prod` profile has no `ddl-auto` / migrations yet (NFR-01).
* In prod (SPA inside the jar) a reload on `/login` or `/register` probably returns 404, because the backend
  doesn't forward those routes to `index.html`. Not verified. Fix in a deployment PR.

## Other epics

Not started: profile (US-03), video catalog and playback (US-04, US-05; `VideoService` still returns a placeholder
list), upload and metadata (US-06, US-07), search (US-08; the navbar filters loaded videos client-side), comments
(US-09).
