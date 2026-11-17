package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.ManagePlaylistsUseCase;
import com.tecnocampus.LS2.protube_back.domain.model.Playlist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = PlaylistsController.class, properties = "pro_tube.store.dir=c:")
@AutoConfigureMockMvc(addFilters = false)
class PlaylistsControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean ManagePlaylistsUseCase playlists;

    @Test
    void listsAndCreatesPlaylistForAuthenticatedUser() throws Exception {
        when(playlists.list("ana@example.com")).thenReturn(List.of(playlist()));
        when(playlists.create("ana@example.com", "Road trip")).thenReturn(playlist());

        mockMvc.perform(get("/api/playlists").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Road trip"));
        mockMvc.perform(post("/api/playlists").principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Road trip\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void renamesAndManagesVideosAndDeletesPlaylist() throws Exception {
        when(playlists.rename("ana@example.com", 7L, "New name")).thenReturn(playlist());
        when(playlists.addVideo("ana@example.com", 7L, 3L)).thenReturn(playlist());
        when(playlists.removeVideo("ana@example.com", 7L, 3L)).thenReturn(playlist());

        mockMvc.perform(put("/api/playlists/7").principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"New name\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/playlists/7/videos/3").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videoIds[0]").value(3));
        mockMvc.perform(delete("/api/playlists/7/videos/3").principal(authentication()))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/playlists/7").principal(authentication()))
                .andExpect(status().isNoContent());
        verify(playlists).delete("ana@example.com", 7L);
    }

    @Test
    void rejectsBlankPlaylistNames() throws Exception {
        mockMvc.perform(post("/api/playlists").principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    private static Playlist playlist() {
        return new Playlist(7L, 1L, "Road trip", List.of(3L));
    }

    private static UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("ana@example.com", "", List.of());
    }
}
