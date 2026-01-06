package com.killseat.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CommentResponseDto {
    private Long commentId;
    private Long postId;
    private Long memberId;
    private String memberName;
    private Long parentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponseDto> children;
    private boolean mine;
}
