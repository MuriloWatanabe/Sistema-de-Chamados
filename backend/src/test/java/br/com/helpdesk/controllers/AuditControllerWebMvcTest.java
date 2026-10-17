package br.com.helpdesk.controllers;

import br.com.helpdesk.config.SecurityConfig;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.security.CurrentUserService;
import br.com.helpdesk.services.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
@Import({SecurityConfig.class, CurrentUserService.class})
class AuditControllerWebMvcTest extends WebMvcSecurityTestSupport {

    @MockitoBean
    private AuditService auditService;

    @Test
    void technicianListingAudits_returns403() throws Exception {
        mockMvc.perform(get("/api/audits").with(authenticatedAs(user(3L, UserRole.TECHNICIAN))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(auditService);
    }

    @Test
    void requesterListingAudits_returns403() throws Exception {
        mockMvc.perform(get("/api/audits").with(authenticatedAs(user(4L, UserRole.REQUESTER))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(auditService);
    }

    @Test
    void supervisorListingAudits_returns200() throws Exception {
        when(auditService.listAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/audits").with(authenticatedAs(user(2L, UserRole.SUPERVISOR))))
                .andExpect(status().isOk());
    }
}
