package io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting;

public enum WaitingQueueStatus {
    WAITING,
    PROCESSING,
    ISSUED,
    DUPLICATE,
    SOLD_OUT,
    FAILED
}
