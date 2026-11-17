package com.tecnocampus.LS2.protube_back.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest({
        "pro_tube.store.dir=c:",
        "pro_tube.load_initial_data=false"
})
@AutoConfigureMockMvc
@Transactional
class VideosApiIntegrationTest {
    private static final byte[] MP4 = new byte[]{0, 0, 0, 16, 'f', 't', 'y', 'p', 1};

    @Autowired
    MockMvc mockMvc;

    @Test
    void uploadPersistsMetadataAndMp4ForPublicBrowseAndPlayback() throws Exception {
        MockHttpSession session = register("video-owner@example.com", "video-owner");
        long videoId = upload(session, "Saved video");

        mockMvc.perform(get("/api/videos").param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(videoId))
                .andExpect(jsonPath("$.items[0].title").value("Saved video"))
                .andExpect(jsonPath("$.items[0].content").doesNotExist());
        mockMvc.perform(get("/api/videos/" + videoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerUsername").value("video-owner"));
        mockMvc.perform(get("/api/videos/mine").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(videoId));
        mockMvc.perform(get("/api/videos/mine"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/videos/" + videoId + "/file"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("video/mp4"))
                .andExpect(content().bytes(MP4));
    }

    @Test
    void onlyOwnerCanEditOrDeleteVideo() throws Exception {
        MockHttpSession ownerSession = register("first-owner@example.com", "first-owner");
        MockHttpSession otherSession = register("other-owner@example.com", "other-owner");
        long videoId = upload(ownerSession, "Original");

        mockMvc.perform(put("/api/videos/" + videoId).session(otherSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"stolen\",\"description\":\"\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/videos/" + videoId).session(otherSession))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/videos/mine").session(otherSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());

        mockMvc.perform(put("/api/videos/" + videoId).session(ownerSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\",\"description\":\"New description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
        mockMvc.perform(delete("/api/videos/" + videoId).session(ownerSession))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/videos/" + videoId)).andExpect(status().isNotFound());
    }

    private MockHttpSession register(String email, String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"username\":\"" + username
                                + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        return session;
    }

    private long upload(MockHttpSession session, String title) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "clip.mp4", "video/mp4", MP4);
        MvcResult result = mockMvc.perform(multipart("/api/videos").file(file).session(session)
                        .param("title", title).param("description", "A database-backed video"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title))
                .andReturn();
        String location = result.getResponse().getHeader("Location");
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }
}
