package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.ChangePasswordRequest;
import br.com.helpdesk.dtos.LoginRequest;
import br.com.helpdesk.dtos.LoginResponse;
import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.services.AuthService;
import br.com.helpdesk.security.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacao", description = "Login, logout, troca de senha e dados do usuario autenticado")
public class AuthController {
    private final AuthService authService;
    private final CurrentUserService currentUserService;

    public AuthController(AuthService authService, CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Login", description = "Valida e-mail e senha e devolve um JWT para o header `Authorization: Bearer <token>`. "
            + "Apos 5 tentativas erradas o e-mail fica bloqueado por 15 minutos.")
    @ApiResponse(responseCode = "200", description = "Login realizado")
    @ApiResponse(responseCode = "400", description = "E-mail ou senha nao informados ou e-mail em formato invalido")
    @ApiResponse(responseCode = "401", description = "E-mail ou senha incorretos, ou usuario inativo")
    @ApiResponse(responseCode = "429", description = "Login bloqueado por excesso de tentativas; o header Retry-After informa os segundos restantes")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Usuario autenticado", description = "Dados do usuario dono do token.")
    @ApiResponse(responseCode = "200", description = "Usuario autenticado")
    public UserResponse me(Authentication authentication) {
        return authService.describe(currentUserService.requireUser(authentication));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Logout", description = "Revoga todos os tokens ja emitidos para o usuario, em qualquer dispositivo.")
    @ApiResponse(responseCode = "204", description = "Sessoes encerradas")
    public void logout(Authentication authentication) {
        authService.logout(currentUserService.requireUser(authentication));
    }

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Trocar a propria senha",
            description = "Revoga os tokens anteriores e devolve um novo token para a sessao atual continuar.")
    @ApiResponse(responseCode = "200", description = "Senha alterada; use o novo token da resposta")
    @ApiResponse(responseCode = "400", description = "Senha atual incorreta, nova senha igual a atual ou fora da politica de senhas")
    public LoginResponse changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        return authService.changePassword(currentUserService.requireUser(authentication), request);
    }
}
