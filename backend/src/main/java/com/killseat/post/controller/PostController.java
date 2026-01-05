package com.killseat.post.controller;

import com.killseat.config.CustomUserDetails;
import com.killseat.post.dto.PostRequestDto;
import com.killseat.post.dto.PostResponseDto;
import com.killseat.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPosts(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable
    )
    {
        Page<PostResponseDto> response = postService.getPosts(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId) {
        PostResponseDto response = postService.getPost(postId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @RequestBody PostRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        PostResponseDto response = postService.createPost(user.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @RequestBody PostRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        PostResponseDto response = postService.updatePost(postId, user.getMemberId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    )
    {
        postService.deletePost(postId, user.getMemberId());
        return ResponseEntity.noContent().build();
    }
}
