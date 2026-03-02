package com.audition.web;

import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class AuditionController {

    private final AuditionService auditionService;

    public AuditionController(AuditionService auditionService) {
        this.auditionService = auditionService;
    }

    @GetMapping
    public ResponseEntity<List<AuditionPost>> getPosts(
            @RequestParam(name = "filter", required = false) String filter) {

        List<AuditionPost> posts = auditionService.getPosts(filter);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditionPost> getPostById(
            @PathVariable("id") @NotBlank String postId) {
        validatePostId(postId);
        AuditionPost post = auditionService.getPostById(postId);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getCommentsByPostId(
            @PathVariable("id") @NotBlank String postId) {
        validatePostId(postId);
        List<Comment> comments = auditionService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}/with-comments")
    public ResponseEntity<AuditionPost> getPostWithComments(
            @PathVariable("id") @NotBlank String postId) {

        validatePostId(postId);
        AuditionPost post = auditionService.getPostWithComments(postId);
        return ResponseEntity.ok(post);
    }

    private void validatePostId(String postId) {
        if (!postId.matches("\\d+")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Post ID must be numeric"
            );
        }
    }
}
