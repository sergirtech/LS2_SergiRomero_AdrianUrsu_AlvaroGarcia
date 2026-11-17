# Persistent comments and theme preference

**Approved on:** 2026-11-16
**Approved by:** Requester (session approval)
**Used for:** Adding persistent video comments, comment engagement, profile comment management, and saved theme choice

## Prompt

Follow `AGENTS.md`, `backend/AGENTS.md`, `frontend/AGENTS.md`, and the applicable instruction files. Implement the
approved comments and theme scope while preserving the hexagonal backend boundaries:

* Persist comments in the configured database. Allow public comment reads; require a signed-in user for creating,
  replying, liking/unliking, and restrict editing/deletion to the comment author.
* Include the video and author details, replies, like count, and the current user's like state in comment responses.
  Add API-backed comment controls on watch pages and edit/delete controls for the signed-in user's comments in their
  profile. Surface loading, empty, validation, and server/network error states.
* Persist the light/dark theme selection in localStorage for both signed-in and anonymous visitors.
* Add backend persistence/service/controller tests and frontend component/hook/integration tests; preserve ownership
  boundaries and update project status and architecture documentation.
