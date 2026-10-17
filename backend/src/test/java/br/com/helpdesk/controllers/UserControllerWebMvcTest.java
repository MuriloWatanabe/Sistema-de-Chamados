package br.com.helpdesk.controllers;

import br.com.helpdesk.config.SecurityConfig;
import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.dtos.UserUpsertRequest;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.exceptions.BusinessRuleException;
import br.com.helpdesk.exceptions.ForbiddenOperationException;
import br.com.helpdesk.security.CurrentUserService;
import br.com.helpdesk.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, CurrentUserService.class})
class UserControllerWebMvcTest extends WebMvcSecurityTestSupport {

    @MockitoBean
    private UserService userService;

    private final User admin = user(1L, UserRole.ADMIN);
    private final User supervisor = user(2L, UserRole.SUPERVISOR);
    private final User technician = user(3L, UserRole.TECHNICIAN);
    private final User requester = user(4L, UserRole.REQUESTER);

    private static final String NEW_USER = """
            {"name": "Maria Santos", "email": "maria@helpdesk.com", "password": "Senha@123", "role": 3, "active": true}
            """;

    @Test
    void requesterListingUsers_returns403() throws Exception {
        mockMvc.perform(get("/api/users").with(authenticatedAs(requester)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void technicianListingUsers_returns200() throws Exception {
        when(userService.listUsers()).thenReturn(List.of(userResponse(4L, 3)));

        mockMvc.perform(get("/api/users").with(authenticatedAs(technician)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4));
    }

    @Test
    void supervisorCreatingUser_returns403() throws Exception {
        mockMvc.perform(post("/api/users").with(authenticatedAs(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_USER))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void adminCreatingUser_returns201() throws Exception {
        when(userService.createUser(any(UserUpsertRequest.class), eq(admin))).thenReturn(userResponse(10L, 3));

        mockMvc.perform(post("/api/users").with(authenticatedAs(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_USER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void unknownRoleCode_returns400() throws Exception {
        mockMvc.perform(post("/api/users").with(authenticatedAs(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Maria\", \"email\": \"maria@helpdesk.com\", \"role\": 7, \"active\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("role")));

        verifyNoInteractions(userService);
    }

    @Test
    void invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/users").with(authenticatedAs(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Maria\", \"email\": \"nao-e-email\", \"role\": 3, \"active\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("email")));
    }

    @Test
    void duplicatedEmail_returns400FromBusinessRule() throws Exception {
        when(userService.createUser(any(UserUpsertRequest.class), eq(admin)))
                .thenThrow(new BusinessRuleException("Email already registered: maria@helpdesk.com"));

        mockMvc.perform(post("/api/users").with(authenticatedAs(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_USER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already registered: maria@helpdesk.com"));
    }

    @Test
    void supervisorPromotingUser_isRejectedWith403ByService() throws Exception {
        when(userService.updateUser(eq(4L), any(UserUpsertRequest.class), eq(supervisor)))
                .thenThrow(new ForbiddenOperationException("Only administrators can change user roles."));

        mockMvc.perform(put("/api/users/{id}", 4L).with(authenticatedAs(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Maria\", \"email\": \"maria@helpdesk.com\", \"role\": 0, \"active\": true}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only administrators can change user roles."));
    }

    @Test
    void technicianUpdatingUser_returns403() throws Exception {
        mockMvc.perform(put("/api/users/{id}", 4L).with(authenticatedAs(technician))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_USER))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void adminDeletingUser_returns204() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 4L).with(authenticatedAs(admin)))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(4L, admin);
    }

    @Test
    void deletingUserWithLinkedRecords_returns400() throws Exception {
        doThrow(new BusinessRuleException("User has linked records and cannot be deleted."))
                .when(userService).deleteUser(4L, admin);

        mockMvc.perform(delete("/api/users/{id}", 4L).with(authenticatedAs(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void supervisorDeletingUser_returns403() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 4L).with(authenticatedAs(supervisor)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    private UserResponse userResponse(Long id, int role) {
        return new UserResponse(id, "Maria Santos", "maria@helpdesk.com", true, role, LocalDateTime.now(), LocalDateTime.now());
    }
}
