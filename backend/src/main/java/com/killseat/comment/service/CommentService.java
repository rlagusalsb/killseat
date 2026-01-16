package com.killseat.comment.service;

import com.killseat.comment.dto.CommentCreateRequestDto;
import com.killseat.comment.dto.CommentResponseDto;
import com.killseat.comment.dto.CommentUpdateRequestDto;
import com.killseat.comment.entity.Comment;
import com.killseat.comment.repository.CommentRepository;
import com.killseat.comment.service.mapper.CommentMapper;
import com.killseat.common.exception.CustomErrorCode;
import com.killseat.common.exception.CustomException;
import com.killseat.member.entity.Member;
import com.killseat.member.repository.MemberRepository;
import com.killseat.post.entity.Post;
import com.killseat.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long postId, Long currentMemberId) {
        validatePostId(postId);

        List<Comment> parents =
                commentRepository.findByPost_PostIdAndParentIsNullOrderByCreatedAtAsc(postId);

        return parents.stream()
                .map(p -> commentMapper.toDtoWithChildren(p, currentMemberId))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponseDto createComment(Long postId, Long memberId, CommentCreateRequestDto request) {
        validatePostId(postId);
        validateMemberId(memberId);
        validateCreateReq(request);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        Comment parent = null;

        if ((request.getParentId() != null)) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.PARENT_COMMENT_NOT_FOUND));

            if (!(parent.getPost().getPostId().equals(postId))) {
                throw new CustomException(CustomErrorCode.PARENT_COMMENT_NOT_MATCH);
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .parent(parent)
                .content(request.getContent().trim())
                .build();

        Comment saved = commentRepository.save(comment);
        return commentMapper.toDto(saved, memberId);
    }

    @Transactional
    public CommentResponseDto updateComment(
            Long commentId, Long memberId, CommentUpdateRequestDto request
    ) throws AccessDeniedException {
        validateCommentId(commentId);
        validateMemberId(memberId);
        validateUpdateReq(request);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.COMMENT_NOT_FOUND));

        if (!(comment.getMember().getMemberId().equals(memberId))) {
            throw new CustomException(CustomErrorCode.ACCESS_DENIED_COMMENT);
        }

        comment.update(request.getContent().trim());
        return commentMapper.toDto(comment, memberId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long memberId) throws AccessDeniedException {
        validateCommentId(commentId);
        validateMemberId(memberId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.COMMENT_NOT_FOUND));

        if (!(comment.getMember().getMemberId().equals(memberId))) {
            throw new CustomException(CustomErrorCode.ACCESS_DENIED_COMMENT);
        }

        commentRepository.delete(comment);
    }

    private void validatePostId(Long postId) {
        if ((postId == null)) {
            throw new CustomException(CustomErrorCode.MISSING_PARAMETER);
        }
    }

    private void validateCommentId(Long commentId) {
        if ((commentId == null)) {
            throw new CustomException(CustomErrorCode.MISSING_PARAMETER);
        }
    }

    private void validateMemberId(Long memberId) {
        if ((memberId == null)) {
            throw new CustomException(CustomErrorCode.TOKEN_NOT_VALID);
        }
    }

    private void validateCreateReq(CommentCreateRequestDto req) {
        if (req == null || req.getContent() == null || req.getContent().isBlank()) {
            throw new CustomException(CustomErrorCode.MISSING_PARAMETER);
        }

        if ((req.getContent().length() > 500)) {
            throw new CustomException(CustomErrorCode.INVALID_INPUT_FORMAT);
        }
    }

    private void validateUpdateReq(CommentUpdateRequestDto req) {
        if (req == null || req.getContent() == null || req.getContent().isBlank()) {
            throw new CustomException(CustomErrorCode.MISSING_PARAMETER);
        }

        if (req.getContent().length() > 500) {
            throw new CustomException(CustomErrorCode.INVALID_INPUT_FORMAT);
        }
    }
}
