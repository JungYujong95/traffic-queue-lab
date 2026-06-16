package io.github.dbwhd5566.trafficqueuelab.infra.persistence.coupon;

import io.github.dbwhd5566.trafficqueuelab.domain.coupon.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {

    boolean existsByCouponIdAndAccountId(Long couponId, Long accountId);
}
