package com.killseat.comment.repository;

import com.killseat.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost_PostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);
}
