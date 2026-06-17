package io.github.dbwhd5566.trafficqueuelab.presentation.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.WaitingCouponIssueService;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueRegisterResult;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatus;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatusResult;
import io.github.dbwhd5566.trafficqueuelab.global.response.ApiResponse;
import io.github.dbwhd5566.trafficqueuelab.presentation.coupon.dto.WaitingQueueRegisterResponse;
import io.github.dbwhd5566.trafficqueuelab.presentation.coupon.dto.WaitingQueueStatusResponse;
import org.junit.jupiter.api.Test;

class WaitingCouponIssueControllerTest {

    @Test
    void registerReturnsWaitingResponse() {
        WaitingCouponIssueService service = mock(WaitingCouponIssueService.class);
        WaitingCouponIssueController controller = new WaitingCouponIssueController(service);
        when(service.register(1L, 100L)).thenReturn(new WaitingQueueRegisterResult(
                1L,
                100L,
                WaitingQueueStatus.WAITING,
                5L
        ));

        ApiResponse<WaitingQueueRegisterResponse> response = controller.register(1L, 100L);

        assertThat(response.success()).isTrue();
        assertThat(response.data().status()).isEqualTo("WAITING");
        assertThat(response.data().rank()).isEqualTo(5L);
        verify(service).register(1L, 100L);
    }

    @Test
    void statusReturnsIssueResult() {
        WaitingCouponIssueService service = mock(WaitingCouponIssueService.class);
        WaitingCouponIssueController controller = new WaitingCouponIssueController(service);
        when(service.findStatus(1L, 100L)).thenReturn(new WaitingQueueStatusResult(
                1L,
                100L,
                WaitingQueueStatus.ISSUED,
                null,
                10L,
                null
        ));

        ApiResponse<WaitingQueueStatusResponse> response = controller.status(1L, 100L);

        assertThat(response.success()).isTrue();
        assertThat(response.data().status()).isEqualTo("ISSUED");
        assertThat(response.data().issueId()).isEqualTo(10L);
        verify(service).findStatus(1L, 100L);
    }
}
