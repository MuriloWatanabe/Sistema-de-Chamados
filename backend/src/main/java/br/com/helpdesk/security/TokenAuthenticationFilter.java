package br.com.helpdesk.security;

import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public TokenAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                JwtService.TokenClaims claims = jwtService.validateAndParse(token);
                userRepository.findById(claims.userId())
                        .filter(user -> Boolean.TRUE.equals(user.getActive()))
                        .ifPresent(user -> {
                            UserRole currentRole = UserRole.fromCode(user.getRole());
                            if (claims.role() != currentRole) {
                                throw new JwtService.InvalidTokenException("Token role mismatch");
                            }
                            AuthenticatedUser principal = new AuthenticatedUser(
                                    user.getId(),
                                    user.getName(),
                                    user.getEmail(),
                                    currentRole
                            );
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            principal,
                                            null,
                                            principal.authorities()
                                    );
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            } catch (JwtService.InvalidTokenException ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
