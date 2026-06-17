# Redis Waiting Queue Pattern

## Decision

Use Redis as an admission-control queue in front of DB coupon issuing.

The queue API accepts the same external load as the Direct API, but it does not let every request enter the database immediately.

## API Role

```text
POST /api/v1/coupons/{couponId}/issue/wait
  -> Redis waiting queue registration
  -> fast WAITING response

worker
  -> limited dequeue
  -> CouponIssueService.issueDirect()
  -> result stored in Redis
```

## Why Redis Queue

The Direct API baseline showed that DB connection pool size 3 cannot accept 500 RPS directly.

Redis queue reduces DB timeout by limiting DB entry rate. It does not increase DB write capacity by itself.

## Data Structures

- `coupon:{couponId}:wait:sequence`: monotonic sequence
- `coupon:{couponId}:wait:queue`: waiting sorted set
- `coupon:{couponId}:wait:processing`: leased processing sorted set
- `coupon:{couponId}:wait:result:{accountId}`: issue result

`ZADD NX` prevents duplicate queue entries for the same account.

## Worker Rule

Only worker code calls the DB issue service.

For local bottleneck testing, start with small worker concurrency because Hikari pool is intentionally small.

## Consistency

Redis provides traffic shaping.

DB remains the source of truth:

- coupon row pessimistic lock protects quantity changes
- unique constraint `(coupon_id, account_id)` protects duplicate issues
- domain exceptions determine `DUPLICATE` and `SOLD_OUT` results

## Trade-off

The request API returns `WAITING`, not immediate coupon issue completion.

This is intentional. The comparison target is timeout reduction and stable admission control under the same incoming load.
