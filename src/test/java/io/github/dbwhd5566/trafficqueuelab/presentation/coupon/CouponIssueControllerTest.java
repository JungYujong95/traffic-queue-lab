package io.github.dbwhd5566.trafficqueuelab.presentation.coupon;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.CouponIssueService;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.result.CouponIssueResult;
import io.github.dbwhd5566.trafficqueuelab.domain.coupon.CouponIssueStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CouponIssueControllerTest {

    private CouponIssueService couponIssueService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        couponIssueService = Mockito.mock(CouponIssueService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CouponIssueController(couponIssueService))
                .build();
    }

    @Test
    void issueDirectReturnsIssueResponse() throws Exception {
        CouponIssueResult result = new CouponIssueResult(
                10L,
                1L,
                100L,
                CouponIssueStatus.ISSUED,
                LocalDateTime.of(2026, 6, 16, 13, 0)
        );
        when(couponIssueService.issueDirect(1L, 100L)).thenReturn(result);

        mockMvc.perform(post("/api/v1/coupons/{couponId}/issue/direct", 1L)
                        .header("X-Account-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.issueId").value(10L))
                .andExpect(jsonPath("$.data.couponId").value(1L))
                .andExpect(jsonPath("$.data.status").value("ISSUED"));

        verify(couponIssueService).issueDirect(1L, 100L);
    }
}
