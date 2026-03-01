package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditionServiceTest {
    @Mock
    private AuditionIntegrationClient client;

    @InjectMocks
    private AuditionService service;

    @Test
    void getPosts_shouldFilterResults() {

        AuditionPost post1 = new AuditionPost();
        post1.setTitle("Spring Boot");

        AuditionPost post2 = new AuditionPost();
        post2.setTitle("Docker");

        when(client.getPosts())
                .thenReturn(List.of(post1, post2));

        List<AuditionPost> result =
                service.getPosts("spring");

        assertEquals(1, result.size());
        assertEquals("Spring Boot", result.get(0).getTitle());
    }

    @Test
    void getPostById_shouldDelegateToClient() {

        AuditionPost post = new AuditionPost();
        post.setId(1);

        when(client.getPostById("1")).thenReturn(post);

        AuditionPost result = service.getPostById("1");

        assertEquals(1L, result.getId());
        verify(client).getPostById("1");
    }

    @Test
    void getCommentsByPostId_shouldReturnEmptyList_whenNull() {

        when(client.getCommentsByPostId("1"))
                .thenReturn(null);

        List<Comment> result =
                service.getCommentsByPostId("1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPostWithComments_shouldDelegateToClient() {

        AuditionPost post = new AuditionPost();
        post.setComments(List.of(new Comment()));

        when(client.getPostWithComments("1"))
                .thenReturn(post);

        AuditionPost result =
                service.getPostWithComments("1");

        assertEquals(1, result.getComments().size());
    }
}
