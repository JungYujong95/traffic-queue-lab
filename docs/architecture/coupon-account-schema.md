# Coupon Account Schema

## Purpose

쿠폰 발급 부하 테스트에서 DB write 병목을 만들기 위한 최소 테이블 구조다.

회원가입과 인증은 이번 범위에서 제외한다. `accounts`는 부하 테스트 요청을 구분하기 위한 사용자 식별 데이터로 사용한다.

## Tables

### accounts

| Column | Type | Rule |
| --- | --- | --- |
| id | BIGINT | PK, auto increment |
| email | VARCHAR(255) | not null, unique |
| nickname | VARCHAR(50) | not null |
| created_at | DATETIME | not null |

### coupons

| Column | Type | Rule |
| --- | --- | --- |
| id | BIGINT | PK, auto increment |
| name | VARCHAR(100) | not null |
| total_quantity | INT | not null |
| issued_quantity | INT | not null |
| started_at | DATETIME | not null |
| ended_at | DATETIME | not null |
| created_at | DATETIME | not null |

### coupon_issues

| Column | Type | Rule |
| --- | --- | --- |
| id | BIGINT | PK, auto increment |
| coupon_id | BIGINT | not null, FK |
| account_id | BIGINT | not null, FK |
| status | VARCHAR(20) | not null |
| created_at | DATETIME | not null |

## Constraints

`coupon_issues` has a unique constraint on `(coupon_id, account_id)`.

This prevents the same account from receiving the same coupon more than once, even under concurrent requests.

## Load Test Usage

The API can use `X-Account-Id` to identify dummy users during load tests.

Authentication is intentionally excluded so the benchmark focuses on DB connection pool pressure, Redis waiting queue admission control, and coupon issue write throughput.

## Direct Coupon Issue API

The direct issue API bypasses Redis and enters the database immediately.

```http
POST /api/v1/coupons/{couponId}/issue/direct
X-Account-Id: 1
```

This API is the baseline for load testing.

The application service uses a pessimistic write lock when loading the coupon row.

```text
CouponIssueController
  -> CouponIssueService.issueDirect()
  -> CouponRepository.findByIdForUpdate()
  -> Coupon.issue()
  -> CouponIssueRepository.save()
```

The direct flow intentionally exposes DB connection pool pressure and coupon row lock contention. The Redis waiting queue flow will be compared against this baseline later.

## Redis Waiting Queue Issue API

The waiting queue issue API accepts load before the database.

```http
POST /api/v1/coupons/{couponId}/issue/wait
X-Account-Id: 1
```

The request path registers the account in Redis and returns a waiting status.

```text
CouponWaitingController
  -> CouponWaitingService
  -> WaitingQueuePort
  -> Redis waiting queue
```

The worker path limits DB entry and reuses the existing issue logic.

```text
CouponWaitingWorker
  -> WaitingQueuePort.dequeue()
  -> CouponIssueService.issueDirect()
  -> WaitingQueuePort.saveResult()
```

Redis is used for admission control. The database remains the source of truth for coupon quantity and duplicate issue prevention.
