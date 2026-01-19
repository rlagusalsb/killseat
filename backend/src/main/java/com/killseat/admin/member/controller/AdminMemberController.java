package com.killseat.admin.member.controller;

import com.killseat.admin.member.dto.AdminMemberResponseDto;
import com.killseat.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<Page<AdminMemberResponseDto>> getAllMembers(
            @PageableDefault(size = 10, sort = "memberId", direction = Sort.Direction.DESC) Pageable pageable
    )
    {
        Page<AdminMemberResponseDto> members = memberService.getAllMembers(pageable);
        return ResponseEntity.ok(members);
    }
}
