package io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting;

public record WaitingQueueRegisterResult(
        Long couponId,
        Long accountId,
        WaitingQueueStatus status,
        Long rank
) {
}
