package br.com.helpdesk.security;

import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.exceptions.ForbiddenOperationException;
import br.com.helpdesk.exceptions.ResourceNotFoundException;
import br.com.helpdesk.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    private AuthenticatedUser authenticatedUser;
    private User user;

    @BeforeEach
    void setUp() {
        authenticatedUser = new AuthenticatedUser(1L, "João Silva", "joao@helpdesk.com", UserRole.TECHNICIAN);

        user = new User();
        user.setId(1L);
        user.setName("João Silva");
        user.setEmail("joao@helpdesk.com");
        user.setRole(UserRole.TECHNICIAN.getCode());
        user.setActive(true);
    }

    // --- requirePrincipal ---

    @Test
    void requirePrincipal_returnsAuthenticatedUserWhenPresentInAuthentication() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);

        AuthenticatedUser result = currentUserService.requirePrincipal(authentication);

        assertThat(result).isEqualTo(authenticatedUser);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("joao@helpdesk.com");
    }

    @Test
    void requirePrincipal_throwsForbiddenWhenAuthenticationIsNull() {
        assertThatThrownBy(() -> currentUserService.requirePrincipal(null))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requirePrincipal_throwsForbiddenWhenPrincipalIsNotAuthenticatedUser() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("not-an-authenticated-user");

        assertThatThrownBy(() -> currentUserService.requirePrincipal(authentication))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requirePrincipal_throwsForbiddenWhenPrincipalIsNull() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(null);

        assertThatThrownBy(() -> currentUserService.requirePrincipal(authentication))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    // --- requireUser ---

    @Test
    void requireUser_returnsUserFromRepositoryWhenAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = currentUserService.requireUser(authentication);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("joao@helpdesk.com");
    }

    @Test
    void requireUser_throwsResourceNotFoundWhenUserDoesNotExistInDatabase() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentUserService.requireUser(authentication))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void requireUser_throwsForbiddenWhenAuthenticationIsNull() {
        assertThatThrownBy(() -> currentUserService.requireUser(null))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireUser_throwsForbiddenWhenPrincipalIsNotAuthenticatedUser() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("string-principal");

        assertThatThrownBy(() -> currentUserService.requireUser(authentication))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
