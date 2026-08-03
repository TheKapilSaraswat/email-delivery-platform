package com.emailplatform.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleRuntime() {
        RuntimeException ex = new RuntimeException("Test error");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntime(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Test error", response.getBody().get("error"));
    }

    @Test
    void testHandleRuntimeWithMessage() {
        RuntimeException ex = new RuntimeException("Email already registered");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntime(ex);

        assertEquals("Email already registered", response.getBody().get("error"));
    }

    @Test
    void testHandleRuntimeEmptyMessage() {
        RuntimeException ex = new RuntimeException((String) null);
        ResponseEntity<Map<String, Object>> response = handler.handleRuntime(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody().get("error"));
    }

    @Test
    void testHandleGeneralException() {
        Exception ex = new Exception("General error");
        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred. Please try again.", response.getBody().get("error"));
    }

    @Test
    void testHandleGeneralExceptionDoesNotLeakDetails() {
        Exception ex = new Exception("Sensitive database info");
        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertFalse(((String) response.getBody().get("error")).contains("database"));
        assertEquals("An unexpected error occurred. Please try again.", response.getBody().get("error"));
    }

    @Test
    void testHandleValidationException() {
        BindingResult bindingResult = new MapBindingResult(new java.util.HashMap<>(), "contactRequest");
        bindingResult.rejectValue("email", "NotBlank", "must not be blank");

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().get("error"));
        Map<?, ?> fields = (Map<?, ?>) response.getBody().get("fields");
        assertTrue(fields.containsKey("email"));
        assertEquals("must not be blank", fields.get("email"));
    }

    @Test
    void testHandleValidationMultipleErrors() {
        BindingResult bindingResult = new MapBindingResult(new java.util.HashMap<>(), "request");
        bindingResult.rejectValue("email", "Email", "invalid email");
        bindingResult.rejectValue("name", "NotBlank", "must not be blank");

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?, ?> fields = (Map<?, ?>) response.getBody().get("fields");
        assertTrue(fields.containsKey("email"));
        assertTrue(fields.containsKey("name"));
    }

    @Test
    void testHandleRuntimeExceptionWithLongMessage() {
        String longMsg = "A".repeat(1000);
        RuntimeException ex = new RuntimeException(longMsg);
        ResponseEntity<Map<String, Object>> response = handler.handleRuntime(ex);

        assertEquals(longMsg, response.getBody().get("error"));
    }

    @Test
    void testHandleGeneralExceptionWithNullMessage() {
        Exception ex = new NullPointerException();
        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertEquals("An unexpected error occurred. Please try again.", response.getBody().get("error"));
    }
}
