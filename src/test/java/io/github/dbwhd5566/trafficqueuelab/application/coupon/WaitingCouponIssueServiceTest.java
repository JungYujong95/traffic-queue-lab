package io.github.dbwhd5566.trafficqueuelab.application.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueRegisterResult;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatus;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatusResult;
import io.github.dbwhd5566.trafficqueuelab.infra.port.WaitingQueuePort;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class WaitingCouponIssueServiceTest {

    @Test
    void registerEnqueuesAccountWhenNoResultExists() {
        WaitingQueuePort waitingQueuePort = mock(WaitingQueuePort.class);
        WaitingCouponIssueService service = new WaitingCouponIssueService(waitingQueuePort);
        WaitingQueueRegisterResult expected = new WaitingQueueRegisterResult(
                1L,
                100L,
                WaitingQueueStatus.WAITING,
                10L
        );
        when(waitingQueuePort.findStatus(1L, 100L)).thenReturn(Optional.empty());
        when(waitingQueuePort.enqueue(1L, 100L)).thenReturn(expected);

        WaitingQueueRegisterResult result = service.register(1L, 100L);

        assertThat(result).isEqualTo(expected);
        verify(waitingQueuePort).enqueue(1L, 100L);
    }

    @Test
    void registerReturnsCompletedResultWithoutEnqueue() {
        WaitingQueuePort waitingQueuePort = mock(WaitingQueuePort.class);
        WaitingCouponIssueService service = new WaitingCouponIssueService(waitingQueuePort);
        when(waitingQueuePort.findStatus(1L, 100L)).thenReturn(Optional.of(
                new WaitingQueueStatusResult(1L, 100L, WaitingQueueStatus.ISSUED, null, 10L, null)
        ));

        WaitingQueueRegisterResult result = service.register(1L, 100L);

        assertThat(result.status()).isEqualTo(WaitingQueueStatus.ISSUED);
        assertThat(result.rank()).isNull();
    }
}
