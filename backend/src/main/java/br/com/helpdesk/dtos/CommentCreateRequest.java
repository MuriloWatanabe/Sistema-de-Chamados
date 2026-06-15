package br.com.helpdesk.dtos;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        @NotBlank String comment
) {
}
