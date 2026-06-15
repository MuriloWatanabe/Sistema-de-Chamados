package br.com.helpdesk.dtos;

public record UserSummaryResponse(
        Long id,
        String name,
        String email,
        Integer role
) {
}
