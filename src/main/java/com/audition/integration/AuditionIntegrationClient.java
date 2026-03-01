package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.audition.model.Comment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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
            log.error("Error fetching posts", ex);
            throw new SystemException(
                    "Failed to fetch posts",
                    ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    public AuditionPost getPostById(final String id) {
        try {
            return restTemplate.getForObject(
                    BASE_URL + "/posts/{id}",
                    AuditionPost.class,
                    id
            );
        } catch (final HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find a Post with id " + id,
                        e.getResponseBodyAsString(),
                        HttpStatus.NOT_FOUND.value()
                );
            } log.error("Client error fetching post {}", id, e);
            throw new SystemException(
                    "Client error while fetching post",
                    e.getResponseBodyAsString(),
                    e.getStatusCode().value()
            );

        } catch (RestClientException e) {
            log.error("Unexpected error fetching post {}", id, e);
            throw new SystemException(
                    "Unexpected error while fetching post",
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
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
            log.error("Error fetching comments for post {}", postId, ex);
            throw new SystemException(
                    "Failed to fetch comments",
                    ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
}
