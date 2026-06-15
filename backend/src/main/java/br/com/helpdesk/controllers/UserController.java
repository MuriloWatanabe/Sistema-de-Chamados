package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.dtos.UserUpsertRequest;
import br.com.helpdesk.services.UserService;
import br.com.helpdesk.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public UserController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'SUPERVISOR', 'ADMIN')")
    public List<UserResponse> listUsers() {
        return userService.listUsers();
    }

    @GetMapping("/technicians")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'SUPERVISOR', 'ADMIN')")
    public List<UserResponse> listTechnicians() {
        return userService.listTechnicians();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'SUPERVISOR', 'ADMIN')")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(@Valid @RequestBody UserUpsertRequest request, Authentication authentication) {
        return userService.createUser(request, currentUserService.requireUser(authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserUpsertRequest request, Authentication authentication) {
        return userService.updateUser(id, request, currentUserService.requireUser(authentication));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id, Authentication authentication) {
        userService.deleteUser(id, currentUserService.requireUser(authentication));
    }
}
