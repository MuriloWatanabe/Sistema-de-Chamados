package br.com.helpdesk.dtos;

public record DashboardStatsResponse(
        long total,
        long open,
        long inProgress,
        long resolved,
        long closed,
        long urgent
) {
}
