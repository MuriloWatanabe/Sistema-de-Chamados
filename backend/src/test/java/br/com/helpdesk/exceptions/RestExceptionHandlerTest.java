package br.com.helpdesk.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestExceptionHandlerTest {

    private RestExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    void handleNotFound_returns404WithCorrectBody() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Chamado não encontrado.");

        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("Chamado não encontrado.");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleBadRequest_withBusinessRuleException_returns400() {
        BusinessRuleException ex = new BusinessRuleException("Chamado já está fechado.");

        ResponseEntity<ApiErrorResponse> response = handler.handleBadRequest(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("Chamado já está fechado.");
    }

    @Test
    void handleBadRequest_withInvalidCredentialsException_returns401() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Email ou senha inválidos.");

        ResponseEntity<ApiErrorResponse> response = handler.handleBadRequest(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Email ou senha inválidos.");
    }

    @Test
    void handleForbidden_returns403WithCorrectBody() {
        ForbiddenOperationException ex = new ForbiddenOperationException("Operação não permitida.");

        ResponseEntity<ApiErrorResponse> response = handler.handleForbidden(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().error()).isEqualTo("Forbidden");
        assertThat(response.getBody().message()).isEqualTo("Operação não permitida.");
    }

    @Test
    void handleValidation_returns400WithFieldErrorMessages() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("ticketCreateRequest", "title", "não deve estar em branco");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("title: não deve estar em branco");
    }

    @Test
    void handleValidation_withMultipleFieldErrors_joinsWithSemicolon() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("req", "title", "não deve estar em branco");
        FieldError error2 = new FieldError("req", "description", "não deve estar em branco");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("title: não deve estar em branco");
        assertThat(response.getBody().message()).contains("description: não deve estar em branco");
        assertThat(response.getBody().message()).contains(";");
    }

    @Test
    void handleValidation_withNoFieldErrors_returnsFallbackMessage() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Validation error.");
    }

    @Test
    void handleGeneric_returns500WithExceptionMessage() {
        Exception ex = new Exception("Erro interno inesperado.");

        ResponseEntity<ApiErrorResponse> response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message()).isEqualTo("Unexpected error: Erro interno inesperado.");
    }

    @Test
    void responseBody_alwaysIncludesRequestPath() {
        request.setRequestURI("/api/users/5");
        ResourceNotFoundException ex = new ResourceNotFoundException("Usuário não encontrado.");

        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(ex, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().path()).isEqualTo("/api/users/5");
    }
}
