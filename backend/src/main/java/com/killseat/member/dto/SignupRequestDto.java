package com.killseat.member.dto;

import com.killseat.member.entity.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
    private Role role;
}
