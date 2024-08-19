package projects.currencyexchangeapi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import projects.currencyexchangeapi.dto.login.UserLoginRequestDto;
import projects.currencyexchangeapi.dto.login.UserLoginResponseDto;
import projects.currencyexchangeapi.security.JwtUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserLoginResponseDto authenticate(UserLoginRequestDto requestDto) {
        log.info("Attempting to authenticate user");

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.email(), requestDto.password())
        );
        log.info("User authenticated successfully, generating token");

        String token = jwtUtil.generateToken(authentication.getName());
        log.info("Token generated successfully");

        return new UserLoginResponseDto(token);
    }
}
