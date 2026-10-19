package com.tecnocampus.LS2.protube_back.controller;

import com.tecnocampus.LS2.protube_back.services.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void validationErrorsReturn400WithOneMessagePerField() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(),
                "registerRequest");
        bindingResult.addError(new FieldError("registerRequest", "email", "Enter a valid email address"));
        bindingResult.addError(new FieldError("registerRequest", "email", "Email is required"));
        bindingResult.addError(new FieldError("registerRequest", "password", "Password must be between 8 and 72 characters"));
                MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid data", response.getBody().get("message"));
        assertEquals(Map.of(
                        "email", "Enter a valid email address",
                        "password", "Password must be between 8 and 72 characters"),
                response.getBody().get("errors"));
    }
    @Test
    void duplicateUserReturns409WithMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleUserAlreadyExists(new UserAlreadyExistsException("This email is already registered"));

                        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(Map.of("message", "This email is already registered"), response.getBody());
    }
}