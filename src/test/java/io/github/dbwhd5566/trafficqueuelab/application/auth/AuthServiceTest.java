package io.github.dbwhd5566.trafficqueuelab.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.dbwhd5566.trafficqueuelab.domain.account.Account;
import io.github.dbwhd5566.trafficqueuelab.global.exception.BusinessException;
import io.github.dbwhd5566.trafficqueuelab.global.exception.ErrorCode;
import io.github.dbwhd5566.trafficqueuelab.infra.persistence.account.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void loginIssuesJwtForExistingAccount() {
        Account account = accountRepository.save(Account.create("login-user@example.com", "login-user"));

        var result = authService.login(account.getEmail());
        Jwt jwt = jwtDecoder.decode(result.accessToken());

        assertThat(result.accountId()).isEqualTo(account.getId());
        assertThat(result.email()).isEqualTo(account.getEmail());
        assertThat(result.nickname()).isEqualTo(account.getNickname());
        assertThat(jwt.getSubject()).isEqualTo(account.getId().toString());
        assertThat((Object) jwt.getClaim("accountId")).isEqualTo(account.getId());
        assertThat((Object) jwt.getClaim("email")).isEqualTo(account.getEmail());
    }

    @Test
    void loginRejectsMissingAccount() {
        assertThatThrownBy(() -> authService.login("missing@example.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
    }
}
