package br.com.helpdesk.security;

import br.com.helpdesk.entities.User;
import br.com.helpdesk.exceptions.ForbiddenOperationException;
import br.com.helpdesk.exceptions.ResourceNotFoundException;
import br.com.helpdesk.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireUser(Authentication authentication) {
        AuthenticatedUser principal = requirePrincipal(authentication);
        return userRepository.findById(principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));
    }

    public AuthenticatedUser requirePrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new ForbiddenOperationException("Authentication required.");
        }
        return principal;
    }
}
