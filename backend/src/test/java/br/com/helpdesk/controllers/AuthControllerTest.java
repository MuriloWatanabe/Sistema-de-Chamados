package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.LoginRequest;
import br.com.helpdesk.dtos.LoginResponse;
import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.security.CurrentUserService;
import br.com.helpdesk.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AuthController controller;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("João Silva");
        user.setEmail("joao@helpdesk.com");
        user.setPassword("hashed");
        user.setRole(1);
        user.setActive(true);

        loginRequest = new LoginRequest("joao@helpdesk.com", "senha123");
    }

    @Test
    void login_returnsTokenFromService() {
        UserResponse userResponse = new UserResponse(1L, "João Silva", "joao@helpdesk.com", true, 1, LocalDateTime.now(), LocalDateTime.now());
        LoginResponse loginResponse = new LoginResponse("jwt-token-abc", "Bearer", userResponse);
        when(authService.login(loginRequest)).thenReturn(loginResponse);

        LoginResponse result = controller.login(loginRequest);

        assertThat(result.token()).isEqualTo("jwt-token-abc");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.user().email()).isEqualTo("joao@helpdesk.com");
    }

    @Test
    void login_callsAuthServiceWithRequest() {
        UserResponse userResponse = new UserResponse(1L, "João Silva", "joao@helpdesk.com", true, 1, LocalDateTime.now(), LocalDateTime.now());
        when(authService.login(loginRequest)).thenReturn(new LoginResponse("token", "Bearer", userResponse));

        controller.login(loginRequest);

        verify(authService).login(loginRequest);
    }

    @Test
    void me_returnsCurrentUserMappedToResponse() {
        Authentication authentication = mock(Authentication.class);
        when(currentUserService.requireUser(authentication)).thenReturn(user);

        UserResponse result = controller.me(authentication);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("João Silva");
        assertThat(result.email()).isEqualTo("joao@helpdesk.com");
        assertThat(result.role()).isEqualTo(1);
        assertThat(result.active()).isTrue();
    }

    @Test
    void me_callsCurrentUserService() {
        Authentication authentication = mock(Authentication.class);
        when(currentUserService.requireUser(authentication)).thenReturn(user);

        controller.me(authentication);

        verify(currentUserService).requireUser(authentication);
    }
}
