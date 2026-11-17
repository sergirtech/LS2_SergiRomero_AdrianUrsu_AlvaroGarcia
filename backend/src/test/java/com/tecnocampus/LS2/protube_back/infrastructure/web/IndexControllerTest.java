package com.tecnocampus.LS2.protube_back.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class IndexControllerTest {
    IndexController indexController = new IndexController();
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(indexController).build();

    @Test
    void home() {

        assertEquals("index", indexController.home().getViewName());
    }

    @Test
    void logout() {

        assertEquals("logout", indexController.logout().getViewName());
    }

    // Reloading a SPA route in prod must return the SPA, not 404.
    @ParameterizedTest
    @ValueSource(strings = {"/", "/login", "/register", "/watch/abc123", "/profile", "/profile/lists", "/upload", "/my-videos"})
    void spaRoutesReturnTheIndex(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    // Static files and unknown API paths must not be swallowed by the SPA routes.
    @ParameterizedTest
    @ValueSource(strings = {"/api/unknown", "/assets/index-abc.js", "/media/video.mp4"})
    void otherPathsAreNotSpaRoutes(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isNotFound());
    }
}
