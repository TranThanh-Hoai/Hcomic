package com.comic.h.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.core.TypeInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.comic.h.common.dto.response.ErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/comics");
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST when InvalidDataAccessApiUsageException is thrown")
    void handleInvalidDataAccessApiUsageException() {
        String errorMessage = "Sort expression '[\"string\"]: ASC' must only contain property references";
        InvalidDataAccessApiUsageException ex = new InvalidDataAccessApiUsageException(errorMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequestException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(response.getBody().getPath()).isEqualTo("/api/comics");
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST when PropertyReferenceException is thrown")
    void handlePropertyReferenceException() {
        PropertyReferenceException ex = new PropertyReferenceException(
                "invalidProperty",
                TypeInformation.of(String.class),
                java.util.Collections.emptyList()
        );

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequestException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("invalidProperty");
        assertThat(response.getBody().getPath()).isEqualTo("/api/comics");
    }
}
