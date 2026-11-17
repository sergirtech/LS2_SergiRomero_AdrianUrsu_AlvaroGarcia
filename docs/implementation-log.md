# ProTube implementation log

This log records the requested feature work across the video/profile, comments/theme, playlist/photo slices and the
validation performed. The staged `docs/Development_Guide.pdf` and existing edits to instruction files were not part
of this implementation and were left untouched.

## Steps and outcomes

1. **Reviewed scope and project constraints.** Read the project requirements, architecture and status notes, and the
   backend/frontend contribution and test rules. Kept the backend hexagonal boundaries, used the existing session
   identity for ownership, and selected PostgreSQL-compatible JPA storage for persistent data. The agreed catalog
   paging behavior is 20 records initially and 10 per continuation; theme preference remains in `localStorage`.
2. **Added persistent video upload and catalog APIs.** Added video metadata/content persistence, authenticated
   upload/edit/delete and owner listing, public detail/file access, and cursor paging. The frontend now loads and
   plays API videos, requests further pages as the home grid scrolls, and provides profile video management.
3. **Fixed optional Google OAuth startup.** Google OAuth configuration is active only when both environment
   credentials are present, allowing local email/password startup without OAuth secrets.
4. **Added comment and theme persistence.** Comments, replies, likes, and author-only edits/deletions use the
   backend API and persistent tables; profile comment management uses that API. Anonymous and signed-in theme
   choices persist across reloads in browser storage.
5. **Added database-backed playlists and profile photos.** Playlists support list/create/rename/delete and video
   membership changes, with owner checks and link cleanup when a video is deleted. The profile now uses the
   authenticated photo API, supports upload/replacement/removal, and falls back to the default avatar. PNG/JPG/JPEG
   signatures and matching MIME/extension are checked; SVG is parsed with external entities disabled and dangerous
   elements/attributes removed before persistence. Uploads are limited to 5 MB.
6. **Updated project records.** Updated architecture and project status and added the approved-scope prompt record
   for playlists and profile photos.
7. **Validated the result.** Backend `.\mvnw.cmd clean verify` passed (107 tests; JaCoCo thresholds met). Frontend
   `npm run build` passed and `npm run test -- --runInBand` passed (109 tests). ESLint passed on the changed
   frontend files. Repository-wide `npm run lint` still reports the documented, unrelated CRLF/Prettier errors in
   existing files. `git diff --check` passed.

## Files created for the requested features

**Backend ports, domain, services, persistence and web**

- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/DeleteVideoUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/EditVideoUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/GetVideoFileUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/GetVideoUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/ListMyVideosUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/ManageCommentsUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/ManagePlaylistsUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/ManageProfilePhotoUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/ProvisionGoogleUserUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/UploadVideoUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/out/CommentRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/out/PlaylistRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/out/ProfilePhotoRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/out/VideoRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/service/CommentService.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/service/GoogleAccountService.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/service/PlaylistService.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/service/ProfilePhotoService.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/CommentNotFoundException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/InvalidCommentException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/InvalidPlaylistException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/InvalidProfilePhotoException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/InvalidVideoException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/PlaylistNotFoundException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/VideoNotFoundException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/Playlist.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/ProfilePhoto.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/Video.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/VideoComment.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/VideoFile.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/VideoPage.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/VideoSummary.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/config/GoogleOAuth2ClientConfiguration.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/CommentJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/CommentJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/CommentLikeJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/CommentLikeJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/CommentPersistenceAdapter.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/PlaylistJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/PlaylistJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/PlaylistPersistenceAdapter.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/ProfilePhotoPersistenceAdapter.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/UserProfilePhotoJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/UserProfilePhotoJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/VideoContentJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/VideoContentJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/VideoJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/VideoJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/VideoPersistenceAdapter.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/security/GoogleOAuth2UserService.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/CommentsController.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/PlaylistsController.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/ProfilePhotoController.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/CommentRequest.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/CommentResponse.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/PlaylistNameRequest.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/PlaylistResponse.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/VideoPageResponse.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/VideoResponse.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/VideoUpdateRequest.java`

**Frontend**

- `frontend/src/__tests__/useAllVideos.test.tsx`
- `frontend/src/__tests__/useMyComments.test.tsx`
- `frontend/src/__tests__/useMyVideos.test.tsx`
- `frontend/src/__tests__/useProfilePhoto.test.tsx`
- `frontend/src/__tests__/videoApi.test.ts`
- `frontend/src/components/ProfileCommentsManager.tsx`
- `frontend/src/components/ProfilePhotoManager.tsx`
- `frontend/src/components/ProfileVideoManager.tsx`
- `frontend/src/components/__tests__/ProfileCommentsManager.test.tsx`
- `frontend/src/components/__tests__/ProfilePhotoManager.test.tsx`
- `frontend/src/components/__tests__/ProfileVideoManager.test.tsx`
- `frontend/src/useMyComments.ts`
- `frontend/src/useMyVideos.ts`
- `frontend/src/useProfilePhoto.ts`
- `frontend/src/videoApi.ts`

**Tests, documentation and prompts**

- `backend/src/test/java/com/tecnocampus/LS2/protube_back/application/service/CommentServiceTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/application/service/GoogleAccountServiceTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/application/service/PlaylistServiceTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/application/service/ProfilePhotoServiceTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/CommentPersistenceAdapterTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/PlaylistPersistenceAdapterTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/ProfilePhotoPersistenceAdapterTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/VideoPersistenceAdapterTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/security/GoogleOAuth2UserServiceTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/web/CommentsControllerTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/web/PlaylistsControllerTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/web/ProfilePhotoControllerTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/web/VideosApiIntegrationTest.java`
- `backend/src/test/resources/application.properties`
- `prompts/04-video-persistence-crud.md`
- `prompts/05-google-oauth-login.md`
- `prompts/06-frontend-video-catalog-playback.md`
- `prompts/07-profile-video-management.md`
- `prompts/08-persistent-comments-and-theme.md`
- `prompts/09-playlists-and-profile-photos.md`
- `docs/implementation-log.md`

## Existing files updated for these features

- Backend: `backend/pom.xml`; `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/ListVideosUseCase.java`;
  `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/out/UserRepository.java`;
  `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/service/VideoService.java`;
  `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/User.java`;
  `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/config/SecurityConfig.java`;
  `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/UserJpaEntity.java`;
  `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/UserJpaRepository.java`;
  `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/UserPersistenceAdapter.java`;
  `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/security/AppUserDetailsService.java`;
  `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/ApiExceptionHandler.java`;
  `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/VideosController.java`;
  `backend/src/main/resources/application.properties`.
- Backend regression tests: `backend/src/test/java/com/tecnocampus/LS2/protube_back/ProtubeBackApplicationTests.java`;
  `backend/src/test/java/com/tecnocampus/LS2/protube_back/application/service/VideoServiceTest.java`;
  `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/config/SecurityConfigTest.java`;
  `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/UserPersistenceAdapterTest.java`;
  `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/security/AppUserDetailsServiceTest.java`;
  `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/web/ApiExceptionHandlerTest.java`;
  `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/web/VideosControllerTest.java`.
- Frontend: `frontend/jest.setup.ts`; `frontend/src/App.css`; `frontend/src/App.tsx`;
  `frontend/src/__tests__/AppAuth.test.tsx`; `frontend/src/components/LoginPage.tsx`;
  `frontend/src/components/ProfileListsPage.tsx`; `frontend/src/components/ProfilePage.tsx`;
  `frontend/src/components/RegisterPage.tsx`; `frontend/src/components/VideoCard.tsx`;
  `frontend/src/components/VideoGrid.tsx`; `frontend/src/components/WatchPage.tsx`;
  `frontend/src/components/__tests__/LoginPage.test.tsx`;
  `frontend/src/components/__tests__/ProfileListsPage.test.tsx`;
  `frontend/src/components/__tests__/ProfilePage.test.tsx`;
  `frontend/src/components/__tests__/RegisterPage.test.tsx`;
  `frontend/src/components/__tests__/VideoGrid.test.tsx`;
  `frontend/src/components/__tests__/WatchPage.test.tsx`; `frontend/src/useAllVideos.ts`;
  `frontend/src/useProfileLists.ts`; `frontend/src/utils/Env.ts`.
- Project records and deployment: `DEVELOPMENT.md`; `compose.yaml`; `docs/architecture.md`;
  `docs/decisions.md`; `docs/project-status.md`.
