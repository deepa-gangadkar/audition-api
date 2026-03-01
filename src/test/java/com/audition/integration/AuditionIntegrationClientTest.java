package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AuditionIntegrationClientTest {
    private RestTemplate restTemplate;
    private AuditionIntegrationClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restTemplate = mock(RestTemplate.class);
        client = new AuditionIntegrationClient(restTemplate);
    }

    // -----------------------------
    // getPosts tests
    // -----------------------------
    @Test
    @DisplayName("getPosts returns list of posts successfully")
    void testGetPostsSuccess() {
        List<AuditionPost> mockPosts = List.of(new AuditionPost(), new AuditionPost());
        ResponseEntity<List<AuditionPost>> responseEntity = ResponseEntity.ok(mockPosts);

        when(restTemplate.exchange(
                eq("https://jsonplaceholder.typicode.com/posts"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPost>>>any()
        )).thenReturn(responseEntity);

        List<AuditionPost> posts = client.getPosts();

        assertThat(posts).isNotNull().hasSize(2);
    }

    @Test
    @DisplayName("getPosts returns empty list when API returns null body")
    void testGetPostsNullBody() {
        ResponseEntity<List<AuditionPost>> responseEntity = ResponseEntity.ok(null);

        when(restTemplate.exchange(
                eq("https://jsonplaceholder.typicode.com/posts"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPost>>>any()
        )).thenReturn(responseEntity);

        List<AuditionPost> posts = client.getPosts();

        assertThat(posts).isEmpty();
    }

    @Test
    @DisplayName("getPosts throws SystemException on RestClientException")
    void testGetPostsRestClientException() {
        when(restTemplate.exchange(
                anyString(), any(HttpMethod.class), any(), any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("API down"));

        assertThatThrownBy(client::getPosts)
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("Failed to fetch posts");
    }

    // -----------------------------
    // getPostById tests
    // -----------------------------
    @Test
    @DisplayName("getPostById returns post successfully")
    void testGetPostByIdSuccess() {
        AuditionPost post = new AuditionPost();
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), eq("1"))).thenReturn(post);

        AuditionPost result = client.getPostById("1");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getPostById throws SystemException for 404 NOT_FOUND")
    void testGetPostByIdNotFound() {
        HttpClientErrorException notFound = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), eq("1")))
                .thenThrow(notFound);

        assertThatThrownBy(() -> client.getPostById("1"))
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("Cannot find a Post with id 1");
    }

    @Test
    @DisplayName("getPostById throws SystemException for other HttpClientErrorException")
    void testGetPostByIdClientError() {
        HttpClientErrorException clientError = HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null);
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), eq("1")))
                .thenThrow(clientError);

        assertThatThrownBy(() -> client.getPostById("1"))
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("Client error while fetching post");
    }

    @Test
    @DisplayName("getPostById throws SystemException for RestClientException")
    void testGetPostByIdRestClientException() {
        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), eq("1")))
                .thenThrow(new RestClientException("Timeout"));

        assertThatThrownBy(() -> client.getPostById("1"))
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("Unexpected error while fetching post");
    }

    // -----------------------------
    // getPostWithComments tests
    // -----------------------------
    @Test
    @DisplayName("getPostWithComments returns post with comments")
    void testGetPostWithCommentsSuccess() {
        AuditionPost post = new AuditionPost();
        post.setId(1);

        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), eq("1"))).thenReturn(post);
        ResponseEntity<List<Comment>> responseEntity = ResponseEntity.ok(List.of(new Comment(), new Comment()));

        when(restTemplate.exchange(
                eq("https://jsonplaceholder.typicode.com/posts/{postId}/comments"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<Comment>>>any(),
                eq("1")
        )).thenReturn(responseEntity);

        AuditionPost result = client.getPostWithComments("1");

        assertThat(result.getComments()).hasSize(2);
    }

    @Test
    @DisplayName("getPostWithComments handles null comments as empty list")
    void testGetPostWithCommentsNullComments() {
        AuditionPost post = new AuditionPost();
        post.setId(1);

        when(restTemplate.getForObject(anyString(), eq(AuditionPost.class), eq("1"))).thenReturn(post);
        ResponseEntity<List<Comment>> responseEntity = ResponseEntity.ok(null);

        when(restTemplate.exchange(
                eq("https://jsonplaceholder.typicode.com/posts/{postId}/comments"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<Comment>>>any(),
                eq("1")
        )).thenReturn(responseEntity);

        AuditionPost result = client.getPostWithComments("1");

        assertThat(result.getComments()).isEmpty();
    }

    // -----------------------------
    // getCommentsByPostId tests
    // -----------------------------
    @Test
    @DisplayName("getCommentsByPostId returns list of comments")
    void testGetCommentsByPostIdSuccess() {
        ResponseEntity<List<Comment>> responseEntity = ResponseEntity.ok(List.of(new Comment(), new Comment()));

        when(restTemplate.exchange(
                eq("https://jsonplaceholder.typicode.com/comments?postId={postId}"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<Comment>>>any(),
                eq("1")
        )).thenReturn(responseEntity);

        List<Comment> comments = client.getCommentsByPostId("1");

        assertThat(comments).hasSize(2);
    }

    @Test
    @DisplayName("getCommentsByPostId handles null response body as empty list")
    void testGetCommentsByPostIdNullBody() {
        ResponseEntity<List<Comment>> responseEntity = ResponseEntity.ok(null);

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class), eq("1")
        )).thenReturn(responseEntity);

        List<Comment> comments = client.getCommentsByPostId("1");

        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("getCommentsByPostId throws SystemException on RestClientException")
    void testGetCommentsByPostIdRestClientException() {
        when(restTemplate.exchange(
                anyString(), any(HttpMethod.class), any(), any(ParameterizedTypeReference.class), eq("1")
        )).thenThrow(new RestClientException("API down"));

        assertThatThrownBy(() -> client.getCommentsByPostId("1"))
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("Failed to fetch comments");
    }
}
