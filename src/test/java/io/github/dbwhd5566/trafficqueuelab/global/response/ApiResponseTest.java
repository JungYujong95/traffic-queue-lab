package io.github.dbwhd5566.trafficqueuelab.global.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void successWithDataCreatesSuccessfulResponse() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertTrue(response.success());
        assertEquals("요청이 성공했습니다.", response.message());
        assertEquals("ok", response.data());
    }

    @Test
    void successWithoutDataCreatesSuccessfulResponse() {
        ApiResponse<Void> response = ApiResponse.empty();

        assertTrue(response.success());
        assertEquals("요청이 성공했습니다.", response.message());
        assertNull(response.data());
    }
}
