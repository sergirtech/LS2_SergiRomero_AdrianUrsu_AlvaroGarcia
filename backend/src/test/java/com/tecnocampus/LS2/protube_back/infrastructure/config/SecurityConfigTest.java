package com.tecnocampus.LS2.protube_back.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest({
        "pro_tube.store.dir=c:",
        "pro_tube.load_initial_data=false",
        "pro_tube.google.client-id=test-google-client",
        "pro_tube.google.client-secret=test-google-secret"
})
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void videosArePublic() throws Exception {
        mockMvc.perform(get("/api/videos")).andExpect(status().isOk());
    }

    @Test
    void googleAuthorizationStartsWithRedirectToProvider() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("accounts.google.com")));
    }

    @Test
    void protectedApiWithoutSessionReturns401InsteadOfRedirect() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/videos/mine")).andExpect(status().isUnauthorized());
    }

    @Test
    void videoMutationEndpointsRequireAuthentication() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "clip.mp4", "video/mp4",
                new byte[]{0, 0, 0, 16, 'f', 't', 'y', 'p'});
        mockMvc.perform(multipart("/api/videos").file(file)
                        .param("title", "title").param("description", "description"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/videos/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/videos/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutReturns204() throws Exception {
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isNoContent());
    }
}
