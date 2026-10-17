package br.com.helpdesk.services;

import br.com.helpdesk.dtos.ChangePasswordRequest;
import br.com.helpdesk.dtos.LoginRequest;
import br.com.helpdesk.dtos.LoginResponse;
import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.exceptions.BusinessRuleException;
import br.com.helpdesk.exceptions.InvalidCredentialsException;
import br.com.helpdesk.exceptions.TooManyRequestsException;
import br.com.helpdesk.repositories.UserRepository;
import br.com.helpdesk.security.JwtService;
import br.com.helpdesk.security.LoginAttemptService;
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

    @Mock
    private LoginAttemptService loginAttemptService;

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
    void describe_mapsUserWithoutExposingPassword() {
        UserResponse result = authService.describe(user);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("joao@helpdesk.com");
        assertThat(result.role()).isEqualTo(3);
        assertThat(UserResponse.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .doesNotContain("password");
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

    // --- protecao contra forca bruta ---

    @Test
    void login_rejectsWhileEmailIsLocked() {
        when(loginAttemptService.remainingLockSeconds("joao@helpdesk.com")).thenReturn(600L);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(TooManyRequestsException.class);

        verify(userRepository, never()).findByEmailIgnoreCase(anyString());
    }

    @Test
    void login_registersFailureAndAuditsInvalidPassword() {
        when(userRepository.findByEmailIgnoreCase("joao@helpdesk.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(loginAttemptService).registerFailure("joao@helpdesk.com");
        verify(auditService).record(eq(user), eq("AUTH_LOGIN_FAILED"), any(), eq(1L), eq(null), any());
    }

    @Test
    void login_stillChecksPasswordHashWhenUserDoesNotExist() {
        when(userRepository.findByEmailIgnoreCase("joao@helpdesk.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("dummy_hash");

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder).matches("senha123", "dummy_hash");
        verify(loginAttemptService).registerFailure("joao@helpdesk.com");
    }

    @Test
    void login_clearsFailuresOnSuccess() {
        when(userRepository.findByEmailIgnoreCase("joao@helpdesk.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        authService.login(loginRequest);

        verify(loginAttemptService).registerSuccess("joao@helpdesk.com");
    }

    // --- logout e troca de senha ---

    @Test
    void logout_revokesTokensAndAudits() {
        authService.logout(user);

        assertThat(user.getTokenVersion()).isEqualTo(1);
        verify(userRepository).saveAndFlush(user);
        verify(auditService).record(eq(user), eq("AUTH_LOGOUT"), any(), eq(1L), eq(null), eq(null));
    }

    @Test
    void changePassword_rejectsWrongCurrentPassword() {
        when(passwordEncoder.matches("errada123", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(user, new ChangePasswordRequest("errada123", "NovaSenha1")))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void changePassword_rejectsSamePassword() {
        when(passwordEncoder.matches("senha123", "hashed_password")).thenReturn(true);

        assertThatThrownBy(() -> authService.changePassword(user, new ChangePasswordRequest("senha123", "senha123")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void changePassword_updatesHashRevokesOldTokensAndReturnsNewToken() {
        when(passwordEncoder.matches("senha123", "hashed_password")).thenReturn(true);
        when(passwordEncoder.encode("NovaSenha1")).thenReturn("new_hash");
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("new-token");

        LoginResponse result = authService.changePassword(user, new ChangePasswordRequest("senha123", "NovaSenha1"));

        assertThat(user.getPassword()).isEqualTo("new_hash");
        assertThat(user.getTokenVersion()).isEqualTo(1);
        assertThat(result.token()).isEqualTo("new-token");
        verify(auditService).record(eq(user), eq("AUTH_PASSWORD_CHANGED"), any(), eq(1L), eq(null), eq(null));
    }
}
