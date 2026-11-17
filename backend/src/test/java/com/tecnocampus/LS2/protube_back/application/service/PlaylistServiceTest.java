package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.FindUserUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.PlaylistRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.InvalidPlaylistException;
import com.tecnocampus.LS2.protube_back.domain.exception.PlaylistNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.Playlist;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {
    private static final String EMAIL = "owner@example.com";
    private static final User OWNER = new User(1L, EMAIL, "owner", "hash");
    @Mock PlaylistRepository playlists;
    @Mock FindUserUseCase users;
    @InjectMocks PlaylistService service;

    @Test
    void listsAndCreatesListsForTheAuthenticatedUser() {
        when(users.getByEmail(EMAIL)).thenReturn(OWNER);
        when(playlists.findByOwnerId(1L)).thenReturn(List.of());
        when(playlists.create(1L, "Road trip")).thenReturn(playlist(3L, 1L, "Road trip", List.of()));

        assertEquals(List.of(), service.list(EMAIL));
        assertEquals("Road trip", service.create(EMAIL, " Road trip ").name());
    }

    @Test
    void validatesNamesAndLimitsRenameToOwner() {
        when(users.getByEmail(EMAIL)).thenReturn(OWNER);
        assertThrows(InvalidPlaylistException.class, () -> service.create(EMAIL, "  "));
        Playlist otherList = playlist(4L, 2L, "Other", List.of());
        when(playlists.findById(4L)).thenReturn(Optional.of(otherList));

        assertThrows(PlaylistNotFoundException.class, () -> service.rename(EMAIL, 4L, "Renamed"));
        verify(playlists, never()).updateName(4L, "Renamed");
    }

    @Test
    void addsAndRemovesExistingVideosOnlyFromOwnedLists() {
        Playlist own = playlist(4L, 1L, "Own", List.of());
        when(users.getByEmail(EMAIL)).thenReturn(OWNER);
        when(playlists.findById(4L)).thenReturn(Optional.of(own));
        when(playlists.addVideo(4L, 8L)).thenReturn(playlist(4L, 1L, "Own", List.of(8L)));
        when(playlists.removeVideo(4L, 8L)).thenReturn(own);

        assertEquals(List.of(8L), service.addVideo(EMAIL, 4L, 8L).videoIds());
        assertEquals(List.of(), service.removeVideo(EMAIL, 4L, 8L).videoIds());
        when(playlists.addVideo(4L, 9L))
                .thenThrow(new VideoNotFoundException(9L));
        assertThrows(VideoNotFoundException.class, () -> service.addVideo(EMAIL, 4L, 9L));
    }

    @Test
    void deletesOnlyOwnedListsAndReturnsNotFoundForUnknownLists() {
        when(users.getByEmail(EMAIL)).thenReturn(OWNER);
        when(playlists.findById(5L)).thenReturn(Optional.of(playlist(5L, 1L, "Own", List.of())));
        service.delete(EMAIL, 5L);
        verify(playlists).delete(5L);

        when(playlists.findById(6L)).thenReturn(Optional.empty());
        assertThrows(PlaylistNotFoundException.class, () -> service.delete(EMAIL, 6L));
    }

    private static Playlist playlist(Long id, Long ownerId, String name, List<Long> videoIds) {
        return new Playlist(id, ownerId, name, videoIds);
    }
}
