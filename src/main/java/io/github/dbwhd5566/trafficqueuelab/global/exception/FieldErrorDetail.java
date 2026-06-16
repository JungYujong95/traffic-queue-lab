package io.github.dbwhd5566.trafficqueuelab.global.exception;

public record FieldErrorDetail(
        String field,
        String rejectedValue,
        String reason
) {
}
