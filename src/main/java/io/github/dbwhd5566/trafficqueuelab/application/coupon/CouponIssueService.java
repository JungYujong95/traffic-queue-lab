package io.github.dbwhd5566.trafficqueuelab.application.coupon;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.result.CouponIssueResult;
import io.github.dbwhd5566.trafficqueuelab.domain.account.Account;
import io.github.dbwhd5566.trafficqueuelab.domain.coupon.Coupon;
import io.github.dbwhd5566.trafficqueuelab.domain.coupon.CouponIssue;
import io.github.dbwhd5566.trafficqueuelab.global.exception.BusinessException;
import io.github.dbwhd5566.trafficqueuelab.global.exception.ErrorCode;
import io.github.dbwhd5566.trafficqueuelab.infra.persistence.account.AccountRepository;
import io.github.dbwhd5566.trafficqueuelab.infra.persistence.coupon.CouponIssueRepository;
import io.github.dbwhd5566.trafficqueuelab.infra.persistence.coupon.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponIssueService {

    private final AccountRepository accountRepository;
    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;

    public CouponIssueService(
            AccountRepository accountRepository,
            CouponRepository couponRepository,
            CouponIssueRepository couponIssueRepository
    ) {
        this.accountRepository = accountRepository;
        this.couponRepository = couponRepository;
        this.couponIssueRepository = couponIssueRepository;
    }

    @Transactional
    public CouponIssueResult issueDirect(Long couponId, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        Coupon coupon = couponRepository.findByIdForUpdate(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        validateNotIssued(couponId, accountId);
        coupon.issue();

        CouponIssue couponIssue = couponIssueRepository.save(CouponIssue.issue(coupon, account));
        return CouponIssueResult.from(couponIssue);
    }

    private void validateNotIssued(Long couponId, Long accountId) {
        if (couponIssueRepository.existsByCouponIdAndAccountId(couponId, accountId)) {
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
        }
    }
}
