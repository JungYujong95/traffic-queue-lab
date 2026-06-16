package io.github.dbwhd5566.trafficqueuelab.domain.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime endedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Coupon() {
    }

    private Coupon(String name, int totalQuantity, LocalDateTime startedAt, LocalDateTime endedAt) {
        this.name = name;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = 0;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.createdAt = LocalDateTime.now();
    }

    public static Coupon create(
            String name,
            int totalQuantity,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        return new Coupon(name, totalQuantity, startedAt, endedAt);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getIssuedQuantity() {
        return issuedQuantity;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
