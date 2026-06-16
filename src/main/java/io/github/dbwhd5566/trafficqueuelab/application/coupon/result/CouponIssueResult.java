package io.github.dbwhd5566.trafficqueuelab.application.coupon.result;

import io.github.dbwhd5566.trafficqueuelab.domain.coupon.CouponIssue;
import io.github.dbwhd5566.trafficqueuelab.domain.coupon.CouponIssueStatus;
import java.time.LocalDateTime;

public record CouponIssueResult(
        Long issueId,
        Long couponId,
        Long accountId,
        CouponIssueStatus status,
        LocalDateTime issuedAt
) {

    public static CouponIssueResult from(CouponIssue couponIssue) {
        return new CouponIssueResult(
                couponIssue.getId(),
                couponIssue.getCoupon().getId(),
                couponIssue.getAccount().getId(),
                couponIssue.getStatus(),
                couponIssue.getCreatedAt()
        );
    }
}
