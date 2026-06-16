# Direct Coupon Issue API Plan

## Purpose

Redis 대기열을 거치지 않고 DB에 바로 진입하는 쿠폰 발급 API를 만든다.

이 API는 이후 Redis 대기열 API와 부하 테스트 결과를 비교하기 위한 baseline이다.

## Scope

- Direct coupon issue HTTP API
- Coupon issue application service
- Account, Coupon, CouponIssue JPA repositories
- Coupon row pessimistic write lock
- Coupon sold-out and duplicate issue validation
- API response DTO and application result
- Service and controller tests
- Architecture and pattern documentation update

## Out of Scope

- Redis waiting queue
- queued coupon issue API
- scheduler
- k6 load test script
- authentication/JWT

## Steps

1. Coupon 발급 도메인 규칙을 추가한다.
2. Repository를 `infra/persistence`에 추가한다.
3. `CouponIssueService.issueDirect()`를 작성한다.
4. Direct issue controller와 response DTO를 작성한다.
5. 서비스/컨트롤러 테스트를 작성한다.
6. 문서에 Direct API와 baseline 역할을 기록한다.
7. `./gradlew test --no-daemon`으로 검증한다.

## Verification

- 정상 발급 시 `issued_quantity`가 증가하고 `coupon_issues`가 저장된다.
- 같은 account가 같은 coupon을 다시 발급하면 실패한다.
- 매진된 coupon 발급은 실패한다.
- Controller는 `X-Account-Id` 헤더와 `couponId` path variable을 service에 전달한다.
- 전체 테스트가 통과한다.
