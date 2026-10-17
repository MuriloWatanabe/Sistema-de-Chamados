package br.com.helpdesk.controllers;

import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.repositories.UserRepository;
import br.com.helpdesk.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.when;

/**
 * Base dos testes HTTP dos controllers. As subclasses usam @WebMvcTest + @Import(SecurityConfig,
 * CurrentUserService), entao cada requisicao passa pelo TokenAuthenticationFilter real, pelo
 * {@code @PreAuthorize} e pelo RestExceptionHandler. So o JwtService e o UserRepository sao mockados.
 */
abstract class WebMvcSecurityTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected UserRepository userRepository;

    /** Envia a requisicao com um Bearer token valido para o usuario informado. */
    protected RequestPostProcessor authenticatedAs(User user) {
        String token = "token-" + user.getId();
        long now = Instant.now().getEpochSecond();
        when(jwtService.validateAndParse(token)).thenReturn(new JwtService.TokenClaims(
                user.getId(), user.getName(), user.getEmail(), UserRole.fromCode(user.getRole()), now, now + 3600,
                user.getTokenVersion(), "jti-" + user.getId()));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            return request;
        };
    }

    protected static User user(long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setName(role.getLabel() + " " + id);
        user.setEmail(role.name().toLowerCase() + id + "@helpdesk.com");
        user.setRole(role.getCode());
        user.setActive(true);
        return user;
    }
}
