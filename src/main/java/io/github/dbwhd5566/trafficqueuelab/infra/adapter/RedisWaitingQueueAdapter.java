package io.github.dbwhd5566.trafficqueuelab.infra.adapter;

import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueRegisterResult;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatus;
import io.github.dbwhd5566.trafficqueuelab.application.coupon.waiting.WaitingQueueStatusResult;
import io.github.dbwhd5566.trafficqueuelab.infra.port.WaitingQueuePort;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

@Component
public class RedisWaitingQueueAdapter implements WaitingQueuePort {

    private static final String STATUS = "status";
    private static final String RANK = "rank";
    private static final String ISSUE_ID = "issueId";
    private static final String MESSAGE = "message";
    private static final String LOCK_VALUE = "locked";

    private final StringRedisTemplate redisTemplate;

    public RedisWaitingQueueAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public WaitingQueueRegisterResult enqueue(Long couponId, Long accountId) {
        String member = accountId.toString();
        Long sequence = redisTemplate.opsForValue().increment(sequenceKey(couponId));
        Boolean added = redisTemplate.opsForZSet().addIfAbsent(queueKey(couponId), member, sequence);
        Long rank = rank(couponId, accountId).orElse(null);

        if (Boolean.FALSE.equals(added)) {
            return new WaitingQueueRegisterResult(couponId, accountId, WaitingQueueStatus.WAITING, rank);
        }

        return new WaitingQueueRegisterResult(couponId, accountId, WaitingQueueStatus.WAITING, rank);
    }

    @Override
    public Optional<WaitingQueueStatusResult> findStatus(Long couponId, Long accountId) {
        Optional<WaitingQueueStatusResult> savedResult = findSavedResult(couponId, accountId);
        if (savedResult.isPresent()) {
            return savedResult;
        }

        Optional<Long> queueRank = rank(couponId, accountId);
        if (queueRank.isPresent()) {
            return Optional.of(new WaitingQueueStatusResult(
                    couponId,
                    accountId,
                    WaitingQueueStatus.WAITING,
                    queueRank.get(),
                    null,
                    null
            ));
        }

        Double processingScore = redisTemplate.opsForZSet().score(processingKey(couponId), accountId.toString());
        if (processingScore != null) {
            return Optional.of(new WaitingQueueStatusResult(
                    couponId,
                    accountId,
                    WaitingQueueStatus.PROCESSING,
                    null,
                    null,
                    null
            ));
        }

        return Optional.empty();
    }

    @Override
    public List<Long> lease(Long couponId, int size) {
        Set<TypedTuple<String>> entries = redisTemplate.opsForZSet().popMin(queueKey(couponId), size);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        long now = System.currentTimeMillis();
        return entries.stream()
                .map(TypedTuple::getValue)
                .filter(value -> value != null && !value.isBlank())
                .peek(value -> redisTemplate.opsForZSet().add(processingKey(couponId), value, now))
                .map(Long::valueOf)
                .toList();
    }

    @Override
    public boolean acquireWorkerLock(Long couponId, Duration ttl) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(workerLockKey(couponId), LOCK_VALUE, ttl);
        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public void releaseWorkerLock(Long couponId) {
        redisTemplate.delete(workerLockKey(couponId));
    }

    @Override
    public void saveResult(
            Long couponId,
            Long accountId,
            WaitingQueueStatus status,
            Long issueId,
            String message,
            Duration ttl
    ) {
        redisTemplate.opsForZSet().remove(processingKey(couponId), accountId.toString());
        String key = resultKey(couponId, accountId);

        redisTemplate.opsForHash().put(key, STATUS, status.name());
        if (issueId != null) {
            redisTemplate.opsForHash().put(key, ISSUE_ID, issueId.toString());
        }
        if (message != null) {
            redisTemplate.opsForHash().put(key, MESSAGE, message);
        }
        redisTemplate.expire(key, ttl);
    }

    private Optional<WaitingQueueStatusResult> findSavedResult(Long couponId, Long accountId) {
        Map<Object, Object> values = redisTemplate.opsForHash().entries(resultKey(couponId, accountId));
        Object statusValue = values.get(STATUS);
        if (statusValue == null) {
            return Optional.empty();
        }

        WaitingQueueStatus status = WaitingQueueStatus.valueOf(statusValue.toString());
        Long issueId = toLong(values.get(ISSUE_ID));
        String message = Optional.ofNullable(values.get(MESSAGE))
                .map(Object::toString)
                .orElse(null);

        return Optional.of(new WaitingQueueStatusResult(couponId, accountId, status, null, issueId, message));
    }

    private Optional<Long> rank(Long couponId, Long accountId) {
        Long zeroBasedRank = redisTemplate.opsForZSet().rank(queueKey(couponId), accountId.toString());
        if (zeroBasedRank == null) {
            return Optional.empty();
        }
        return Optional.of(zeroBasedRank + 1);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        return Long.valueOf(value.toString());
    }

    private String sequenceKey(Long couponId) {
        return "coupon:%d:wait:sequence".formatted(couponId);
    }

    private String queueKey(Long couponId) {
        return "coupon:%d:wait:queue".formatted(couponId);
    }

    private String processingKey(Long couponId) {
        return "coupon:%d:wait:processing".formatted(couponId);
    }

    private String resultKey(Long couponId, Long accountId) {
        return "coupon:%d:wait:result:%d".formatted(couponId, accountId);
    }

    private String workerLockKey(Long couponId) {
        return "coupon:%d:wait:worker-lock".formatted(couponId);
    }
}
