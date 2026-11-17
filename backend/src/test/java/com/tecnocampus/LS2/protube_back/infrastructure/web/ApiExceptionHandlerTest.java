package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.domain.exception.InvalidVideoException;
import com.tecnocampus.LS2.protube_back.domain.exception.UserAlreadyExistsException;
import com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoAccessDeniedException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoStorageException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void badCredentialsReturn401WithGenericMessage() {
        ResponseEntity<Map<String, Object>> response = handler.handleBadCredentials();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of("message", "Incorrect email or password"), response.getBody());
    }

    @Test
    void missingUserIsHandledAsUnauthorized() throws NoSuchMethodException {
        // A session whose user no longer exists gets the same 401 as bad credentials.
        ExceptionHandler mapping = ApiExceptionHandler.class.getMethod("handleBadCredentials")
                .getAnnotation(ExceptionHandler.class);

        assertTrue(List.of(mapping.value()).contains(UserNotFoundException.class));
    }

    @Test
    void invalidVideoReturns400WithTheRuleBroken() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleInvalidVideo(new InvalidVideoException("Only .mp4 files are supported"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("message", "Only .mp4 files are supported"), response.getBody());
    }

    @Test
    void missingUploadFieldReturns400() {
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleMissingUploadField().getStatusCode());
    }

    @Test
    void tooLargeUploadReturns413() {
        ResponseEntity<Map<String, Object>> response = handler.handleTooLarge();

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertEquals("The video file must be 100 MB or smaller", response.getBody().get("message"));
    }

    @Test
    void changingSomeoneElsesVideoReturns403() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleVideoAccessDenied(new VideoAccessDeniedException(5L));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void storageFailureReturns500WithoutTheInternalDetail() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleVideoStorage(new VideoStorageException("C:/secret/path", new RuntimeException()));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(Map.of("message", "The video could not be saved. Try again later."), response.getBody());
    }
}
