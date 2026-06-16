package io.github.dbwhd5566.trafficqueuelab.application.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.result.CouponIssueResult;
import io.github.dbwhd5566.trafficqueuelab.domain.account.Account;
import io.github.dbwhd5566.trafficqueuelab.domain.coupon.Coupon;
import io.github.dbwhd5566.trafficqueuelab.domain.coupon.CouponIssueStatus;
import io.github.dbwhd5566.trafficqueuelab.global.exception.BusinessException;
import io.github.dbwhd5566.trafficqueuelab.global.exception.ErrorCode;
import io.github.dbwhd5566.trafficqueuelab.infra.persistence.account.AccountRepository;
import io.github.dbwhd5566.trafficqueuelab.infra.persistence.coupon.CouponIssueRepository;
import io.github.dbwhd5566.trafficqueuelab.infra.persistence.coupon.CouponRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class CouponIssueServiceTest {

    @Autowired
    private CouponIssueService couponIssueService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @Test
    void issueDirectIssuesCoupon() {
        Account account = accountRepository.save(Account.create("direct-user@example.com", "direct-user"));
        Coupon coupon = couponRepository.save(createCoupon("direct-coupon", 1000));

        CouponIssueResult result = couponIssueService.issueDirect(coupon.getId(), account.getId());

        Coupon foundCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(result.issueId()).isNotNull();
        assertThat(result.couponId()).isEqualTo(coupon.getId());
        assertThat(result.accountId()).isEqualTo(account.getId());
        assertThat(result.status()).isEqualTo(CouponIssueStatus.ISSUED);
        assertThat(foundCoupon.getIssuedQuantity()).isEqualTo(1);
        assertThat(couponIssueRepository.existsByCouponIdAndAccountId(coupon.getId(), account.getId())).isTrue();
    }

    @Test
    void issueDirectRejectsDuplicatedIssue() {
        Account account = accountRepository.save(Account.create("duplicate-user@example.com", "duplicate-user"));
        Coupon coupon = couponRepository.save(createCoupon("duplicate-coupon", 1000));
        couponIssueService.issueDirect(coupon.getId(), account.getId());

        assertThatThrownBy(() -> couponIssueService.issueDirect(coupon.getId(), account.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COUPON_ALREADY_ISSUED);
    }

    @Test
    void issueDirectRejectsSoldOutCoupon() {
        Account firstAccount = accountRepository.save(Account.create("sold-out-user-1@example.com", "sold-out-user-1"));
        Account secondAccount = accountRepository.save(Account.create("sold-out-user-2@example.com", "sold-out-user-2"));
        Coupon coupon = couponRepository.save(createCoupon("sold-out-coupon", 1));
        couponIssueService.issueDirect(coupon.getId(), firstAccount.getId());

        assertThatThrownBy(() -> couponIssueService.issueDirect(coupon.getId(), secondAccount.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COUPON_SOLD_OUT);
    }

    private Coupon createCoupon(String name, int totalQuantity) {
        return Coupon.create(
                name,
                totalQuantity,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30)
        );
    }
}
