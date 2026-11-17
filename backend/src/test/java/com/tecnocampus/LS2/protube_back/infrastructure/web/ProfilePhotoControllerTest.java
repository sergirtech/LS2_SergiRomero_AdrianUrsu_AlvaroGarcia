package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.ManageProfilePhotoUseCase;
import com.tecnocampus.LS2.protube_back.domain.model.ProfilePhoto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ProfilePhotoController.class, properties = "pro_tube.store.dir=c:")
@AutoConfigureMockMvc(addFilters = false)
class ProfilePhotoControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean ManageProfilePhotoUseCase photos;

    @Test
    void returnsNotFoundForDefaultAndServesStoredImageSafely() throws Exception {
        when(photos.get("ana@example.com")).thenReturn(Optional.empty())
                .thenReturn(Optional.of(new ProfilePhoto("image/png", new byte[]{1, 2, 3})));

        mockMvc.perform(get("/api/users/me/photo").principal(authentication()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/users/me/photo").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));
    }

    @Test
    void uploadsAndDeletesAuthenticatedUsersPhoto() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/users/me/photo").file(file).principal(authentication())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/users/me/photo").principal(authentication()))
                .andExpect(status().isNoContent());
        verify(photos).update("ana@example.com", "avatar.png", "image/png", new byte[]{1, 2, 3});
        verify(photos).delete("ana@example.com");
    }

    private static UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("ana@example.com", "", List.of());
    }
}
