package io.github.dbwhd5566.trafficqueuelab.presentation.coupon.dto;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.result.CouponIssueResult;
import java.time.LocalDateTime;

public record CouponIssueResponse(
        Long issueId,
        Long couponId,
        String status,
        LocalDateTime issuedAt
) {

    public static CouponIssueResponse from(CouponIssueResult result) {
        return new CouponIssueResponse(
                result.issueId(),
                result.couponId(),
                result.status().name(),
                result.issuedAt()
        );
    }
}
