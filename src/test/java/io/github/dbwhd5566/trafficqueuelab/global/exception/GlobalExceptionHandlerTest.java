package io.github.dbwhd5566.trafficqueuelab.global.exception;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.sql.SQLTransientConnectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void businessExceptionReturnsErrorResponse() throws Exception {
        mockMvc.perform(get("/test/business-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    @Test
    void validationExceptionReturnsFieldErrors() throws Exception {
        mockMvc.perform(post("/test/validation-error")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void missingRequestParameterReturnsErrorResponse() throws Exception {
        mockMvc.perform(get("/test/missing-parameter"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.MISSING_REQUIRED_PARAMETER.getCode()));
    }

    @Test
    void cannotGetJdbcConnectionReturnsServiceUnavailable() throws Exception {
        mockMvc.perform(get("/test/db-connection-timeout"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.DB_CONNECTION_TIMEOUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.DB_CONNECTION_TIMEOUT.getMessage()));
    }

    @Test
    void transactionConnectionTimeoutReturnsServiceUnavailable() throws Exception {
        mockMvc.perform(get("/test/transaction-connection-timeout"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.DB_CONNECTION_TIMEOUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.DB_CONNECTION_TIMEOUT.getMessage()));
    }

    @RestController
    private static class TestController {

        @GetMapping("/test/business-error")
        void businessError() {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        @PostMapping("/test/validation-error")
        void validationError(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/missing-parameter")
        void missingParameter(@RequestParam String value) {
        }

        @GetMapping("/test/db-connection-timeout")
        void dbConnectionTimeout() {
            throw new CannotGetJdbcConnectionException("Connection is not available");
        }

        @GetMapping("/test/transaction-connection-timeout")
        void transactionConnectionTimeout() {
            throw new CannotCreateTransactionException(
                    "Could not open JPA EntityManager for transaction",
                    new SQLTransientConnectionException("HikariPool-1 - Connection is not available")
            );
        }
    }

    private record TestRequest(
            @NotBlank(message = "이름은 필수입니다.")
            String name
    ) {
    }
}
