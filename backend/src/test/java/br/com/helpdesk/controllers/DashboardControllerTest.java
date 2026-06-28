package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.DashboardStatsResponse;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.security.CurrentUserService;
import br.com.helpdesk.services.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private DashboardController controller;

    private User user;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Supervisor");
        user.setEmail("supervisor@helpdesk.com");
        user.setRole(2);

        authentication = mock(Authentication.class);
    }

    @Test
    void stats_returnsStatsFromService() {
        DashboardStatsResponse stats = new DashboardStatsResponse(10, 3, 4, 2, 1, 2);
        when(currentUserService.requireUser(authentication)).thenReturn(user);
        when(ticketService.getDashboardStats(user)).thenReturn(stats);

        DashboardStatsResponse result = controller.stats(authentication);

        assertThat(result.total()).isEqualTo(10);
        assertThat(result.open()).isEqualTo(3);
        assertThat(result.inProgress()).isEqualTo(4);
        assertThat(result.resolved()).isEqualTo(2);
        assertThat(result.closed()).isEqualTo(1);
        assertThat(result.urgent()).isEqualTo(2);
    }

    @Test
    void stats_callsCurrentUserServiceAndTicketService() {
        DashboardStatsResponse stats = new DashboardStatsResponse(0, 0, 0, 0, 0, 0);
        when(currentUserService.requireUser(authentication)).thenReturn(user);
        when(ticketService.getDashboardStats(user)).thenReturn(stats);

        controller.stats(authentication);

        verify(currentUserService).requireUser(authentication);
        verify(ticketService).getDashboardStats(user);
    }

    @Test
    void stats_withZeroTickets_returnsAllZeros() {
        DashboardStatsResponse stats = new DashboardStatsResponse(0, 0, 0, 0, 0, 0);
        when(currentUserService.requireUser(authentication)).thenReturn(user);
        when(ticketService.getDashboardStats(user)).thenReturn(stats);

        DashboardStatsResponse result = controller.stats(authentication);

        assertThat(result.total()).isZero();
        assertThat(result.open()).isZero();
        assertThat(result.urgent()).isZero();
    }
}
