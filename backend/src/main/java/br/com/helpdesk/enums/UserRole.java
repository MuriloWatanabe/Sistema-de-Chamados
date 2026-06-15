package br.com.helpdesk.enums;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;

public enum UserRole {
    ADMIN(0, "ROLE_ADMIN", "Administrador"),
    SUPERVISOR(1, "ROLE_SUPERVISOR", "Supervisor"),
    TECHNICIAN(2, "ROLE_TECHNICIAN", "Tecnico"),
    REQUESTER(3, "ROLE_REQUESTER", "Solicitante");

    private final int code;
    private final String authority;
    private final String label;

    UserRole(int code, String authority, String label) {
        this.code = code;
        this.authority = authority;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getAuthority() {
        return authority;
    }

    public String getLabel() {
        return label;
    }

    public GrantedAuthority toGrantedAuthority() {
        return new SimpleGrantedAuthority(authority);
    }

    public boolean hasAccessAtLeast(UserRole requiredRole) {
        return this.code <= requiredRole.code;
    }

    public static UserRole fromCode(Integer code) {
        if (code == null) {
            return REQUESTER;
        }
        return Arrays.stream(values())
                .filter(role -> role.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role code: " + code));
    }

    public static List<UserRole> orderedByPrivilege() {
        return List.of(ADMIN, SUPERVISOR, TECHNICIAN, REQUESTER);
    }
}
