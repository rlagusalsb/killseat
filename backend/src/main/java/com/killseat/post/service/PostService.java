package com.killseat.post.service;

import com.killseat.member.entity.Member;
import com.killseat.member.repository.MemberRepository;
import com.killseat.post.dto.PostRequestDto;
import com.killseat.post.dto.PostResponseDto;
import com.killseat.post.entity.Post;
import com.killseat.post.repository.PostRepository;
import com.killseat.post.service.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostMapper postMapper;

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPosts(String keyword, Pageable pageable) {
        Page<Post> page;

        if ((keyword == null) || (keyword.isBlank())) {
            page = postRepository.findAll(pageable);
        } else {
            page = postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                    keyword, keyword, pageable
            );
        }

        return page.map(postMapper::toDto);
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long postId) {
        validatePostId(postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return postMapper.toDto(post);
    }

    @Transactional
    public PostResponseDto createPost(Long memberId, PostRequestDto request) {
        validateMemberId(memberId);
        validatePostReq(request);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        Post saved = postRepository.save(postMapper.toEntity(request, member));
        return postMapper.toDto(saved);
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, Long memberId, PostRequestDto request) {
        validatePostId(postId);
        validateMemberId(memberId);
        validatePostReq(request);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!(post.getMember().getMemberId().equals(memberId))) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        post.update(request.getTitle().trim(), request.getContent().trim());
        return postMapper.toDto(post);
    }

    @Transactional
    public void deletePost(Long postId, Long memberId) {
        validatePostId(postId);
        validateMemberId(memberId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!(post.getMember().getMemberId().equals(memberId))) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }

    private void validatePostId(Long postId) {
        if ((postId == null)) {
            throw new IllegalArgumentException("게시글 ID가 필요합니다.");
        }
    }

    private void validateMemberId(Long memberId) {
        if ((memberId == null)) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
    }

    private void validatePostReq(PostRequestDto req) {
        if ((req == null)) {
            throw new IllegalArgumentException("요청 본문이 필요합니다.");
        }

        if ((req.getTitle() == null) || (req.getTitle().isBlank())) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }

        if ((req.getContent() == null) || (req.getContent().isBlank())) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }

        if ((req.getTitle().length() > 100)) {
            throw new IllegalArgumentException("제목은 100자 이하여야 합니다.");
        }
    }
}
