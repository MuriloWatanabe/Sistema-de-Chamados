package br.com.helpdesk.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpsertRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 255) String password,
        @NotNull Integer role,
        @NotNull Boolean active
) {
}
