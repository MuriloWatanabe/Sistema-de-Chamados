package br.com.helpdesk.services;

import br.com.helpdesk.dtos.LoginRequest;
import br.com.helpdesk.dtos.LoginResponse;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.exceptions.InvalidCredentialsException;
import br.com.helpdesk.repositories.UserRepository;
import br.com.helpdesk.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("João Silva");
        user.setEmail("joao@helpdesk.com");
        user.setPassword("hashed_password");
        user.setRole(3);
        user.setActive(true);

        loginRequest = new LoginRequest("joao@helpdesk.com", "senha123");
    }

    @Test
    void login_returnsLoginResponseOnSuccess() {
        when(userRepository.findByEmailIgnoreCase("joao@helpdesk.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResponse result = authService.login(loginRequest);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.user().email()).isEqualTo("joao@helpdesk.com");
    }

    @Test
    void login_throwsInvalidCredentialsWhenUserNotFound() {
        when(userRepository.findByEmailIgnoreCase("joao@helpdesk.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_throwsInvalidCredentialsWhenUserIsInactive() {
        user.setActive(false);
        when(userRepository.findByEmailIgnoreCase("joao@helpdesk.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_throwsInvalidCredentialsWhenPasswordDoesNotMatch() {
        when(userRepository.findByEmailIgnoreCase("joao@helpdesk.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_recordsAuditOnSuccess() {
        when(userRepository.findByEmailIgnoreCase("joao@helpdesk.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        authService.login(loginRequest);

        verify(auditService).record(eq(user), eq("AUTH_LOGIN"), any(), eq(user.getId()), eq(null), any());
    }

    @Test
    void login_callsJwtServiceToGenerateToken() {
        when(userRepository.findByEmailIgnoreCase("joao@helpdesk.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        authService.login(loginRequest);

        verify(jwtService).generateToken(user);
    }

    @Test
    void login_throwsInvalidCredentialsWhenActiveIsNull() {
        user.setActive(null);
        when(userRepository.findByEmailIgnoreCase("joao@helpdesk.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
