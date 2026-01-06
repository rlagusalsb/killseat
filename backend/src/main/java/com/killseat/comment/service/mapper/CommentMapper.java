package com.killseat.comment.service.mapper;

import com.killseat.comment.dto.CommentResponseDto;
import com.killseat.comment.entity.Comment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public CommentResponseDto toDto(Comment comment, Long currentMemberId) {
        boolean mine =
                (currentMemberId != null) && (comment.getMember().equals(currentMemberId));

        return new CommentResponseDto(
                comment.getCommentId(),
                comment.getPost().getPostId(),
                comment.getMember().getMemberId(),
                comment.getMember().getName(),
                (comment.getParent() != null ? comment.getParent().getCommentId() : null),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                null,
                mine
        );
    }

    //부모 댓글 하나를 받아, 해당 댓글의 대댓글들을 포함한 DTO로 변환
    public CommentResponseDto toDtoWithChildren(Comment parent, Long currentMemberId) {
        List<CommentResponseDto> children = parent.getChildren().stream()
                .map(child -> toDto(child,currentMemberId))
                .collect(Collectors.toList());

        boolean mine =
                (currentMemberId != null) &&
                        (parent.getMember().getMemberId().equals(currentMemberId));

        return new CommentResponseDto(
                parent.getCommentId(),
                parent.getPost().getPostId(),
                parent.getMember().getMemberId(),
                parent.getMember().getName(),
                null,
                parent.getContent(),
                parent.getCreatedAt(),
                parent.getUpdatedAt(),
                children,
                mine
        );
    }
}
