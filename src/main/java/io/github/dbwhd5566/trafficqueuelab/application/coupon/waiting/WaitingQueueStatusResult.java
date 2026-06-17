package io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting;

public record WaitingQueueStatusResult(
        Long couponId,
        Long accountId,
        WaitingQueueStatus status,
        Long rank,
        Long issueId,
        String message
) {
}
