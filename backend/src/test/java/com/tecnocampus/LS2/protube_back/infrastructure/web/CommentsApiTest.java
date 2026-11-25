package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.tecnocampus.LS2.protube_back.domain.model.VideoFixtures.video;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-09 over HTTP, with the real security filter chain and database. */
@SpringBootTest({
        "pro_tube.store.dir=c:",
        "pro_tube.load_initial_data=false"
})
@AutoConfigureMockMvc
@Transactional
class CommentsApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    VideoRepository videoRepository;

    Long videoId;

    @BeforeEach
    void saveVideo() {
        videoId = videoRepository.save(video(null, "comments-api-test")).id();
    }

    private MockHttpSession register() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"carla@mail.com\",\"username\":\"carla\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private MockHttpServletRequestBuilder comment(Long id, String text) throws Exception {
        return post("/api/videos/{id}/comments", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("text", text)));
    }

    @Test
    void aSignedInUserCommentsAndEveryoneSeesItNewestFirst() throws Exception {
        MockHttpSession carla = register();

        mockMvc.perform(comment(videoId, "  First  ").session(carla))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.authorId").isNumber())
                .andExpect(jsonPath("$.authorName").value("carla"))
                .andExpect(jsonPath("$.text").value("First"))
                .andExpect(jsonPath("$.createdAt").isString());
        mockMvc.perform(comment(videoId, "Second").session(carla)).andExpect(status().isCreated());

        // No session: reading is public.
        mockMvc.perform(get("/api/videos/{id}/comments", videoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("Second"))
                .andExpect(jsonPath("$[1].text").value("First"));
    }

    @Test
    void visitorsCannotComment() throws Exception {
        mockMvc.perform(comment(videoId, "Hi")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/videos/{id}/comments", videoId)).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void emptyOrTooLongCommentsAreRejected() throws Exception {
        MockHttpSession carla = register();

        mockMvc.perform(comment(videoId, "   ").session(carla))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Comment must have between 1 and 1000 characters"));
        mockMvc.perform(comment(videoId, "a".repeat(1001)).session(carla)).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/videos/{id}/comments", videoId).contentType(MediaType.APPLICATION_JSON)
                .content("{}").session(carla)).andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/videos/{id}/comments", videoId)).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void commentsOfAnUnknownVideoAreNotFound() throws Exception {
        mockMvc.perform(get("/api/videos/{id}/comments", 999_999)).andExpect(status().isNotFound());
        mockMvc.perform(comment(999_999L, "Hi").session(register())).andExpect(status().isNotFound());
    }
}
