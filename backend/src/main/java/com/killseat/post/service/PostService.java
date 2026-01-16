package com.killseat.post.service;

import com.killseat.common.exception.CustomErrorCode;
import com.killseat.common.exception.CustomException;
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
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        return postMapper.toDto(post);
    }

    @Transactional
    public PostResponseDto createPost(Long memberId, PostRequestDto request) {
        validateMemberId(memberId);
        validatePostReq(request);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        Post saved = postRepository.save(postMapper.toEntity(request, member));
        return postMapper.toDto(saved);
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, Long memberId, PostRequestDto request) {
        validatePostId(postId);
        validateMemberId(memberId);
        validatePostReq(request);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        if (!(post.getMember().getMemberId().equals(memberId))) {
            throw new CustomException(CustomErrorCode.REJECTED_PERMISSION);
        }

        post.update(request.getTitle().trim(), request.getContent().trim());
        return postMapper.toDto(post);
    }

    @Transactional
    public void deletePost(Long postId, Long memberId) {
        validatePostId(postId);
        validateMemberId(memberId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        if (!(post.getMember().getMemberId().equals(memberId))) {
            throw new CustomException(CustomErrorCode.REJECTED_PERMISSION);
        }

        postRepository.delete(post);
    }

    private void validatePostId(Long postId) {
        if ((postId == null)) {
            throw new CustomException(CustomErrorCode.MISSING_PARAMETER);
        }
    }

    private void validateMemberId(Long memberId) {
        if ((memberId == null)) {
            throw new CustomException(CustomErrorCode.TOKEN_NOT_VALID);
        }
    }

    private void validatePostReq(PostRequestDto req) {
        if (req == null) {
            throw new CustomException(CustomErrorCode.MISSING_PARAMETER);
        }

        if (req.getTitle() == null || req.getTitle().isBlank() ||
                req.getContent() == null || req.getContent().isBlank()) {
            throw new CustomException(CustomErrorCode.MISSING_PARAMETER);
        }

        if (req.getTitle().length() > 100) {
            throw new CustomException(CustomErrorCode.INVALID_INPUT_FORMAT);
        }
    }
}
