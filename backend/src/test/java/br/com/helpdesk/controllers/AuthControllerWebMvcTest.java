package br.com.helpdesk.controllers;

import br.com.helpdesk.config.SecurityConfig;
import br.com.helpdesk.dtos.LoginRequest;
import br.com.helpdesk.dtos.LoginResponse;
import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.exceptions.InvalidCredentialsException;
import br.com.helpdesk.security.CurrentUserService;
import br.com.helpdesk.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, HealthController.class})
@Import({SecurityConfig.class, CurrentUserService.class})
class AuthControllerWebMvcTest extends WebMvcSecurityTestSupport {

    @MockitoBean
    private AuthService authService;

    @Test
    void login_isPublicAndReturnsToken() throws Exception {
        UserResponse userResponse = new UserResponse(1L, "Admin", "admin@helpdesk.com", true, 0, LocalDateTime.now(), LocalDateTime.now());
        when(authService.login(new LoginRequest("admin@helpdesk.com", "admin123")))
                .thenReturn(new LoginResponse("jwt-token", "Bearer", userResponse));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"admin@helpdesk.com\", \"password\": \"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        when(authService.login(new LoginRequest("admin@helpdesk.com", "errada")))
                .thenThrow(new InvalidCredentialsException("Invalid e-mail or password."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"admin@helpdesk.com\", \"password\": \"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid e-mail or password."));
    }

    @Test
    void login_withInvalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"admin\", \"password\": \"admin123\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_returnsAuthenticatedUser() throws Exception {
        User technician = user(7L, UserRole.TECHNICIAN);
        when(authService.describe(technician)).thenReturn(new UserResponse(
                7L, technician.getName(), technician.getEmail(), true, UserRole.TECHNICIAN.getCode(), LocalDateTime.now(), LocalDateTime.now()));

        mockMvc.perform(get("/api/auth/me").with(authenticatedAs(technician)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.role").value(UserRole.TECHNICIAN.getCode()));
    }

    @Test
    void health_isPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
