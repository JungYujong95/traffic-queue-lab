package io.github.dbwhd5566.trafficqueuelab.infra.port;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueRegisterResult;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatus;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatusResult;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface WaitingQueuePort {

    WaitingQueueRegisterResult enqueue(Long couponId, Long accountId);

    Optional<WaitingQueueStatusResult> findStatus(Long couponId, Long accountId);

    List<Long> lease(Long couponId, int size);

    boolean acquireWorkerLock(Long couponId, Duration ttl);

    void releaseWorkerLock(Long couponId);

    void saveResult(
            Long couponId,
            Long accountId,
            WaitingQueueStatus status,
            Long issueId,
            String message,
            Duration ttl
    );
}
