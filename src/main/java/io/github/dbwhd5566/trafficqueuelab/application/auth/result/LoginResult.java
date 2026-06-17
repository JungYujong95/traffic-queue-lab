package io.github.dbwhd5566.trafficqueuelab.application.auth.result;

public record LoginResult(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        Long accountId,
        String email,
        String nickname
) {
}
