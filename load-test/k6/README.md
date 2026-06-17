# k6 Load Tests

## Direct Coupon Issue

`direct-coupon-issue.js` calls the Direct coupon issue API without JWT login.

```http
POST /api/v1/coupons/{couponId}/issue/direct
X-Account-Id: {accountId}
```

Default load profile:

- `KTX_RPS=500`
- `KTX_DURATION=2m`
- `KTX_PREALLOCATED_VUS=500`
- `KTX_MAX_VUS=1500`
- `ACCOUNT_COUNT=100000`

Run locally:

```bash
k6 run load-test/k6/direct-coupon-issue.js
```

Run with explicit values:

```bash
BASE_URL=http://localhost:8080 \
COUPON_ID=1 \
ACCOUNT_COUNT=100000 \
KTX_RPS=500 \
KTX_DURATION=2m \
KTX_PREALLOCATED_VUS=500 \
KTX_MAX_VUS=1500 \
THINK_TIME_SECONDS=0.1 \
k6 run load-test/k6/direct-coupon-issue.js
```

Run with Docker:

```bash
docker build -t traffic-queue-k6 -f load-test/k6/Dockerfile load-test/k6
docker run --rm --network host \
  -e BASE_URL=http://localhost:8080 \
  -e COUPON_ID=1 \
  -e ACCOUNT_COUNT=100000 \
  -e KTX_RPS=500 \
  -e KTX_DURATION=2m \
  -e KTX_PREALLOCATED_VUS=500 \
  -e KTX_MAX_VUS=1500 \
  -e THINK_TIME_SECONDS=0.1 \
  traffic-queue-k6
```

## JWT Coupon Issue

`jwt-coupon-issue.js` is a separate protected-flow benchmark. It logs in through `/api/v1/auth/token`, caches the Bearer token, and calls `/api/v1/coupons/{couponId}/issue`.

```bash
docker build -t traffic-queue-k6 -f load-test/k6/Dockerfile --build-arg K6_SCRIPT=jwt-coupon-issue.js load-test/k6
```

## Wait Coupon Issue

`wait-coupon-issue.js` uses the same external load profile as the Direct script, but calls the Redis waiting queue API.

```http
POST /api/v1/coupons/{couponId}/issue/wait
X-Account-Id: {accountId}
```

Run locally:

```bash
k6 run load-test/k6/wait-coupon-issue.js
```

Run with Docker:

```bash
docker build -t traffic-queue-k6 -f load-test/k6/Dockerfile --build-arg K6_SCRIPT=wait-coupon-issue.js load-test/k6
docker run --rm --network host \
  -e BASE_URL=http://localhost:8080 \
  -e COUPON_ID=1 \
  -e ACCOUNT_COUNT=100000 \
  -e KTX_RPS=500 \
  -e KTX_DURATION=2m \
  -e KTX_PREALLOCATED_VUS=500 \
  -e KTX_MAX_VUS=1500 \
  -e THINK_TIME_SECONDS=0.1 \
  traffic-queue-k6
```
