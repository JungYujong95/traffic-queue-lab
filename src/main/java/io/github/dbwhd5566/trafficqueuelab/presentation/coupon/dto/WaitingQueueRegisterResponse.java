package io.github.dbwhd5566.trafficqueuelab.presentation.coupon.dto;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueRegisterResult;

public record WaitingQueueRegisterResponse(
        Long couponId,
        Long accountId,
        String status,
        Long rank
) {

    public static WaitingQueueRegisterResponse from(WaitingQueueRegisterResult result) {
        return new WaitingQueueRegisterResponse(
                result.couponId(),
                result.accountId(),
                result.status().name(),
                result.rank()
        );
    }
}
