package io.github.dbwhd5566.trafficqueuelab.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {

    private static final String SUCCESS_MESSAGE = "요청이 성공했습니다.";

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, SUCCESS_MESSAGE, data);
    }

    public static ApiResponse<Void> empty() {
        return new ApiResponse<>(true, SUCCESS_MESSAGE, null);
    }
}
