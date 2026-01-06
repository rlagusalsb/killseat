package com.killseat.comment.service;

import com.killseat.comment.dto.CommentCreateRequestDto;
import com.killseat.comment.dto.CommentResponseDto;
import com.killseat.comment.dto.CommentUpdateRequestDto;
import com.killseat.comment.entity.Comment;
import com.killseat.comment.repository.CommentRepository;
import com.killseat.comment.service.mapper.CommentMapper;
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
    public List<CommentResponseDto> getComments(Long postId) {
        validatePostId(postId);

        List<Comment> parents =
                commentRepository.findByPost_PostIdAndParentIsNullOrderByCreatedAtAsc(postId);

        return parents.stream()
                .map(commentMapper::toDtoWithChildren)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponseDto createComment(Long postId, Long memberId, CommentCreateRequestDto request) {
        validatePostId(postId);
        validateMemberId(memberId);
        validateCreateReq(request);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        Comment parent = null;

        if ((request.getParentId() != null)) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));

            if (!(parent.getPost().getPostId().equals(postId))) {
                throw new IllegalArgumentException("부모 댓글이 해당 게시글에 속하지 않습니다.");
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .parent(parent)
                .content(request.getContent().trim())
                .build();

        Comment saved = commentRepository.save(comment);
        return commentMapper.toDto(saved);
    }

    @Transactional
    public CommentResponseDto updateComment(
            Long commentId, Long memberId, CommentUpdateRequestDto request
    ) throws AccessDeniedException {
        validateCommentId(commentId);
        validateMemberId(memberId);
        validateUpdateReq(request);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!(comment.getMember().getMemberId().equals(memberId))) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        comment.update(request.getContent().trim());
        return commentMapper.toDto(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long memberId) throws AccessDeniedException {
        validateCommentId(commentId);
        validateMemberId(memberId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!(comment.getMember().getMemberId().equals(memberId))) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    private void validatePostId(Long postId) {
        if ((postId == null)) {
            throw new IllegalArgumentException("게시글 ID가 필요합니다.");
        }
    }

    private void validateCommentId(Long commentId) {
        if ((commentId == null)) {
            throw new IllegalArgumentException("댓글 ID가 필요합니다.");
        }
    }

    private void validateMemberId(Long memberId) {
        if ((memberId == null)) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
    }

    private void validateCreateReq(CommentCreateRequestDto req) {
        if ((req == null)) {
            throw new IllegalArgumentException("요청 본문이 필요합니다.");
        }

        if ((req.getContent() == null) || (req.getContent().isBlank())) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }

        if ((req.getContent().length() > 500)) {
            throw new IllegalArgumentException("댓글은 500자 이하여야 합니다.");
        }
    }

    private void validateUpdateReq(CommentUpdateRequestDto req) {
        if ((req == null)) {
            throw new IllegalArgumentException("요청 본문이 필요합니다.");
        }

        if ((req.getContent() == null) || (req.getContent().isBlank())) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }

        if ((req.getContent().length() > 500)) {
            throw new IllegalArgumentException("댓글은 500자 이하여야 합니다.");
        }
    }
}
