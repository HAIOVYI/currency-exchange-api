package projects.currencyexchangeapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import projects.currencyexchangeapi.dto.login.UserLoginRequestDto;
import projects.currencyexchangeapi.dto.login.UserLoginResponseDto;
import projects.currencyexchangeapi.dto.registration.UserRegisterRequestDto;
import projects.currencyexchangeapi.dto.registration.UserRegisterResponseDto;
import projects.currencyexchangeapi.service.UserService;
import projects.currencyexchangeapi.service.impl.AuthenticationService;

@RestController
@RequiredArgsConstructor
@Tag(name = "User authentication", description = "Endpoints for authenticate users")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "User Registration",
            description = "This endpoint allows for the registration of a "
                    + "new user by providing the necessary"
                    + " details such as username, password, and email.")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegisterResponseDto register(@Valid @RequestBody UserRegisterRequestDto requestDto) {
        return userService.register(requestDto);
    }

    @Operation(summary = "User Login",
            description = "This endpoint allows an existing user to log in"
                    + " by providing the necessary "
                    + "details such as email and password.")
    @PostMapping("/login")
    public UserLoginResponseDto login(@Valid @RequestBody UserLoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }
}
