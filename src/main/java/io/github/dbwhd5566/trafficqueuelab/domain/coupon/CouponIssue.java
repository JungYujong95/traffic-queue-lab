package io.github.dbwhd5566.trafficqueuelab.domain.coupon;

import io.github.dbwhd5566.trafficqueuelab.domain.account.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupon_issues",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_coupon_issues_coupon_account",
                        columnNames = {"coupon_id", "account_id"}
                )
        },
        indexes = {
                @Index(name = "idx_coupon_issues_account_id", columnList = "account_id")
        }
)
public class CouponIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponIssueStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected CouponIssue() {
    }

    private CouponIssue(Coupon coupon, Account account, CouponIssueStatus status) {
        this.coupon = coupon;
        this.account = account;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public static CouponIssue issue(Coupon coupon, Account account) {
        return new CouponIssue(coupon, account, CouponIssueStatus.ISSUED);
    }

    public Long getId() {
        return id;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public Account getAccount() {
        return account;
    }

    public CouponIssueStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
