package com.killseat.comment.controller;

import com.killseat.comment.dto.CommentCreateRequestDto;
import com.killseat.comment.dto.CommentResponseDto;
import com.killseat.comment.dto.CommentUpdateRequestDto;
import com.killseat.comment.service.CommentService;
import com.killseat.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/posts/{postId}")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long postId) {
        List<CommentResponseDto> response = commentService.getComments(postId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts/{postId}")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        CommentResponseDto response =
                commentService.createComment(postId, user.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    ) throws AccessDeniedException {
        CommentResponseDto response =
                commentService.updateComment(commentId, user.getMemberId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) throws AccessDeniedException {
        commentService.deleteComment(commentId, user.getMemberId());
        return ResponseEntity.noContent().build();
    }
}
