package com.audition.controller;

import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import com.audition.web.AuditionController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuditionController.class)
public class AuditionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditionService auditionService;

    @MockBean
    private AuditionLogger auditionLogger;

    @Test
    @DisplayName("GET /posts - should return list of posts")
    void getPosts_shouldReturnPosts() throws Exception {

        AuditionPost post = new AuditionPost();
        post.setId(1);
        post.setTitle("Spring Boot Guide");
        post.setBody("Learn Spring");

        when(auditionService.getPosts(null))
                .thenReturn(List.of(post));

        mockMvc.perform(get("/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Spring Boot Guide"))
                .andExpect(jsonPath("$[0].body").value("Learn Spring"));

        verify(auditionService).getPosts(null);
    }

    @Test
    @DisplayName("GET /posts?filter=value - should pass filter to service")
    void getPosts_shouldApplyFilter() throws Exception {

        when(auditionService.getPosts("spring"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/posts")
                        .param("filter", "spring"))
                .andExpect(status().isOk());

        verify(auditionService).getPosts("spring");
    }

    // --------------------------------------------------
    // GET /posts/{id}
    // --------------------------------------------------
    @Test
    @DisplayName("GET /posts/{id} - should return single post")
    void getPostById_shouldReturnPost() throws Exception {

        AuditionPost post = new AuditionPost();
        post.setId(1);
        post.setTitle("Single Post");

        when(auditionService.getPostById("1"))
                .thenReturn(post);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Single Post"));

        verify(auditionService).getPostById("1");
    }

    @Test
    @DisplayName("GET /posts/{id} - should return 400 for invalid ID")
    void getPostById_shouldReturnBadRequest_whenInvalidId() throws Exception {

        mockMvc.perform(get("/posts/abc"))
                .andExpect(status().isBadRequest());
    }

    // --------------------------------------------------
    // GET /posts/{id}/comments
    // --------------------------------------------------

    @Test
    @DisplayName("GET /posts/{id}/comments - should return comments")
    void getComments_shouldReturnComments() throws Exception {

        Comment comment = new Comment();
        comment.setId(10L);
        comment.setBody("Nice post!");

        when(auditionService.getCommentsByPostId("1"))
                .thenReturn(List.of(comment));

        mockMvc.perform(get("/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].body").value("Nice post!"));

        verify(auditionService).getCommentsByPostId("1");
    }

    // --------------------------------------------------
    // GET /posts/{id}/with-comments
    // --------------------------------------------------

    @Test
    @DisplayName("GET /posts/{id}/with-comments - should return post with embedded comments")
    void getPostWithComments_shouldReturnPostWithComments() throws Exception {

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setBody("Great!");

        AuditionPost post = new AuditionPost();
        post.setId(1);
        post.setTitle("Post With Comments");
        post.setComments(List.of(comment));

        when(auditionService.getPostWithComments("1"))
                .thenReturn(post);

        mockMvc.perform(get("/posts/1/with-comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.comments[0].id").value(100))
                .andExpect(jsonPath("$.comments[0].body").value("Great!"));

        verify(auditionService).getPostWithComments("1");
    }
}
