package io.github.dbwhd5566.trafficqueuelab.application.auth;

import io.github.dbwhd5566.trafficqueuelab.application.auth.result.LoginResult;
import io.github.dbwhd5566.trafficqueuelab.domain.account.Account;
import io.github.dbwhd5566.trafficqueuelab.global.exception.BusinessException;
import io.github.dbwhd5566.trafficqueuelab.global.exception.ErrorCode;
import io.github.dbwhd5566.trafficqueuelab.global.security.JwtTokenService;
import io.github.dbwhd5566.trafficqueuelab.infra.persistence.account.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AccountRepository accountRepository;
    private final JwtTokenService jwtTokenService;

    public AuthService(AccountRepository accountRepository, JwtTokenService jwtTokenService) {
        this.accountRepository = accountRepository;
        this.jwtTokenService = jwtTokenService;
    }

    public LoginResult login(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        String accessToken = jwtTokenService.issueAccessToken(account);
        return new LoginResult(
                accessToken,
                "Bearer",
                jwtTokenService.getAccessTokenTtlSeconds(),
                account.getId(),
                account.getEmail(),
                account.getNickname()
        );
    }
}
