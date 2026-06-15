package br.com.helpdesk.security;

import br.com.helpdesk.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public record AuthenticatedUser(
        Long id,
        String name,
        String email,
        UserRole role
) {
    public List<GrantedAuthority> authorities() {
        return List.of(role.toGrantedAuthority());
    }
}
