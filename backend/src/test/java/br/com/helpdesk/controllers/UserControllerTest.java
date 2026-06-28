package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.dtos.UserUpsertRequest;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.security.CurrentUserService;
import br.com.helpdesk.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserController controller;

    private User adminUser;
    private Authentication authentication;
    private UserResponse userResponse;
    private UserUpsertRequest upsertRequest;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setName("Admin");
        adminUser.setEmail("admin@helpdesk.com");
        adminUser.setRole(3);

        authentication = mock(Authentication.class);

        userResponse = new UserResponse(2L, "Carlos", "carlos@helpdesk.com", true, 1,
                LocalDateTime.now(), LocalDateTime.now());

        upsertRequest = new UserUpsertRequest("Carlos", "carlos@helpdesk.com", "senha123", 1, true);
    }

    @Test
    void listUsers_returnsListFromService() {
        when(userService.listUsers()).thenReturn(List.of(userResponse));

        List<UserResponse> result = controller.listUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Carlos");
    }

    @Test
    void listUsers_callsServiceListUsers() {
        when(userService.listUsers()).thenReturn(List.of());

        controller.listUsers();

        verify(userService).listUsers();
    }

    @Test
    void listTechnicians_returnsListFromService() {
        UserResponse technician = new UserResponse(3L, "Lucas", "lucas@helpdesk.com", true, 2,
                LocalDateTime.now(), LocalDateTime.now());
        when(userService.listTechnicians()).thenReturn(List.of(technician));

        List<UserResponse> result = controller.listTechnicians();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Lucas");
        assertThat(result.get(0).role()).isEqualTo(2);
        verify(userService).listTechnicians();
    }

    @Test
    void getUser_returnsUserFromService() {
        when(userService.getUser(2L)).thenReturn(userResponse);

        UserResponse result = controller.getUser(2L);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.email()).isEqualTo("carlos@helpdesk.com");
        verify(userService).getUser(2L);
    }

    @Test
    void createUser_returnsCreatedUserFromService() {
        when(currentUserService.requireUser(authentication)).thenReturn(adminUser);
        when(userService.createUser(upsertRequest, adminUser)).thenReturn(userResponse);

        UserResponse result = controller.createUser(upsertRequest, authentication);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Carlos");
        verify(userService).createUser(upsertRequest, adminUser);
    }

    @Test
    void createUser_callsCurrentUserService() {
        when(currentUserService.requireUser(authentication)).thenReturn(adminUser);
        when(userService.createUser(upsertRequest, adminUser)).thenReturn(userResponse);

        controller.createUser(upsertRequest, authentication);

        verify(currentUserService).requireUser(authentication);
    }

    @Test
    void updateUser_returnsUpdatedUserFromService() {
        UserResponse updated = new UserResponse(2L, "Carlos Atualizado", "carlos@helpdesk.com", true, 1,
                LocalDateTime.now(), LocalDateTime.now());
        UserUpsertRequest updateRequest = new UserUpsertRequest("Carlos Atualizado", "carlos@helpdesk.com", null, 1, true);
        when(currentUserService.requireUser(authentication)).thenReturn(adminUser);
        when(userService.updateUser(2L, updateRequest, adminUser)).thenReturn(updated);

        UserResponse result = controller.updateUser(2L, updateRequest, authentication);

        assertThat(result.name()).isEqualTo("Carlos Atualizado");
        verify(userService).updateUser(2L, updateRequest, adminUser);
    }

    @Test
    void deleteUser_callsServiceWithCorrectId() {
        when(currentUserService.requireUser(authentication)).thenReturn(adminUser);

        controller.deleteUser(2L, authentication);

        verify(userService).deleteUser(2L, adminUser);
    }

    @Test
    void deleteUser_callsCurrentUserService() {
        when(currentUserService.requireUser(authentication)).thenReturn(adminUser);

        controller.deleteUser(2L, authentication);

        verify(currentUserService).requireUser(authentication);
    }
}
