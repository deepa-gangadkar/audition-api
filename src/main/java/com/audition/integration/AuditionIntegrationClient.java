package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.audition.model.Comment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AuditionIntegrationClient {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private final RestTemplate restTemplate;

    public AuditionIntegrationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<AuditionPost> getPosts() {
        try {
            ResponseEntity<List<AuditionPost>> response =
                    restTemplate.exchange(
                            BASE_URL + "/posts",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<AuditionPost>>() {}
                    );

            return Optional.ofNullable(response.getBody())
                    .orElse(Collections.emptyList());

        } catch (RestClientException ex) {
            throw handleGenericRestException("Error fetching posts", "Failed to fetch posts", ex);
        }
    }

    public AuditionPost getPostById(final String id) {
        try {
            return restTemplate.getForObject(
                    BASE_URL + "/posts/{id}",
                    AuditionPost.class,
                    id
            );
        } catch (final HttpClientErrorException ex) {
            throw handleHttpClientErrorException(
                    ex,
                    "Error fetching post " + id,
                    "Cannot find a Post with id " + id
            );

        } catch (RestClientException ex) {
            throw handleGenericRestException(
                    "Unexpected error fetching post " + id,
                    "Unexpected error while fetching post",
                    ex
            );
        }
    }

    public AuditionPost getPostWithComments(final String postId) {
        AuditionPost post = getPostById(postId);
        ResponseEntity<List<Comment>> response =
                restTemplate.exchange(
                        BASE_URL + "/posts/{postId}/comments",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Comment>>() {},
                        postId
                );

        List<Comment> comments =
                Optional.ofNullable(response.getBody())
                        .orElse(Collections.emptyList());
        post.setComments(comments);
        return post;
    }

    public List<Comment> getCommentsByPostId(final String postId) {

        try {
            ResponseEntity<List<Comment>> response =
                    restTemplate.exchange(
                            BASE_URL + "/comments?postId={postId}",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Comment>>() {},
                            postId
                    );

            return Optional.ofNullable(response.getBody())
                    .orElse(Collections.emptyList());

        } catch (RestClientException ex) {
            throw handleGenericRestException(
                    "Error fetching comments for post " + postId,
                    "Failed to fetch comments",
                    ex
            );
        }
    }

    /* =========================
       Exception Handling Logic
       ========================= */
    private SystemException handleGenericRestException(
            String logMessage,
            String clientMessage,
            RestClientException ex
    ) {
        log.error(logMessage, ex);
        return new SystemException(
                clientMessage,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }

    private SystemException handleHttpClientErrorException(
            HttpClientErrorException ex,
            String logMessage,
            String notFoundMessage
    ) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.warn(logMessage + " - NOT FOUND", ex);
            return new SystemException(
                    notFoundMessage,
                    ex.getResponseBodyAsString(),
                    HttpStatus.NOT_FOUND.value()
            );
        }

        log.error(logMessage, ex);
        return new SystemException(
                "Client error while fetching post",
                ex.getResponseBodyAsString(),
                ex.getStatusCode().value()
        );
    }
}
