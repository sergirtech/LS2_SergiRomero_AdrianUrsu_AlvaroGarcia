package com.tecnocampus.LS2.protube_back.infrastructure.web;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Reloading any frontend route must return the SPA, with the real MVC setup and security chain. The standalone
 * {@link IndexControllerTest} missed that "" stops matching "/" once the mapping lists several paths.
 * The built frontend (classpath:/static/index.html) only exists with the prod profile, so the view is read from
 * the placeholder in templates/: this test checks the routing, not the page.
 */
@SpringBootTest(properties = "spring.thymeleaf.prefix=classpath:/templates/")
@AutoConfigureMockMvc
class SpaRoutesTest {

    @Autowired
    MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {"/", "/login", "/register", "/watch/1", "/profile/lists", "/upload", "/my-videos"})
    void frontendRoutesReturnTheIndex(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/not-a-page", "/assets/missing.js"})
    void otherPathsAreNotFound(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isNotFound());
    }
}
