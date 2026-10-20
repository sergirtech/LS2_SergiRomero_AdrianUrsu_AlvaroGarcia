package com.tecnocampus.LS2.protube_back.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest({
        "pro_tube.store.dir=c:",
        "pro_tube.load_initial_data=false"
})
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    private static final String REGISTER_JSON =
            "{\"email\":\"Ana@Mail.com\",\"username\":\"ana\",\"password\":\"password123\"}";

    @Autowired
    MockMvc mockMvc;

    private MockHttpSession register() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(REGISTER_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        return session;
    }
    private MvcResult login(String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@mail.com\",\"password\":\"" + password + "\"}"))
                .andReturn();
    }

    @Test
    void registerCreatesUserStartsSessionAndNeverReturnsThePassword() throws Exception {
        MockHttpSession session = register();

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@mail.com"))
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registerWithDuplicateEmailReturns409() throws Exception {
        register();

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)

                        .content("{\"email\":\"ana@mail.com\",\"username\":\"other\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This email is already registered"));
    }

    @Test
    void registerWithInvalidDataReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)

                        .content("{\"email\":\"not-an-email\",\"username\":\"ana\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Enter a valid email address"))
                .andExpect(jsonPath("$.errors.password").value("Password must be between 8 and 72 characters"));
    }

    @Test
    void loginWithCorrectCredentialsStartsSession() throws Exception {
        register();

        MvcResult result = login("password123");

        assertNotNull(result.getRequest().getSession(false));
        mockMvc.perform(get("/api/auth/me").session((MockHttpSession)
                        result.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana"));
    }

    @Test
    void loginWithWrongPasswordReturns401WithGenericMessage() throws Exception {
        register();

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@mail.com\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Incorrect email or password"));
    }

    @Test
    void loginWithUnknownEmailReturnsTheSameMessage() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@mail.com\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Incorrect email or password"));
    }

    @Test
    void afterLogoutMeReturns401() throws Exception {
        MockHttpSession session = register();

        mockMvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void loginChangesTheSessionIdToPreventSessionFixation() throws Exception {
        MockHttpSession session = register();
        String idBeforeLogin = session.getId();

        MvcResult result = mockMvc.perform(post("/api/auth/login").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@mail.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        assertNotEquals(idBeforeLogin, result.getRequest().getSession(false).getId());
    }
}
