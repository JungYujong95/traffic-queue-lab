# JWT Coupon k6 Load Test

JWT 로그인 후 발급받은 토큰으로 쿠폰 발급 API를 호출하는 부하 테스트 문서다.

이 스크립트는 Direct API와 비교할 때 인증 비용이 추가된 상태를 측정하기 위한 기준선이다.

## Script

- `load-test/k6/jwt-coupon-issue.js`

## Flow

1. VU마다 고정된 더미 계정 이메일로 로그인한다.
2. 로그인 응답에서 `accessToken`을 꺼내 캐시한다.
3. 캐시된 Bearer Token으로 `/api/v1/coupons/{couponId}/issue`를 호출한다.
4. 200, 401, 409 응답을 분리해서 집계한다.

## Run

로컬에서 직접 실행한다.

```bash
k6 run load-test/k6/jwt-coupon-issue.js
```

Docker로 실행할 때는 기존 direct 스크립트와 같은 방식으로 `BASE_URL`만 맞추면 된다.

```bash
docker build -t traffic-queue-k6 -f load-test/k6/Dockerfile --build-arg K6_SCRIPT=jwt-coupon-issue.js load-test/k6
docker run --rm --network host \
  -e BASE_URL=http://localhost:8080 \
  -e COUPON_ID=1 \
  -e ACCOUNT_COUNT=100000 \
  -e THINK_TIME_SECONDS=0.1 \
  traffic-queue-k6
```

## What It Measures

- JWT 로그인 응답 시간
- Bearer Token 인증 오버헤드
- DB 커넥션 풀 병목
- 쿠폰 발급 경합과 중복 발급 응답

## Compare Later

Redis 큐를 붙인 뒤에는 다음을 비교한다.

- Direct API
- JWT Protected API
- Redis Queue API

비교 기준은 성공률, p95 latency, 401/409 비율, DB timeout 비율이다.
