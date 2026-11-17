package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-06 and US-07 over HTTP, with the real security filter chain, database and a temporary video store. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VideoUploadApiTest {

    private static final byte[] MP4 = "\0\0\0\u0018ftypisom-rest-of-the-file".getBytes(StandardCharsets.ISO_8859_1);

    @TempDir
    static Path storeDir;

    @DynamicPropertySource
    static void store(DynamicPropertyRegistry registry) {
        registry.add("pro_tube.store.dir", storeDir::toString);
        registry.add("pro_tube.load_initial_data", () -> "false");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private MockHttpSession register(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + name + "@mail.com\",\"username\":\"" + name
                                + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private JsonNode upload(MockHttpSession session) throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/videos")
                        .file(new MockMultipartFile("file", "clip.mp4", "video/mp4", MP4))
                        .param("title", "My first upload")
                        .param("description", "Recorded at home")
                        .param("tags", "Music,Live")
                        .param("category", "Music")
                        .session(session))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/videos/")))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void uploadedVideoIsStoredAndShownInTheCatalogAndTheOwnersList() throws Exception {
        MockHttpSession ana = register("ana");

        JsonNode video = upload(ana);
        long id = video.get("id").asLong();
        String fileKey = video.get("fileKey").asText();

        assertTrue(Files.exists(storeDir.resolve(fileKey + ".mp4")));
        mockMvc.perform(get("/api/videos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My first upload"))
                .andExpect(jsonPath("$.author").value("ana"))
                .andExpect(jsonPath("$.tags[0]").value("music"))
                .andExpect(jsonPath("$.tags[1]").value("live"))
                .andExpect(jsonPath("$.category").value("Music"));
        // The catalog is ordered by id (US-04), so the newest upload is the last item.
        JsonNode firstPage = objectMapper.readTree(mockMvc.perform(get("/api/videos").param("size", "1"))
                .andReturn().getResponse().getContentAsString());
        long lastPage = firstPage.get("totalItems").asLong() - 1;
        mockMvc.perform(get("/api/videos").param("page", String.valueOf(lastPage)).param("size", "1"))
                .andExpect(jsonPath("$.items[0].id").value(id))
                .andExpect(jsonPath("$.items[0].title").value("My first upload"));
        mockMvc.perform(get("/api/videos/mine").session(ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(id))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void visitorsCannotUploadOrListTheirVideos() throws Exception {
        mockMvc.perform(multipart("/api/videos")
                        .file(new MockMultipartFile("file", "clip.mp4", "video/mp4", MP4))
                        .param("title", "Title"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/videos/mine")).andExpect(status().isUnauthorized());
    }

    @Test
    void invalidUploadsAreRejectedWithAClearMessage() throws Exception {
        MockHttpSession ana = register("ana");

        mockMvc.perform(multipart("/api/videos")
                        .file(new MockMultipartFile("file", "clip.avi", "video/avi", MP4))
                        .param("title", "Title")
                        .session(ana))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only .mp4 files are supported"));
        mockMvc.perform(multipart("/api/videos")
                        .file(new MockMultipartFile("file", "clip.mp4", "video/mp4", "not a video".getBytes()))
                        .param("title", "Title")
                        .session(ana))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The file is not a valid MP4 video"));
        mockMvc.perform(multipart("/api/videos").param("title", "Title").session(ana))
                .andExpect(status().isBadRequest());

        // Nothing half-uploaded is published.
        mockMvc.perform(get("/api/videos/mine").session(ana)).andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void ownerEditsTheMetadataAndOthersAreDenied() throws Exception {
        MockHttpSession ana = register("ana");
        MockHttpSession bob = register("bob");
        long id = upload(ana).get("id").asLong();
        String body = "{\"title\":\"Edited\",\"description\":\"New text\",\"tags\":[\"Pop\"],\"category\":\"Gaming\"}";

        mockMvc.perform(put("/api/videos/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body)
                        .session(bob))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/videos/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/videos/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body)
                        .session(ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Edited"));

        mockMvc.perform(get("/api/videos/{id}", id))
                .andExpect(jsonPath("$.title").value("Edited"))
                .andExpect(jsonPath("$.description").value("New text"))
                .andExpect(jsonPath("$.tags[0]").value("pop"))
                .andExpect(jsonPath("$.category").value("Gaming"));
    }

    @Test
    void ownerDeletesTheVideoAndItsFile() throws Exception {
        MockHttpSession ana = register("ana");
        MockHttpSession bob = register("bob");
        JsonNode video = upload(ana);
        long id = video.get("id").asLong();
        Path file = storeDir.resolve(video.get("fileKey").asText() + ".mp4");

        mockMvc.perform(delete("/api/videos/{id}", id).session(bob)).andExpect(status().isForbidden());
        assertTrue(Files.exists(file));

        mockMvc.perform(delete("/api/videos/{id}", id).session(ana)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/videos/{id}", id)).andExpect(status().isNotFound());
        assertFalse(Files.exists(file));
    }
}
