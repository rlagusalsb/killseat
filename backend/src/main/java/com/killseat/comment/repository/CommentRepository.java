package com.killseat.comment.repository;

import com.killseat.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost_PostIdOrderByCreatedAtAsc(Long postId);

    List<Comment> findByPost_PostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);

    List<Comment> findByParent_CommentIdOrderByCreatedAtAsc(Long parentId);

    Optional<Comment> findByCommentIdAndMember_MemberId(Long commentId, Long memberId);
}
