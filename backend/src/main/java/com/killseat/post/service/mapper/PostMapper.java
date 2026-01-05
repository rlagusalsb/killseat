package com.killseat.post.service.mapper;

import com.killseat.member.entity.Member;
import com.killseat.post.dto.PostRequestDto;
import com.killseat.post.dto.PostResponseDto;
import com.killseat.post.entity.Post;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public PostResponseDto toDto(Post post) {
        return new PostResponseDto(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getMemberId(),
                post.getMember().getName(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public Post toEntity(PostRequestDto request, Member member) {
        return Post.builder()
                .member(member)
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .build();
    }
}
