package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.ManageCommentsUseCase;
import com.tecnocampus.LS2.protube_back.domain.model.VideoComment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CommentsController.class, properties = "pro_tube.store.dir=c:")
@AutoConfigureMockMvc(addFilters = false)
class CommentsControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean ManageCommentsUseCase comments;

    @Test
    void listsPublicAndCurrentUserComments() throws Exception {
        when(comments.listForVideo(2L, null)).thenReturn(List.of(comment(8L)));
        when(comments.listForAuthor("ana@example.com")).thenReturn(List.of(comment(8L)));

        mockMvc.perform(get("/api/videos/2/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("A comment"))
                .andExpect(jsonPath("$[0].videoTitle").value("A video"));
        mockMvc.perform(get("/api/comments/mine").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].authorUsername").value("ana"));
    }

    @Test
    void createsAndEditsComments() throws Exception {
        when(comments.create("ana@example.com", 2L, null, "A comment")).thenReturn(comment(8L));
        when(comments.edit("ana@example.com", 8L, "Changed")).thenReturn(comment(8L));

        mockMvc.perform(post("/api/videos/2/comments").principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"A comment\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8));
        mockMvc.perform(put("/api/comments/8").principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"Changed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8));
    }

    @Test
    void deletesAndTogglesLike() throws Exception {
        when(comments.toggleLike("ana@example.com", 8L)).thenReturn(true);

        mockMvc.perform(delete("/api/comments/8").principal(authentication()))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/comments/8/likes").principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));
        verify(comments).delete("ana@example.com", 8L);
    }

    @Test
    void rejectsBlankComments() throws Exception {
        mockMvc.perform(post("/api/videos/2/comments").principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    private static VideoComment comment(Long id) {
        return new VideoComment(id, 2L, "A video", 1L, "ana", null, "A comment", Instant.EPOCH, 0, false);
    }

    private static UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("ana@example.com", "", List.of());
    }
}
