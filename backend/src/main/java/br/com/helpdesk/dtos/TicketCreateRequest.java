package br.com.helpdesk.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String description,
        @NotNull Integer priority,
        Long requesterId,
        Long assignedToId
) {
}
