package br.com.helpdesk.security;

import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TokenAuthenticationFilter filter;

    private User activeUser;
    private JwtService.TokenClaims claims;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setName("João Silva");
        activeUser.setEmail("joao@helpdesk.com");
        activeUser.setRole(UserRole.TECHNICIAN.getCode());
        activeUser.setActive(true);

        claims = new JwtService.TokenClaims(1L, "João Silva", "joao@helpdesk.com", UserRole.TECHNICIAN, 1000L, 9999999999L);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_setsAuthenticationForValidBearerToken() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
        when(jwtService.validateAndParse("valid-token")).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthenticatedUser.class);
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        assertThat(principal.id()).isEqualTo(1L);
        assertThat(principal.email()).isEqualTo("joao@helpdesk.com");
    }

    @Test
    void doFilterInternal_doesNotSetAuthWhenAuthorizationHeaderIsMissing() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).validateAndParse(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void doFilterInternal_doesNotSetAuthForNonBearerToken() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).validateAndParse(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void doFilterInternal_clearsContextWhenTokenIsInvalid() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer invalid-token");
        when(jwtService.validateAndParse("invalid-token"))
                .thenThrow(new JwtService.InvalidTokenException("Invalid token"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_doesNotSetAuthWhenUserIsInactive() throws Exception {
        activeUser.setActive(false);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
        when(jwtService.validateAndParse("valid-token")).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_doesNotSetAuthWhenUserNotFoundInDatabase() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
        when(jwtService.validateAndParse("valid-token")).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_clearsContextWhenTokenRoleDoesNotMatchCurrentUserRole() throws Exception {
        activeUser.setRole(UserRole.ADMIN.getCode());
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
        when(jwtService.validateAndParse("valid-token")).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_alwaysCallsFilterChainRegardlessOfAuthOutcome() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_callsFilterChainEvenWhenTokenIsInvalid() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer bad-token");
        when(jwtService.validateAndParse("bad-token"))
                .thenThrow(new JwtService.InvalidTokenException("Bad token"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_callsFilterChainWhenAuthenticationSucceeds() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
        when(jwtService.validateAndParse("valid-token")).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_setsCorrectRoleAuthorityInAuthentication() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
        when(jwtService.validateAndParse("valid-token")).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_TECHNICIAN");
    }
}
