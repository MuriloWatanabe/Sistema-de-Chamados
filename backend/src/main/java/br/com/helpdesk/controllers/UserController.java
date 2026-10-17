package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.dtos.UserUpsertRequest;
import br.com.helpdesk.services.UserService;
import br.com.helpdesk.security.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Cadastro e consulta de usuarios")
public class UserController {
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public UserController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'SUPERVISOR', 'ADMIN')")
    @Operation(summary = "Listar usuarios", description = "Ordenados por nome. Tecnicos, supervisores e administradores.")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    public List<UserResponse> listUsers() {
        return userService.listUsers();
    }

    @GetMapping("/technicians")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'SUPERVISOR', 'ADMIN')")
    @Operation(summary = "Listar tecnicos ativos", description = "Usuarios que podem ser atribuidos como responsaveis por um chamado.")
    @ApiResponse(responseCode = "200", description = "Lista de tecnicos ativos")
    public List<UserResponse> listTechnicians() {
        return userService.listTechnicians();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'SUPERVISOR', 'ADMIN')")
    @Operation(summary = "Buscar usuario por ID")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "Usuario inexistente")
    public UserResponse getUser(@Parameter(description = "ID do usuario", example = "1") @PathVariable Long id) {
        return userService.getUser(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar usuario",
            description = "Somente administradores. A senha e obrigatoria e segue a politica de senhas.")
    @ApiResponse(responseCode = "201", description = "Usuario criado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos, senha ausente ou fora da politica, ou e-mail ja cadastrado")
    public UserResponse createUser(@Valid @RequestBody UserUpsertRequest request, Authentication authentication) {
        return userService.createUser(request, currentUserService.requireUser(authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    @Operation(summary = "Atualizar usuario",
            description = "Supervisores e administradores. Somente administradores podem alterar o perfil (role) "
                    + "ou editar contas de administrador; ninguem altera o proprio perfil nem se desativa. "
                    + "Senha omitida mantem a atual. Mudar perfil, senha ou desativar revoga os tokens do usuario.")
    @ApiResponse(responseCode = "200", description = "Usuario atualizado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos, e-mail ja cadastrado, ou tentativa de alterar o proprio perfil ou se desativar")
    @ApiResponse(responseCode = "403", description = "Supervisor tentando alterar perfil ou editar um administrador")
    @ApiResponse(responseCode = "404", description = "Usuario inexistente")
    public UserResponse updateUser(
            @Parameter(description = "ID do usuario", example = "1") @PathVariable Long id,
            @Valid @RequestBody UserUpsertRequest request,
            Authentication authentication
    ) {
        return userService.updateUser(id, request, currentUserService.requireUser(authentication));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir usuario",
            description = "Somente administradores. Usuarios com chamados, comentarios ou registros de auditoria "
                    + "nao podem ser excluidos (desative-os).")
    @ApiResponse(responseCode = "204", description = "Usuario excluido")
    @ApiResponse(responseCode = "400", description = "Usuario possui registros vinculados ou e o proprio usuario autenticado")
    @ApiResponse(responseCode = "404", description = "Usuario inexistente")
    public void deleteUser(
            @Parameter(description = "ID do usuario", example = "1") @PathVariable Long id,
            Authentication authentication
    ) {
        userService.deleteUser(id, currentUserService.requireUser(authentication));
    }
}
