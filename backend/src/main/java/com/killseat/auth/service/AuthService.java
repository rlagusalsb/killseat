package com.killseat.auth.service;

import com.killseat.auth.dto.LoginRequestDto;
import com.killseat.auth.dto.RefreshRequestDto;
import com.killseat.auth.dto.TokenResponseDto;
import com.killseat.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    }

    public TokenResponseDto refresh(RefreshRequestDto request) {
        String username = jwtUtil.extractUsername(request.getRefreshToken());

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.validateToken(request.getRefreshToken(), userDetails)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        return new TokenResponseDto(newAccessToken, request.getRefreshToken());
    }
}
