package io.github.dbwhd5566.trafficqueuelab.application.coupon;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.result.CouponIssueResult;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatus;
import io.github.dbwhd5566.trafficqueuelab.global.exception.BusinessException;
import io.github.dbwhd5566.trafficqueuelab.global.exception.ErrorCode;
import io.github.dbwhd5566.trafficqueuelab.infra.port.WaitingQueuePort;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WaitingCouponIssueWorker {

    private final WaitingQueuePort waitingQueuePort;
    private final CouponIssueService couponIssueService;
    private final WaitingQueueWorkerProperties properties;

    public WaitingCouponIssueWorker(
            WaitingQueuePort waitingQueuePort,
            CouponIssueService couponIssueService,
            WaitingQueueWorkerProperties properties
    ) {
        this.waitingQueuePort = waitingQueuePort;
        this.couponIssueService = couponIssueService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.wait-queue.worker.fixed-delay-ms:50}")
    public void process() {
        if (!properties.isEnabled()) {
            return;
        }

        Long couponId = properties.getCouponId();
        if (!waitingQueuePort.acquireWorkerLock(couponId, properties.getLockTtl())) {
            return;
        }

        try {
            List<Long> accountIds = waitingQueuePort.lease(couponId, properties.getBatchSize());
            for (Long accountId : accountIds) {
                issue(couponId, accountId);
            }
        } finally {
            waitingQueuePort.releaseWorkerLock(couponId);
        }
    }

    private void issue(Long couponId, Long accountId) {
        try {
            CouponIssueResult result = couponIssueService.issueDirect(couponId, accountId);
            waitingQueuePort.saveResult(
                    couponId,
                    accountId,
                    WaitingQueueStatus.ISSUED,
                    result.issueId(),
                    null,
                    properties.getResultTtl()
            );
        } catch (BusinessException exception) {
            WaitingQueueStatus status = statusFrom(exception.getErrorCode());
            waitingQueuePort.saveResult(
                    couponId,
                    accountId,
                    status,
                    null,
                    exception.getErrorCode().getMessage(),
                    properties.getResultTtl()
            );
        } catch (Exception exception) {
            waitingQueuePort.saveResult(
                    couponId,
                    accountId,
                    WaitingQueueStatus.FAILED,
                    null,
                    "쿠폰 발급 처리에 실패했습니다.",
                    properties.getResultTtl()
            );
        }
    }

    private WaitingQueueStatus statusFrom(ErrorCode errorCode) {
        if (errorCode == ErrorCode.COUPON_ALREADY_ISSUED) {
            return WaitingQueueStatus.DUPLICATE;
        }

        if (errorCode == ErrorCode.COUPON_SOLD_OUT) {
            return WaitingQueueStatus.SOLD_OUT;
        }

        return WaitingQueueStatus.FAILED;
    }
}
