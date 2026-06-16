package io.github.dbwhd5566.trafficqueuelab.domain.coupon;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.dbwhd5566.trafficqueuelab.domain.account.Account;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class CouponIssueEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistCouponIssueWithAccountAndCoupon() {
        Account account = Account.create("load-test-user-1@example.com", "load-test-user-1");
        Coupon coupon = Coupon.create(
                "load-test-coupon",
                1000,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30)
        );

        entityManager.persist(account);
        entityManager.persist(coupon);
        CouponIssue couponIssue = CouponIssue.issue(coupon, account);
        entityManager.persist(couponIssue);
        entityManager.flush();
        entityManager.clear();

        CouponIssue found = entityManager.find(CouponIssue.class, couponIssue.getId());

        assertThat(found.getId()).isNotNull();
        assertThat(found.getStatus()).isEqualTo(CouponIssueStatus.ISSUED);
        assertThat(found.getAccount().getId()).isEqualTo(account.getId());
        assertThat(found.getCoupon().getId()).isEqualTo(coupon.getId());
    }
}
