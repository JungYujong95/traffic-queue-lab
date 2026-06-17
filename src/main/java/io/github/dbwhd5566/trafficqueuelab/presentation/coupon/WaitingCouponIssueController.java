package io.github.dbwhd5566.trafficqueuelab.presentation.coupon;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.WaitingCouponIssueService;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueRegisterResult;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatusResult;
import io.github.dbwhd5566.trafficqueuelab.global.response.ApiResponse;
import io.github.dbwhd5566.trafficqueuelab.presentation.coupon.dto.WaitingQueueRegisterResponse;
import io.github.dbwhd5566.trafficqueuelab.presentation.coupon.dto.WaitingQueueStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WaitingCouponIssueController {

    private final WaitingCouponIssueService waitingCouponIssueService;

    public WaitingCouponIssueController(WaitingCouponIssueService waitingCouponIssueService) {
        this.waitingCouponIssueService = waitingCouponIssueService;
    }

    @PostMapping("/api/v1/coupons/{couponId}/issue/wait")
    public ApiResponse<WaitingQueueRegisterResponse> register(
            @PathVariable Long couponId,
            @RequestHeader("X-Account-Id") Long accountId
    ) {
        WaitingQueueRegisterResult result = waitingCouponIssueService.register(couponId, accountId);
        return ApiResponse.success(WaitingQueueRegisterResponse.from(result));
    }

    @GetMapping("/api/v1/coupons/{couponId}/issue/wait/status")
    public ApiResponse<WaitingQueueStatusResponse> status(
            @PathVariable Long couponId,
            @RequestHeader("X-Account-Id") Long accountId
    ) {
        WaitingQueueStatusResult result = waitingCouponIssueService.findStatus(couponId, accountId);
        return ApiResponse.success(WaitingQueueStatusResponse.from(result));
    }
}
