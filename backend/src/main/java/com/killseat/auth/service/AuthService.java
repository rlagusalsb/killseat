package com.killseat.auth.service;

import com.killseat.auth.dto.LoginRequestDto;
import com.killseat.auth.dto.RefreshRequestDto;
import com.killseat.auth.dto.TokenResponseDto;
import com.killseat.common.exception.CustomErrorCode;
import com.killseat.common.exception.CustomException;
import com.killseat.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public TokenResponseDto login(LoginRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            return new TokenResponseDto(accessToken, refreshToken);
        } catch (AuthenticationException e) {
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }

    }

    public TokenResponseDto refresh(RefreshRequestDto request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomException(CustomErrorCode.TOKEN_NOT_VALID);
        }

        String username = jwtUtil.extractUsername(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        return new TokenResponseDto(newAccessToken, refreshToken);
    }
}
