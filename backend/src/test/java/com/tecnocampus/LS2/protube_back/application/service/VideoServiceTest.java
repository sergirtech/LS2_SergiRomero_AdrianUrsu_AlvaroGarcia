package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.UploadVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.UploadVideoUseCase.UploadVideoCommand;
import com.tecnocampus.LS2.protube_back.application.port.out.UserRepository;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoFileStore;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoMetadataSource;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.InvalidVideoException;
import com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoAccessDeniedException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoStorageException;
import com.tecnocampus.LS2.protube_back.domain.model.PageResult;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static com.tecnocampus.LS2.protube_back.domain.model.VideoFixtures.ownedVideo;
import static com.tecnocampus.LS2.protube_back.domain.model.VideoFixtures.video;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    /** Smallest content the service accepts as MP4: 4 size bytes, then "ftyp". */
    private static final byte[] MP4 = "\0\0\0\u0018ftypisom-rest-of-the-file".getBytes(StandardCharsets.ISO_8859_1);
    private static final User ANA = new User(1L, "ana@mail.com", "ana", "hash");
    private static final User BOB = new User(2L, "bob@mail.com", "bob", "hash");

    @Mock
    VideoRepository videoRepository;

    @Mock
    VideoMetadataSource metadataSource;

    @Mock
    VideoFileStore fileStore;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    VideoService videoService;

    private static UploadVideoCommand upload(String fileName, byte[] content) {
        return new UploadVideoCommand("  My video ", " About it ", List.of("Music", "music", " live "), "Music",
                fileName, content.length, new ByteArrayInputStream(content));
    }

    private void signedIn(User user) {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void listVideosClampsNegativePageAndTooBigSize() {
        PageResult<Video> empty = new PageResult<>(List.of(), 0, ListVideosUseCase.MAX_PAGE_SIZE, 0);
        when(videoRepository.findPage(0, ListVideosUseCase.MAX_PAGE_SIZE)).thenReturn(empty);

        assertSame(empty, videoService.listVideos(-3, 1000));
    }

    @Test
    void listVideosUsesAtLeastOneItemPerPage() {
        PageResult<Video> empty = new PageResult<>(List.of(), 2, 1, 0);
        when(videoRepository.findPage(2, 1)).thenReturn(empty);

        assertSame(empty, videoService.listVideos(2, 0));
    }

    @Test
    void getVideoReturnsStoredVideo() {
        Video video = video(7L, "7");
        when(videoRepository.findById(7L)).thenReturn(Optional.of(video));

        assertSame(video, videoService.getVideo(7L));
    }

    @Test
    void getVideoThrowsWhenMissing() {
        when(videoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(VideoNotFoundException.class, () -> videoService.getVideo(99L));
    }

    @Test
    void importSavesOnlyVideosNotStoredYet() {
        Video existing = video(null, "0");
        Video fresh = video(null, "1");
        when(metadataSource.readAll()).thenReturn(List.of(existing, fresh));
        when(videoRepository.existsByFileKey("0")).thenReturn(true);
        when(videoRepository.existsByFileKey("1")).thenReturn(false);

        assertEquals(1, videoService.importFromStore());
        verify(videoRepository).save(fresh);
        verify(videoRepository, never()).save(existing);
    }

    @Test
    void listMyVideosUsesTheOwnerIdAndClampsThePage() {
        signedIn(ANA);
        PageResult<Video> page = new PageResult<>(List.of(ownedVideo(5L, 1L)), 0, 1, 1);
        when(videoRepository.findPageByOwner(1L, 0, 1)).thenReturn(page);

        // The email is normalized like everywhere else (D4).
        assertSame(page, videoService.listMyVideos(" ANA@mail.com ", -1, 0));
    }

    @Test
    void uploadStoresTheFileFirstAndThenTheVideoOwnedByTheUser() {
        signedIn(ANA);
        byte[][] written = new byte[1][];
        doAnswer(invocation -> {
            written[0] = ((InputStream) invocation.getArgument(1)).readAllBytes();
            return null;
        }).when(fileStore).saveVideo(anyString(), any());
        when(videoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Video saved = videoService.upload("ana@mail.com", upload("clip.MP4", MP4));

        // The signature check must not eat the first bytes of the file.
        assertArrayEquals(MP4, written[0]);
        assertTrue(saved.fileKey().startsWith("upload-"));
        assertEquals("My video", saved.title());
        assertEquals("About it", saved.description());
        assertEquals(List.of("music", "live"), saved.tags());
        assertEquals("Music", saved.category());
        assertEquals("ana", saved.author());
        assertEquals(1L, saved.ownerId());
        verify(fileStore).saveVideo(eq(saved.fileKey()), any());
    }

    @Test
    void uploadRejectsFilesThatAreNotMp4BeforeStoringAnything() {
        assertThrows(InvalidVideoException.class,
                () -> videoService.upload("ana@mail.com", upload("clip.avi", MP4)));
        assertThrows(InvalidVideoException.class,
                () -> videoService.upload("ana@mail.com", upload(null, MP4)));
        verifyNoInteractions(fileStore, videoRepository);
    }

    @Test
    void uploadRejectsEmptyAndTooBigFiles() {
        assertThrows(InvalidVideoException.class,
                () -> videoService.upload("ana@mail.com", upload("clip.mp4", new byte[0])));

        UploadVideoCommand tooBig = new UploadVideoCommand("Title", "", List.of(), null, "clip.mp4",
                UploadVideoUseCase.MAX_FILE_SIZE_BYTES + 1, new ByteArrayInputStream(MP4));
        assertThrows(InvalidVideoException.class, () -> videoService.upload("ana@mail.com", tooBig));
        verifyNoInteractions(fileStore, videoRepository);
    }

    @Test
    void uploadRejectsContentWithoutTheMp4Signature() {
        signedIn(ANA);
        byte[] fake = "this is not a video at all".getBytes(StandardCharsets.US_ASCII);

        assertThrows(InvalidVideoException.class,
                () -> videoService.upload("ana@mail.com", upload("clip.mp4", fake)));
        assertThrows(InvalidVideoException.class,
                () -> videoService.upload("ana@mail.com", upload("clip.mp4", new byte[] {0, 0, 0})));
        verifyNoInteractions(fileStore, videoRepository);
    }

    @Test
    void uploadRejectsInvalidMetadata() {
        UploadVideoCommand noTitle = new UploadVideoCommand("  ", "", List.of(), null, "clip.mp4", MP4.length,
                new ByteArrayInputStream(MP4));

        assertThrows(InvalidVideoException.class, () -> videoService.upload("ana@mail.com", noTitle));
        verifyNoInteractions(fileStore, videoRepository);
    }

    @Test
    void uploadReportsAFileThatCannotBeRead() {
        signedIn(ANA);
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("connection reset");
            }
        };
        UploadVideoCommand command = new UploadVideoCommand("Title", "", List.of(), null, "clip.mp4", 10, broken);

        assertThrows(VideoStorageException.class, () -> videoService.upload("ana@mail.com", command));
        verifyNoInteractions(fileStore);
    }

    @Test
    void uploadRemovesTheFileWhenTheVideoCannotBeSaved() {
        signedIn(ANA);
        when(videoRepository.save(any())).thenThrow(new IllegalStateException("database down"));

        assertThrows(IllegalStateException.class,
                () -> videoService.upload("ana@mail.com", upload("clip.mp4", MP4)));

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(fileStore).saveVideo(key.capture(), any());
        verify(fileStore).deleteVideo(key.getValue());
    }

    @Test
    void uploadForAnUnknownUserFails() {
        when(userRepository.findByEmail("ghost@mail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> videoService.upload("ghost@mail.com", upload("clip.mp4", MP4)));
        verifyNoInteractions(fileStore);
    }

    @Test
    void ownerCanEditTheMetadata() {
        signedIn(ANA);
        when(videoRepository.findById(5L)).thenReturn(Optional.of(ownedVideo(5L, 1L)));
        when(videoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Video edited = videoService.edit("ana@mail.com", 5L, " New title ", "New text", List.of("Tag"), " ");

        assertEquals("New title", edited.title());
        assertEquals("New text", edited.description());
        assertEquals(List.of("tag"), edited.tags());
        assertEquals(null, edited.category());
        assertEquals("upload-5", edited.fileKey());
        assertEquals(1L, edited.ownerId());
    }

    @Test
    void editingSomeoneElsesVideoIsDenied() {
        signedIn(BOB);
        when(videoRepository.findById(5L)).thenReturn(Optional.of(ownedVideo(5L, 1L)));

        assertThrows(VideoAccessDeniedException.class,
                () -> videoService.edit("bob@mail.com", 5L, "Title", "", List.of(), null));
        verify(videoRepository, never()).save(any());
    }

    @Test
    void importedVideosHaveNoOwnerSoNobodyCanEditThem() {
        signedIn(ANA);
        when(videoRepository.findById(3L)).thenReturn(Optional.of(video(3L, "3")));

        assertThrows(VideoAccessDeniedException.class,
                () -> videoService.edit("ana@mail.com", 3L, "Title", "", List.of(), null));
    }

    @Test
    void editingAMissingVideoIsNotFound() {
        signedIn(ANA);
        when(videoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(VideoNotFoundException.class,
                () -> videoService.edit("ana@mail.com", 9L, "Title", "", List.of(), null));
    }

    @Test
    void ownerCanDeleteTheVideoAndItsFiles() {
        signedIn(ANA);
        when(videoRepository.findById(5L)).thenReturn(Optional.of(ownedVideo(5L, 1L)));

        videoService.delete("ana@mail.com", 5L);

        verify(videoRepository).deleteById(5L);
        verify(fileStore).deleteVideo("upload-5");
    }

    @Test
    void deletingSomeoneElsesVideoIsDenied() {
        signedIn(BOB);
        when(videoRepository.findById(5L)).thenReturn(Optional.of(ownedVideo(5L, 1L)));

        assertThrows(VideoAccessDeniedException.class, () -> videoService.delete("bob@mail.com", 5L));
        verify(videoRepository, never()).deleteById(any());
        verifyNoInteractions(fileStore);
    }
}
