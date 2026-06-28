package br.com.helpdesk.services;

import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.dtos.UserUpsertRequest;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.exceptions.BusinessRuleException;
import br.com.helpdesk.exceptions.ResourceNotFoundException;
import br.com.helpdesk.repositories.AuditRepository;
import br.com.helpdesk.repositories.CommentRepository;
import br.com.helpdesk.repositories.TicketRepository;
import br.com.helpdesk.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private User admin;
    private User technician;
    private User requester;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setName("Admin User");
        admin.setEmail("admin@helpdesk.com");
        admin.setPassword("hashed");
        admin.setRole(UserRole.ADMIN.getCode());
        admin.setActive(true);

        technician = new User();
        technician.setId(2L);
        technician.setName("Tech User");
        technician.setEmail("tech@helpdesk.com");
        technician.setPassword("hashed");
        technician.setRole(UserRole.TECHNICIAN.getCode());
        technician.setActive(true);

        requester = new User();
        requester.setId(3L);
        requester.setName("Requester User");
        requester.setEmail("requester@helpdesk.com");
        requester.setPassword("hashed");
        requester.setRole(UserRole.REQUESTER.getCode());
        requester.setActive(true);
    }

    // --- listUsers ---

    @Test
    void listUsers_returnsAllUsersOrderedByName() {
        when(userRepository.findByOrderByNameAsc()).thenReturn(List.of(admin, requester, technician));

        List<UserResponse> result = userService.listUsers();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("Admin User");
        assertThat(result.get(1).name()).isEqualTo("Requester User");
    }

    @Test
    void listUsers_returnsEmptyListWhenNoUsers() {
        when(userRepository.findByOrderByNameAsc()).thenReturn(List.of());

        List<UserResponse> result = userService.listUsers();

        assertThat(result).isEmpty();
    }

    // --- listTechnicians ---

    @Test
    void listTechnicians_returnsActiveTechnicians() {
        when(userRepository.findByActiveTrueAndRoleOrderByNameAsc(UserRole.TECHNICIAN.getCode()))
                .thenReturn(List.of(technician));

        List<UserResponse> result = userService.listTechnicians();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("tech@helpdesk.com");
        assertThat(result.get(0).role()).isEqualTo(UserRole.TECHNICIAN.getCode());
    }

    @Test
    void listTechnicians_returnsEmptyListWhenNoActiveTechnicians() {
        when(userRepository.findByActiveTrueAndRoleOrderByNameAsc(UserRole.TECHNICIAN.getCode()))
                .thenReturn(List.of());

        List<UserResponse> result = userService.listTechnicians();

        assertThat(result).isEmpty();
    }

    // --- getUser ---

    @Test
    void getUser_returnsUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        UserResponse result = userService.getUser(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Admin User");
        assertThat(result.email()).isEqualTo("admin@helpdesk.com");
    }

    @Test
    void getUser_throwsResourceNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- findUser ---

    @Test
    void findUser_returnsUserEntity() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(technician));

        User result = userService.findUser(2L);

        assertThat(result).isEqualTo(technician);
    }

    @Test
    void findUser_throwsResourceNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- createUser ---

    @Test
    void createUser_savesAndReturnsUserResponse() {
        UserUpsertRequest request = new UserUpsertRequest("New User", "new@helpdesk.com", "Pass@123", UserRole.REQUESTER.getCode(), true);
        when(userRepository.existsByEmailIgnoreCase("new@helpdesk.com")).thenReturn(false);
        when(passwordEncoder.encode("Pass@123")).thenReturn("encoded_pass");
        User saved = new User();
        saved.setId(10L);
        saved.setName("New User");
        saved.setEmail("new@helpdesk.com");
        saved.setRole(UserRole.REQUESTER.getCode());
        saved.setActive(true);
        saved.setPassword("encoded_pass");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(saved);

        UserResponse result = userService.createUser(request, admin);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("New User");
        assertThat(result.email()).isEqualTo("new@helpdesk.com");
    }

    @Test
    void createUser_usesDefaultPasswordWhenNotProvided() {
        UserUpsertRequest request = new UserUpsertRequest("New User", "new@helpdesk.com", null, UserRole.REQUESTER.getCode(), true);
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Helpdesk@123")).thenReturn("encoded_default");
        User saved = new User();
        saved.setId(10L);
        saved.setName("New User");
        saved.setEmail("new@helpdesk.com");
        saved.setRole(UserRole.REQUESTER.getCode());
        saved.setActive(true);
        saved.setPassword("encoded_default");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(saved);

        userService.createUser(request, admin);

        verify(passwordEncoder).encode("Helpdesk@123");
    }

    @Test
    void createUser_throwsBusinessRuleWhenEmailAlreadyExists() {
        UserUpsertRequest request = new UserUpsertRequest("New User", "admin@helpdesk.com", null, UserRole.REQUESTER.getCode(), true);
        when(userRepository.existsByEmailIgnoreCase("admin@helpdesk.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request, admin))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("admin@helpdesk.com");

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void createUser_recordsAuditAfterSave() {
        UserUpsertRequest request = new UserUpsertRequest("New User", "new@helpdesk.com", "Pass@123", UserRole.REQUESTER.getCode(), true);
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        User saved = new User();
        saved.setId(10L);
        saved.setName("New User");
        saved.setEmail("new@helpdesk.com");
        saved.setRole(UserRole.REQUESTER.getCode());
        saved.setActive(true);
        saved.setPassword("encoded");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(saved);

        userService.createUser(request, admin);

        verify(auditService).record(eq(admin), eq("USER_CREATED"), any(), eq(10L), eq(null), any());
    }

    // --- updateUser ---

    @Test
    void updateUser_updatesAndReturnsUserResponse() {
        UserUpsertRequest request = new UserUpsertRequest("Updated Name", "requester@helpdesk.com", null, UserRole.REQUESTER.getCode(), true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("requester@helpdesk.com", 3L)).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(requester);

        UserResponse result = userService.updateUser(3L, request, admin);

        assertThat(result.id()).isEqualTo(3L);
        verify(userRepository).saveAndFlush(requester);
    }

    @Test
    void updateUser_throwsBusinessRuleWhenEmailTakenByAnotherUser() {
        UserUpsertRequest request = new UserUpsertRequest("Requester", "admin@helpdesk.com", null, UserRole.REQUESTER.getCode(), true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("admin@helpdesk.com", 3L)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(3L, request, admin))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateUser_encodesPasswordWhenProvided() {
        UserUpsertRequest request = new UserUpsertRequest("Requester", "requester@helpdesk.com", "NewPass@1", UserRole.REQUESTER.getCode(), true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(passwordEncoder.encode("NewPass@1")).thenReturn("new_encoded");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(requester);

        userService.updateUser(3L, request, admin);

        verify(passwordEncoder).encode("NewPass@1");
    }

    @Test
    void updateUser_doesNotEncodePasswordWhenNotProvided() {
        UserUpsertRequest request = new UserUpsertRequest("Requester", "requester@helpdesk.com", null, UserRole.REQUESTER.getCode(), true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(requester);

        userService.updateUser(3L, request, admin);

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateUser_recordsAuditAfterSave() {
        UserUpsertRequest request = new UserUpsertRequest("Updated Name", "requester@helpdesk.com", null, UserRole.REQUESTER.getCode(), true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(requester);

        userService.updateUser(3L, request, admin);

        verify(auditService).record(eq(admin), eq("USER_UPDATED"), any(), eq(3L), any(), any());
    }

    // --- deleteUser ---

    @Test
    void deleteUser_deletesUserWhenNoLinkedRecords() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));
        when(ticketRepository.existsByRequesterId(3L)).thenReturn(false);
        when(ticketRepository.existsByAssignedToId(3L)).thenReturn(false);
        when(commentRepository.existsByUserId(3L)).thenReturn(false);
        when(auditRepository.existsByUserId(3L)).thenReturn(false);

        userService.deleteUser(3L, admin);

        verify(userRepository).delete(requester);
    }

    @Test
    void deleteUser_throwsBusinessRuleWhenUserHasRequestedTickets() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));
        when(ticketRepository.existsByRequesterId(3L)).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteUser(3L, admin))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("linked records");

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_throwsBusinessRuleWhenUserHasAssignedTickets() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(technician));
        when(ticketRepository.existsByRequesterId(2L)).thenReturn(false);
        when(ticketRepository.existsByAssignedToId(2L)).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteUser(2L, admin))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_throwsBusinessRuleWhenUserHasComments() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));
        when(ticketRepository.existsByRequesterId(3L)).thenReturn(false);
        when(ticketRepository.existsByAssignedToId(3L)).thenReturn(false);
        when(commentRepository.existsByUserId(3L)).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteUser(3L, admin))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_throwsBusinessRuleWhenUserHasAuditEntries() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));
        when(ticketRepository.existsByRequesterId(3L)).thenReturn(false);
        when(ticketRepository.existsByAssignedToId(3L)).thenReturn(false);
        when(commentRepository.existsByUserId(3L)).thenReturn(false);
        when(auditRepository.existsByUserId(3L)).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteUser(3L, admin))
                .isInstanceOf(BusinessRuleException.class);

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_throwsResourceNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L, admin))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUser_recordsAuditAfterDeletion() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));
        when(ticketRepository.existsByRequesterId(3L)).thenReturn(false);
        when(ticketRepository.existsByAssignedToId(3L)).thenReturn(false);
        when(commentRepository.existsByUserId(3L)).thenReturn(false);
        when(auditRepository.existsByUserId(3L)).thenReturn(false);

        userService.deleteUser(3L, admin);

        verify(auditService).record(eq(admin), eq("USER_DELETED"), any(), eq(3L), any(), eq(null));
    }
}
