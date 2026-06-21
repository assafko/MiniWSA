package com.miniwsa.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @Test
    void handleIllegalArgumentException_returnsBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<Object> resp = handler.handleIllegalArgumentException(new IllegalArgumentException("x"), mock(WebRequest.class));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(((Map<?, ?>) resp.getBody()).get("message").toString().contains("x"));
    }

    @Test
    void handleGlobalException_returnsInternalServerError() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<Object> resp = handler.handleGlobalException(new RuntimeException("boom"), mock(WebRequest.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
    }

    @Test
    void handleValidationExceptions_returnsBadRequestWithErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "field", "must not be blank"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Object> resp = handler.handleValidationExceptions(ex, mock(WebRequest.class));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) resp.getBody();
        assertNotNull(body.get("errors"));
        Map<?, ?> errors = (Map<?, ?>) body.get("errors");
        assertEquals("must not be blank", errors.get("field"));
    }
}

