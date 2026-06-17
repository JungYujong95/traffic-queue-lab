package io.github.dbwhd5566.trafficqueuelab.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.WaitingCouponIssueService;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.result.CouponIssueResult;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueRegisterResult;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatus;
import io.github.dbwhd5566.trafficqueuelab.domain.account.Account;
import io.github.dbwhd5566.trafficqueuelab.domain.coupon.CouponIssueStatus;
import io.github.dbwhd5566.trafficqueuelab.infra.persistence.account.AccountRepository;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.CouponIssueService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class JwtSecurityFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CouponIssueService couponIssueService;

    @MockitoBean
    private WaitingCouponIssueService waitingCouponIssueService;

    @Test
    void directCouponIssueAllowsAccountHeaderWithoutToken() throws Exception {
        when(couponIssueService.issueDirect(1L, 100L)).thenReturn(
                new CouponIssueResult(
                        99L,
                        1L,
                        100L,
                        CouponIssueStatus.ISSUED,
                        LocalDateTime.of(2026, 6, 16, 13, 0)
                )
        );

        mockMvc.perform(post("/api/v1/coupons/{couponId}/issue/direct", 1L)
                        .header("X-Account-Id", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.issueId").value(99L));

        verify(couponIssueService).issueDirect(1L, 100L);
    }

    @Test
    void waitCouponIssueAllowsAccountHeaderWithoutToken() throws Exception {
        when(waitingCouponIssueService.register(1L, 100L)).thenReturn(
                new WaitingQueueRegisterResult(1L, 100L, WaitingQueueStatus.WAITING, 1L)
        );

        mockMvc.perform(post("/api/v1/coupons/{couponId}/issue/wait", 1L)
                        .header("X-Account-Id", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("WAITING"))
                .andExpect(jsonPath("$.data.rank").value(1L));

        verify(waitingCouponIssueService).register(1L, 100L);
    }

    @Test
    void jwtLoginWorks() throws Exception {
        Account account = accountRepository.save(Account.create("jwt-user@example.com", "jwt-user"));

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"jwt-user@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.path("data").path("accessToken").asText();
        assertThat(jwtDecoder.decode(accessToken).getSubject()).isEqualTo(account.getId().toString());
    }

    @Test
    void jwtLoginAndCouponIssueFlowWorks() throws Exception {
        Account account = accountRepository.save(Account.create("jwt-issue-user@example.com", "jwt-issue-user"));
        when(couponIssueService.issueDirect(1L, account.getId())).thenReturn(
                new CouponIssueResult(
                        100L,
                        1L,
                        account.getId(),
                        CouponIssueStatus.ISSUED,
                        LocalDateTime.of(2026, 6, 16, 13, 0)
                )
        );

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"jwt-issue-user@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.path("data").path("accessToken").asText();
        assertThat(jwtDecoder.decode(accessToken).getSubject()).isEqualTo(account.getId().toString());

        mockMvc.perform(post("/api/v1/coupons/{couponId}/issue", 1L)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.issueId").value(100L));

        verify(couponIssueService).issueDirect(1L, account.getId());
    }
}
