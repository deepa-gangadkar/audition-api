package com.audition.model;

import lombok.Data;

@Data
public class Comment {
    private Long postId;
    private Long id;
    private String name;
    private String email;
    private String body;
}
