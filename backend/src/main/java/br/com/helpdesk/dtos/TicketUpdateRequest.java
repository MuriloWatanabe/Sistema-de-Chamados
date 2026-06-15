package br.com.helpdesk.dtos;

public record TicketUpdateRequest(
        Integer status,
        Integer priority,
        Long assignedToId
) {
}
