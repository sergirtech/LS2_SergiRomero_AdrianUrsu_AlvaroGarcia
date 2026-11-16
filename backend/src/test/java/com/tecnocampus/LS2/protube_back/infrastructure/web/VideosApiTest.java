package com.tecnocampus.LS2.protube_back.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP behaviour of the catalog endpoints, with the real security filter chain and exception handler. */
@SpringBootTest
@AutoConfigureMockMvc
class VideosApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void catalogIsPublicAndPaginated() throws Exception {
        mockMvc.perform(get("/api/videos").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.items.length()").value(lessThanOrEqualTo(2)));
    }

    @Test
    void unknownVideoIsNotFound() throws Exception {
        mockMvc.perform(get("/api/videos/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Video 999999 not found"));
    }
}
