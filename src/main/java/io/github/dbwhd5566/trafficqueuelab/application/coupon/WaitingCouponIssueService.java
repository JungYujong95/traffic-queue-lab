package io.github.dbwhd5566.trafficqueuelab.application.coupon;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueRegisterResult;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatus;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatusResult;
import io.github.dbwhd5566.trafficqueuelab.infra.port.WaitingQueuePort;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class WaitingCouponIssueService {

    private final WaitingQueuePort waitingQueuePort;

    public WaitingCouponIssueService(WaitingQueuePort waitingQueuePort) {
        this.waitingQueuePort = waitingQueuePort;
    }

    public WaitingQueueRegisterResult register(Long couponId, Long accountId) {
        Optional<WaitingQueueStatusResult> status = waitingQueuePort.findStatus(couponId, accountId);
        if (status.isPresent() && status.get().status() != WaitingQueueStatus.WAITING) {
            WaitingQueueStatusResult result = status.get();
            return new WaitingQueueRegisterResult(couponId, accountId, result.status(), result.rank());
        }

        return waitingQueuePort.enqueue(couponId, accountId);
    }

    public WaitingQueueStatusResult findStatus(Long couponId, Long accountId) {
        return waitingQueuePort.findStatus(couponId, accountId)
                .orElseGet(() -> new WaitingQueueStatusResult(
                        couponId,
                        accountId,
                        WaitingQueueStatus.FAILED,
                        null,
                        null,
                        "대기열 등록 정보를 찾을 수 없습니다."
                ));
    }
}
