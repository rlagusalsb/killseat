package com.killseat.comment.service.mapper;

import com.killseat.comment.dto.CommentResponseDto;
import com.killseat.comment.entity.Comment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public CommentResponseDto toDto(Comment comment) {
        return new CommentResponseDto(
                comment.getCommentId(),
                comment.getPost().getPostId(),
                comment.getMember().getMemberId(),
                comment.getMember().getName(),
                (comment.getParent() != null ? comment.getParent().getCommentId() : null),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                null
        );
    }

    //부모 댓글 하나를 받아, 해당 댓글의 대댓글들을 포함한 DTO로 변환
    public CommentResponseDto toDtoWithChildren(Comment parent) {
        List<CommentResponseDto> children = parent.getChildren().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new CommentResponseDto(
                parent.getCommentId(),
                parent.getPost().getPostId(),
                parent.getMember().getMemberId(),
                parent.getMember().getName(),
                null,
                parent.getContent(),
                parent.getCreatedAt(),
                parent.getUpdatedAt(),
                children
        );
    }
}
