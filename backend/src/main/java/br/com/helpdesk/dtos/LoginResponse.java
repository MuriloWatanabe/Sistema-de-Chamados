package br.com.helpdesk.dtos;

public record LoginResponse(
        String token,
        String tokenType,
        UserResponse user
) {
}
