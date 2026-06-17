# Redis Waiting Queue API Plan

## Purpose

Direct API에서 확인한 DB connection timeout을 줄이기 위해 Redis 대기열과 제한된 worker 기반 쿠폰 발급 API를 설계한다.

동일한 부하 조건(`500 RPS`, `2m`, account `100,000`, coupon `100,000`)에서 Direct API와 비교한다.

## Goal

- 외부 요청은 Redis 대기열에 빠르게 등록한다.
- DB 발급은 worker가 제한된 속도로만 수행한다.
- Hikari connection timeout(`DB_503_001`)을 Direct API 대비 크게 줄인다.
- 기존 `CouponIssueService.issueDirect()`의 도메인 발급 로직과 DB 정합성 검증을 재사용한다.

## Out of Scope

- 결제
- 알림
- 운영용 관리자 화면
- 정확한 ETA 계산
- Redis Stream 전환

## API Design

### 대기 등록

```http
POST /api/v1/coupons/{couponId}/issue/wait
X-Account-Id: {accountId}
```

응답:

```json
{
  "success": true,
  "message": "요청이 성공했습니다.",
  "data": {
    "couponId": 1,
    "accountId": 100,
    "status": "WAITING",
    "rank": 1234
  }
}
```

### 대기/발급 상태 조회

```http
GET /api/v1/coupons/{couponId}/issue/wait/status
X-Account-Id: {accountId}
```

응답 상태:

- `WAITING`: Redis 대기열에 있음
- `ISSUED`: DB 발급 완료
- `DUPLICATE`: 이미 발급됨
- `SOLD_OUT`: 쿠폰 소진
- `FAILED`: 재시도 불가능한 실패

## Redis Data Design

키는 쿠폰 단위로 분리한다.

```text
coupon:{couponId}:wait:sequence
coupon:{couponId}:wait:queue
coupon:{couponId}:wait:processing
coupon:{couponId}:wait:result:{accountId}
```

### `wait:sequence`

Redis `INCR`로 증가시키는 대기 순번이다.

### `wait:queue`

Sorted Set.

- member: `accountId`
- score: `sequence`

`ZADD NX`를 사용해 같은 계정의 중복 대기 등록을 막는다.

### `wait:processing`

Sorted Set.

worker가 queue에서 꺼낸 뒤 DB 발급 처리 중인 계정을 기록한다.

- member: `accountId`
- score: processing started timestamp millis

worker 장애 시 일정 시간이 지난 processing 항목은 queue로 복구할 수 있다.

### `wait:result:{accountId}`

Hash 또는 JSON 문자열.

```json
{
  "status": "ISSUED",
  "issueId": 10,
  "message": null
}
```

TTL을 둬서 부하 테스트 이후 Redis 메모리가 계속 증가하지 않게 한다.

## Worker Design

worker는 Redis에서 제한된 수만 꺼내 DB 발급에 진입시킨다.

```text
Redis wait queue
  -> worker leases N accounts
  -> CouponIssueService.issueDirect(couponId, accountId)
  -> result 저장
```

초기 로컬 설정:

```env
APP_WAIT_QUEUE_WORKER_ENABLED=true
APP_WAIT_QUEUE_WORKER_BATCH_SIZE=1
APP_WAIT_QUEUE_WORKER_FIXED_DELAY=50ms
APP_WAIT_QUEUE_RESULT_TTL=30m
APP_WAIT_QUEUE_PROCESSING_TIMEOUT=30s
```

Hikari pool size가 3인 로컬 병목 실험에서는 worker batch/concurrency를 작게 시작한다.
처음 목표는 처리량 극대화가 아니라 DB timeout 감소다.

## Multi Instance Rule

로컬 Compose에는 Spring Boot 컨테이너를 여러 개 띄울 수 있다.

worker가 모든 인스턴스에서 동시에 돌면 DB 진입량 제한이 깨질 수 있으므로 Redis lock을 둔다.

```text
coupon:{couponId}:wait:worker-lock
```

`SET key value NX PX {ttl}`로 짧은 lease를 잡은 worker만 dequeue를 수행한다.
worker는 batch 처리 후 lock을 release하고, TTL은 worker crash 시 자동 복구를 위한 안전장치로 사용한다.

## Consistency Rules

- Redis 대기열은 유입 제어용이다.
- 최종 정합성은 DB transaction, coupon row pessimistic lock, unique constraint `(coupon_id, account_id)`가 보장한다.
- worker에서 중복 발급 예외가 나면 result를 `DUPLICATE`로 저장한다.
- 매진 예외가 나면 result를 `SOLD_OUT`으로 저장하고 이후 worker는 해당 쿠폰 queue 처리를 멈출 수 있다.
- DB connection timeout이 발생하면 해당 account는 queue로 재등록하거나 processing timeout 복구 대상으로 둔다.

## Comparison Metrics

Direct API와 Redis Queue API는 같은 k6 부하 조건으로 비교한다.

```text
KTX_RPS=500
KTX_DURATION=2m
KTX_PREALLOCATED_VUS=500
KTX_MAX_VUS=1500
ACCOUNT_COUNT=100000
```

비교 지표:

- API p95 latency
- `DB_503_001` count
- dropped iterations
- 대기 등록 성공률
- 최종 발급 성공 수
- Redis queue length
- worker 처리량

## Implementation Steps

1. `WaitingQueuePort`를 정의한다.
2. Redis adapter를 구현한다.
3. 대기 등록 result DTO와 controller endpoint를 추가한다.
4. 상태 조회 endpoint를 추가한다.
5. worker service를 추가하고 `CouponIssueService.issueDirect()`를 재사용한다.
6. Redis queue unit/integration test를 작성한다.
7. wait API k6 script를 작성한다.
8. Direct API와 같은 부하 조건으로 비교 문서를 작성한다.

## Verification

- 같은 account의 중복 대기 등록은 하나의 queue entry만 만든다.
- 대기 등록은 DB connection을 사용하지 않는다.
- worker만 DB 발급 트랜잭션에 진입한다.
- worker batch/concurrency 제한으로 `DB_503_001`이 Direct API 대비 감소한다.
- 전체 테스트는 `./gradlew test --no-daemon`으로 검증한다.
