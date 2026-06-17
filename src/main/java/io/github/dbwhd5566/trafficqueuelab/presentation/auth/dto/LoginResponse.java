package io.github.dbwhd5566.trafficqueuelab.presentation.auth.dto;

import io.github.dbwhd5566.trafficqueuelab.application.auth.result.LoginResult;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        Long accountId,
        String email,
        String nickname
) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresInSeconds(),
                result.accountId(),
                result.email(),
                result.nickname()
        );
    }
}
