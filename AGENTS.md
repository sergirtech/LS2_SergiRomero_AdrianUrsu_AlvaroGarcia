# AGENTS.md

ProTube is a university group project (LS2, Tecnocampus): a web app where registered users upload, watch and comment
on videos. Assignment rules are in `README.md`; the MVP spec (epics, user stories, NFRs, acceptance criteria) is in
`REQUIREMENT.md` — check it before implementing a feature.

More specific rules live in [`backend/AGENTS.md`](backend/AGENTS.md) and [`frontend/AGENTS.md`](frontend/AGENTS.md).

## Repository layout

* `backend/` — Spring Boot 3.3 REST API (Java 21, Maven).
* `frontend/` — React 19 + TypeScript SPA (Vite, Jest).
* `tooling/videoGrabber/` — Python script (`yt-dlp` + `ffmpeg`) that writes `.mp4`/`.webp`/`.json` triples into the
  video store folder the backend serves.
* `prompts/` — approved AI prompts (see "AI usage" below).
* `docs/` — `architecture.md` (how the pieces fit), `decisions.md` (design decisions and why),
  `project-status.md` (what is done / in progress per user story). Read `project-status.md` before continuing work.

## Commands (Windows / PowerShell)

| What                     | Where       | Command                                   |
|--------------------------|-------------|-------------------------------------------|
| Backend build + tests    | `backend/`  | `.\mvnw.cmd clean verify`                 |
| Backend single test      | `backend/`  | `.\mvnw.cmd test "-Dtest=ClassName"`      |
| Run backend              | `backend/`  | `.\mvnw.cmd spring-boot:run`              |
| Frontend install         | `frontend/` | `npm ci`                                  |
| Frontend dev server      | `frontend/` | `npm run dev`                             |
| Frontend build           | `frontend/` | `npm run build`                           |
| Frontend tests           | `frontend/` | `npm run test`                            |
| Frontend lint            | `frontend/` | `npm run lint`                            |

The Maven wrapper is not committed yet; CI generates it with `mvn -N io.takari:maven:wrapper`. If `mvnw.cmd` is
missing locally, generate it the same way or use a local `mvn`.

CI (`.github/workflows/main.yml`) runs on PRs and pushes to `main`: frontend `npm ci` → build → tests → lint, then
backend `./mvnw clean verify -Pcoverage` (the profile is a no-op, see "Mistakes to avoid").

## Architecture

* Backend layering: `controller/` (REST under `/api/...`) → `services/` (business logic) → `repository/` (Spring
  Data JPA) → JPA entities in `model/`. Controllers never talk to repositories directly. H2 is the dev datastore and
  only a placeholder per NFR-01.
* `configuration/MvcConfig` serves the video store under `/media/**`. The store path comes from
  `pro_tube.store.dir` (env var `ENV_PROTUBE_STORE_DIR`); never hardcode it.
* Frontend calls the backend through `src/utils/Env.ts` (`${VITE_API_DOMAIN}/api`, `${VITE_MEDIA_DOMAIN}/media`).
* Authentication (Epic 1, **in progress** on `feature/auth`): email + password, BCrypt hashes, server session
  (`JSESSIONID` cookie) with Spring Security. See `docs/decisions.md` for the reasons and `docs/project-status.md`
  for which steps are done.

## Conventions

* Branch from `main` as `feature/<short-description>` (e.g. `feature/video-comments`). Never commit or push directly
  to `main`.
* Commits follow [Conventional Commits](https://www.conventionalcommits.org/) (`type(scope): description`) and
  reference the task.
* Every change goes through a Pull Request: at least 1 approval from another team member, CI green, **squash merge**.
* AI-assisted commits must say so in the message and reference the prompt used from `prompts/`.

## Definition of Done

* Acceptance criteria of the user story in `REQUIREMENT.md` are met.
* New/changed code has tests; backend JaCoCo thresholds pass (≥50% lines per class, ≥75% instructions/lines/methods
  overall).
* `.\mvnw.cmd clean verify` and frontend `npm run build`, `npm run test`, `npm run lint` all pass locally and in CI.
* PR reviewed and approved by at least one teammate.
* If AI was used, the prompt is committed in `prompts/` and referenced in the commit.

## Mistakes to avoid

* Don't commit secrets: OAuth client id/secret and DB credentials are env vars set locally, never in committed files.
* Don't commit generated videos or downloads (e.g. stray `.webm`/`.mp4` files from `tooling/videoGrabber/`).
* Don't add `-Pcoverage` expecting it to do something: that profile does not exist; JaCoCo always runs in `verify`.
* Don't treat H2 as the final datastore.
* Don't run ad hoc prompts for a task type already covered in `prompts/`; reuse the approved one.
* Don't store, log or return plain-text passwords, and don't return JPA entities from controllers: use DTOs.
* Don't leave `docs/project-status.md` stale: update it when a user story step is finished.
