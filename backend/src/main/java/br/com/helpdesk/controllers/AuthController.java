package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.LoginRequest;
import br.com.helpdesk.dtos.LoginResponse;
import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.services.AuthService;
import br.com.helpdesk.security.CurrentUserService;
import br.com.helpdesk.utils.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CurrentUserService currentUserService;

    public AuthController(AuthService authService, CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse me(Authentication authentication) {
        return DtoMapper.toUserResponse(currentUserService.requireUser(authentication));
    }
}
