package io.github.dbwhd5566.trafficqueuelab.presentation.coupon.dto;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatusResult;

public record WaitingQueueStatusResponse(
        Long couponId,
        Long accountId,
        String status,
        Long rank,
        Long issueId,
        String message
) {

    public static WaitingQueueStatusResponse from(WaitingQueueStatusResult result) {
        return new WaitingQueueStatusResponse(
                result.couponId(),
                result.accountId(),
                result.status().name(),
                result.rank(),
                result.issueId(),
                result.message()
        );
    }
}
