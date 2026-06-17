package io.github.dbwhd5566.trafficqueuelab.application.coupon;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.result.CouponIssueResult;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatus;
import io.github.dbwhd5566.trafficqueuelab.domain.coupon.CouponIssueStatus;
import io.github.dbwhd5566.trafficqueuelab.global.exception.BusinessException;
import io.github.dbwhd5566.trafficqueuelab.global.exception.ErrorCode;
import io.github.dbwhd5566.trafficqueuelab.infra.port.WaitingQueuePort;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class WaitingCouponIssueWorkerTest {

    @Test
    void processDoesNothingWhenDisabled() {
        WaitingQueuePort waitingQueuePort = mock(WaitingQueuePort.class);
        CouponIssueService couponIssueService = mock(CouponIssueService.class);
        WaitingQueueWorkerProperties properties = new WaitingQueueWorkerProperties();
        properties.setEnabled(false);
        WaitingCouponIssueWorker worker = new WaitingCouponIssueWorker(
                waitingQueuePort,
                couponIssueService,
                properties
        );

        worker.process();

        verify(waitingQueuePort, never()).lease(any(), anyInt());
    }

    @Test
    void processIssuesLeasedAccountAndStoresIssuedResult() {
        WaitingQueuePort waitingQueuePort = mock(WaitingQueuePort.class);
        CouponIssueService couponIssueService = mock(CouponIssueService.class);
        WaitingQueueWorkerProperties properties = enabledProperties();
        WaitingCouponIssueWorker worker = new WaitingCouponIssueWorker(
                waitingQueuePort,
                couponIssueService,
                properties
        );
        when(waitingQueuePort.acquireWorkerLock(1L, Duration.ofSeconds(30))).thenReturn(true);
        when(waitingQueuePort.lease(1L, 1)).thenReturn(List.of(100L));
        when(couponIssueService.issueDirect(1L, 100L)).thenReturn(new CouponIssueResult(
                10L,
                1L,
                100L,
                CouponIssueStatus.ISSUED,
                LocalDateTime.of(2026, 6, 16, 13, 0)
        ));

        worker.process();

        verify(waitingQueuePort).saveResult(
                eq(1L),
                eq(100L),
                eq(WaitingQueueStatus.ISSUED),
                eq(10L),
                eq(null),
                eq(Duration.ofMinutes(30))
        );
        verify(waitingQueuePort).releaseWorkerLock(1L);
    }

    @Test
    void processStoresDuplicateResultWhenIssueAlreadyExists() {
        WaitingQueuePort waitingQueuePort = mock(WaitingQueuePort.class);
        CouponIssueService couponIssueService = mock(CouponIssueService.class);
        WaitingQueueWorkerProperties properties = enabledProperties();
        WaitingCouponIssueWorker worker = new WaitingCouponIssueWorker(
                waitingQueuePort,
                couponIssueService,
                properties
        );
        when(waitingQueuePort.acquireWorkerLock(1L, Duration.ofSeconds(30))).thenReturn(true);
        when(waitingQueuePort.lease(1L, 1)).thenReturn(List.of(100L));
        when(couponIssueService.issueDirect(1L, 100L))
                .thenThrow(new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED));

        worker.process();

        verify(waitingQueuePort).saveResult(
                eq(1L),
                eq(100L),
                eq(WaitingQueueStatus.DUPLICATE),
                eq(null),
                eq(ErrorCode.COUPON_ALREADY_ISSUED.getMessage()),
                eq(Duration.ofMinutes(30))
        );
        verify(waitingQueuePort).releaseWorkerLock(1L);
    }

    private WaitingQueueWorkerProperties enabledProperties() {
        WaitingQueueWorkerProperties properties = new WaitingQueueWorkerProperties();
        properties.setEnabled(true);
        properties.setCouponId(1L);
        properties.setBatchSize(1);
        properties.setLockTtl(Duration.ofSeconds(30));
        properties.setResultTtl(Duration.ofMinutes(30));
        return properties;
    }
}
