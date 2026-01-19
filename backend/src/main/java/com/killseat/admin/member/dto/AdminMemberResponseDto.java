package com.killseat.admin.member.dto;

import com.killseat.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminMemberResponseDto {
    private Long memberId;
    private String email;
    private String name;
    private Role role;
    private LocalDateTime createdAt;
}
