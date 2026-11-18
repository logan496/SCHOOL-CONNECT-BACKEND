package com.schoolconnect.api.identity.web;

import com.schoolconnect.api.identity.dto.AuthDtos.AuthResponse;
import com.schoolconnect.api.identity.dto.AuthDtos.LoginRequest;
import com.schoolconnect.api.identity.dto.AuthDtos.RegisterRequest;
import com.schoolconnect.api.identity.dto.AuthDtos.UserView;
import com.schoolconnect.api.identity.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserView me(Authentication authentication) {
        return authService.currentUser(authentication.getName());
    }
}
