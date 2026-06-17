package io.github.dbwhd5566.trafficqueuelab.presentation.coupon;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.CouponIssueService;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.result.CouponIssueResult;
import io.github.dbwhd5566.trafficqueuelab.global.exception.BusinessException;
import io.github.dbwhd5566.trafficqueuelab.global.exception.ErrorCode;
import io.github.dbwhd5566.trafficqueuelab.global.response.ApiResponse;
import io.github.dbwhd5566.trafficqueuelab.presentation.coupon.dto.CouponIssueResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @PostMapping("/api/v1/coupons/{couponId}/issue")
    public ApiResponse<CouponIssueResponse> issueByJwt(
            @PathVariable Long couponId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long accountId = extractAccountId(jwt);
        CouponIssueResult result = couponIssueService.issueDirect(couponId, accountId);
        return ApiResponse.success(CouponIssueResponse.from(result));
    }

    private Long extractAccountId(Jwt jwt) {
        if (jwt == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object accountIdClaim = jwt.getClaims().get("accountId");
        if (accountIdClaim instanceof Number number) {
            return number.longValue();
        }

        if (accountIdClaim instanceof String value) {
            return Long.valueOf(value);
        }

        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}
