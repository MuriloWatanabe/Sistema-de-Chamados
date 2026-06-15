package br.com.helpdesk.dtos;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Boolean active,
        Integer role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
