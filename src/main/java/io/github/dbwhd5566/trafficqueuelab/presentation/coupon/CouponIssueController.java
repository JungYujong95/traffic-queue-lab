package io.github.dbwhd5566.trafficqueuelab.presentation.coupon;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.CouponIssueService;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.result.CouponIssueResult;
import io.github.dbwhd5566.trafficqueuelab.global.response.ApiResponse;
import io.github.dbwhd5566.trafficqueuelab.presentation.coupon.dto.CouponIssueResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CouponIssueController {

    private final CouponIssueService couponIssueService;

    public CouponIssueController(CouponIssueService couponIssueService) {
        this.couponIssueService = couponIssueService;
    }

    @PostMapping("/api/v1/coupons/{couponId}/issue/direct")
    public ApiResponse<CouponIssueResponse> issueDirect(
            @PathVariable Long couponId,
            @RequestHeader("X-Account-Id") Long accountId
    ) {
        CouponIssueResult result = couponIssueService.issueDirect(couponId, accountId);
        return ApiResponse.success(CouponIssueResponse.from(result));
    }
}
