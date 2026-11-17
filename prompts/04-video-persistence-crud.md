# Video persistence and CRUD

**Approved on:** 2026-11-12
**Approved by:** Requester (session approval)
**Used for:** Implementing the first backend video-persistence phase

## Prompt

Implement a backend video catalog and CRUD slice in ProTube using its documented hexagonal architecture. Store uploaded
MP4 bytes and metadata in PostgreSQL, require authentication for upload/edit/delete and the user's uploads endpoint, enforce ownership for changes,
and keep binary content out of catalog and metadata responses. Provide public video metadata and playback endpoints.
The catalog must return 20 records initially and support cursor-based continuation in batches of 10 for infinite
scrolling. Validate uploads and metadata, add tests for domain/service, persistence, HTTP and authorization behavior,
and update directly related project documentation. Keep PostgreSQL as the production database and do not expose JPA
entities or add framework dependencies to the domain.

## Notes

Dark-mode persistence was separately selected as browser `localStorage`; its frontend implementation is out of this
phase.
