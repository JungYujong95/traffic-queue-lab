package io.github.dbwhd5566.trafficqueuelab.application.coupon;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.wait-queue.worker")
public class WaitingQueueWorkerProperties {

    private boolean enabled = true;
    private Long couponId = 1L;
    private int batchSize = 1;
    private Duration lockTtl = Duration.ofSeconds(30);
    private Duration resultTtl = Duration.ofMinutes(30);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public Duration getLockTtl() {
        return lockTtl;
    }

    public void setLockTtl(Duration lockTtl) {
        this.lockTtl = lockTtl;
    }

    public Duration getResultTtl() {
        return resultTtl;
    }

    public void setResultTtl(Duration resultTtl) {
        this.resultTtl = resultTtl;
    }
}
