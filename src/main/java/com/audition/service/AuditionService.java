package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.audition.model.Comment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuditionService {

    private final AuditionIntegrationClient auditionIntegrationClient;

    public AuditionService(AuditionIntegrationClient auditionIntegrationClient) {
        this.auditionIntegrationClient = auditionIntegrationClient;
    }

    public List<AuditionPost> getPosts(String filter) {
        List<AuditionPost> posts =
                Optional.ofNullable(auditionIntegrationClient.getPosts())
                        .orElse(Collections.emptyList());

        if (filter == null || filter.trim().isEmpty()) {
            return posts;
        }

        String normalizedFilter = filter.trim().toLowerCase();

        return posts.stream()
                .filter(post -> matchesFilter(post, normalizedFilter))
                .collect(Collectors.toList());
    }

    /**
     * Returns post by ID
     */
    public AuditionPost getPostById(final String postId) {
        return auditionIntegrationClient.getPostById(postId);
    }

    /**
     * Returns comments for a specific post
     */
    public List<Comment> getCommentsByPostId(final String postId) {

        List<Comment> comments =
                Optional.ofNullable(
                                auditionIntegrationClient.getCommentsByPostId(postId))
                        .orElse(Collections.emptyList());

        return comments;
    }

    /**
     * Returns post with embedded comments
     */
    public AuditionPost getPostWithComments(final String postId) {
        return auditionIntegrationClient.getPostWithComments(postId);
    }

    /**
     * Filtering logic (title + body)
     */
    private boolean matchesFilter(AuditionPost post, String filter) {

        return (post.getTitle() != null &&
                post.getTitle().toLowerCase().contains(filter))

                || (post.getBody() != null &&
                post.getBody().toLowerCase().contains(filter));
    }
}
