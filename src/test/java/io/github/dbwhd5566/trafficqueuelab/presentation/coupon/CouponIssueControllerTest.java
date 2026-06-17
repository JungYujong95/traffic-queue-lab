package io.github.dbwhd5566.trafficqueuelab.presentation.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.CouponIssueService;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.result.CouponIssueResult;
import io.github.dbwhd5566.trafficqueuelab.domain.coupon.CouponIssueStatus;
import io.github.dbwhd5566.trafficqueuelab.global.response.ApiResponse;
import io.github.dbwhd5566.trafficqueuelab.presentation.coupon.dto.CouponIssueResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CouponIssueControllerTest {

    @Test
    void issueDirectReturnsIssueResponse() {
        CouponIssueService couponIssueService = mock(CouponIssueService.class);
        CouponIssueController controller = new CouponIssueController(couponIssueService);
        CouponIssueResult result = new CouponIssueResult(
                10L,
                1L,
                100L,
                CouponIssueStatus.ISSUED,
                LocalDateTime.of(2026, 6, 16, 13, 0)
        );
        when(couponIssueService.issueDirect(1L, 100L)).thenReturn(result);

        ApiResponse<CouponIssueResponse> response = controller.issueDirect(1L, 100L);

        assertThat(response.success()).isTrue();
        assertThat(response.data().issueId()).isEqualTo(10L);
        assertThat(response.data().couponId()).isEqualTo(1L);
        assertThat(response.data().status()).isEqualTo("ISSUED");

        verify(couponIssueService).issueDirect(1L, 100L);
    }
}
