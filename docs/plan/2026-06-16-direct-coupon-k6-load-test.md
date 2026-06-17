# Direct Coupon k6 Load Test Plan

## Purpose

Redis 대기열 없이 Direct 쿠폰 발급 API에 부하를 주는 k6 스크립트를 작성한다.

이 스크립트는 DB 커넥션 풀 병목과 coupon row 비관락 경합을 관찰하기 위한 baseline 부하 테스트다.

## Scope

- k6 스크립트 추가
- 실행 방법 문서화
- 스크립트 주요 코드 의미 설명
- `X-Account-Id` 기반 Direct API 호출
- DB timeout 커스텀 지표 분리

## Out of Scope

- Redis queued API 부하 테스트
- 테스트 결과 측정값 기록
- Grafana/Prometheus/InfluxDB 연동
- CI 부하 테스트 자동화

## Steps

1. `load-test/k6` 디렉터리를 만든다.
2. Direct API를 호출하는 k6 스크립트를 작성한다.
3. 더미 account 범위 안에서 account id를 생성한다.
4. 기본 유입량을 `500 RPS`, `preAllocatedVUs=500`, `maxVUs=1500`으로 설정한다.
5. 성공, 매진, 중복 발급, DB timeout, 서버 오류를 구분할 수 있는 check를 작성한다.
6. 실행 방법과 스크립트 의미를 문서화한다.

## Verification

- k6 스크립트 문법은 JavaScript module 형식으로 작성한다.
- Direct 스크립트는 JWT 로그인을 호출하지 않고 `X-Account-Id` 헤더를 사용한다.
- `git diff --check`를 통과한다.
- 애플리케이션 테스트는 `./gradlew test --no-daemon`으로 재확인한다.
