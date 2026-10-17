package com.tecnocampus.LS2.protube_back.controller;

import com.tecnocampus.LS2.protube_back.services.__Resource__Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Spring Boot 3.3.x: use @MockBean (@MockitoBean only exists from 3.4)
@WebMvcTest(__Resource__Controller.class)
class __Resource__ControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    __Resource__Service __resource__Service;

    @Test
    void getAllReturnsOkWithList() throws Exception {
        when(__resource__Service.getAll()).thenReturn(List.of("item 1", "item 2"));

        mockMvc.perform(get("/api/__resources__"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("item 1"));
    }

    @Test
    void getByIdReturnsOkWhenFound() throws Exception {
        when(__resource__Service.getById(1L)).thenReturn(Optional.of("item 1"));

        mockMvc.perform(get("/api/__resources__/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdReturnsNotFoundWhenMissing() throws Exception {
        when(__resource__Service.getById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/__resources__/99"))
                .andExpect(status().isNotFound());
    }
}
