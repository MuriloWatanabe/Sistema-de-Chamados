package br.com.helpdesk.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TicketUpdateRequest(
        @Min(0) @Max(3) Integer status,
        @Min(0) @Max(3) Integer priority,
        Long assignedToId
) {
}
