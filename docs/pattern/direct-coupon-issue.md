# Direct Coupon Issue Pattern

## Decision

Provide a direct coupon issue API before adding Redis waiting queue control.

```http
POST /api/v1/coupons/{couponId}/issue/direct
X-Account-Id: {accountId}
```

## Rationale

The direct API is not the protected production flow. It is the baseline for proving the bottleneck.

Without Redis admission control, concurrent requests enter the database immediately. This makes DB connection pool pressure and pessimistic lock contention observable during load tests.

## Consistency Rules

- Coupon quantity is changed inside a transaction.
- Coupon row is loaded with pessimistic write lock.
- Duplicate issue is blocked by application validation and the database unique constraint on `(coupon_id, account_id)`.
- Sold-out coupons raise a business exception.
- DB connection acquisition timeout is returned as `DB_503_001`.

## Layer Flow

```text
presentation/coupon/CouponIssueController
  -> application/coupon/CouponIssueService
  -> infra/persistence repositories
  -> domain/coupon/Coupon.issue()
```

## Next Step

The queued issue flow should keep the same domain issue logic but require Redis active-token validation before entering the DB transaction.
