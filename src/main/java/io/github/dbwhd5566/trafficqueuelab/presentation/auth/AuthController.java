package io.github.dbwhd5566.trafficqueuelab.presentation.auth;

import io.github.dbwhd5566.trafficqueuelab.application.auth.AuthService;
import io.github.dbwhd5566.trafficqueuelab.application.auth.result.LoginResult;
import io.github.dbwhd5566.trafficqueuelab.global.response.ApiResponse;
import io.github.dbwhd5566.trafficqueuelab.presentation.auth.dto.LoginRequest;
import io.github.dbwhd5566.trafficqueuelab.presentation.auth.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/v1/auth/token")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request.email());
        return ApiResponse.success(LoginResponse.from(result));
    }
}
