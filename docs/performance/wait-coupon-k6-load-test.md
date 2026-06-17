# Wait Coupon k6 Load Test

## Purpose

Redis 대기열 API에 Direct API와 같은 외부 부하를 걸어 DB connection timeout 감소를 확인한다.

Direct API와 동일한 기본 조건을 사용한다.

```text
KTX_RPS=500
KTX_DURATION=2m
KTX_PREALLOCATED_VUS=500
KTX_MAX_VUS=1500
ACCOUNT_COUNT=100000
```

## Target API

```http
POST /api/v1/coupons/{couponId}/issue/wait
X-Account-Id: {accountId}
```

이 API는 쿠폰을 즉시 발급하지 않고 Redis 대기열에 등록한다.
DB 발급은 worker가 제한된 속도로 처리한다.

## Worker Setting

로컬 병목 실험에서는 worker가 기본으로 켜져 있다.

```env
APP_WAIT_QUEUE_WORKER_ENABLED=true
APP_WAIT_QUEUE_WORKER_COUPON_ID=1
APP_WAIT_QUEUE_WORKER_BATCH_SIZE=1
APP_WAIT_QUEUE_WORKER_FIXED_DELAY_MS=50
APP_WAIT_QUEUE_WORKER_LOCK_TTL=30s
APP_WAIT_QUEUE_RESULT_TTL=30m
```

Hikari pool size가 작으므로 처음에는 batch size를 1로 둔다.
이전 테스트의 Redis queue가 남아 있으면 앱 시작 후 worker가 바로 소비할 수 있으므로, 비교 테스트 전에는 Redis queue와 DB 발급 내역을 초기화한다.

## Run

```bash
k6 run load-test/k6/wait-coupon-issue.js
```

명시적으로 실행한다.

```bash
BASE_URL=http://localhost:8080 \
COUPON_ID=1 \
ACCOUNT_COUNT=100000 \
KTX_RPS=500 \
KTX_DURATION=2m \
KTX_PREALLOCATED_VUS=500 \
KTX_MAX_VUS=1500 \
THINK_TIME_SECONDS=0.1 \
k6 run load-test/k6/wait-coupon-issue.js
```

## Expected Result

Direct API 대비 기대하는 변화:

- wait API p95 latency 감소
- `DB_503_001` 감소
- k6 dropped iterations 감소
- 요청 응답은 `WAITING` 중심으로 증가
- 최종 발급은 worker 처리량에 맞춰 천천히 증가

Redis 대기열은 DB 처리량을 늘리는 기능이 아니라 DB 진입량을 제한해 timeout을 줄이는 기능이다.
