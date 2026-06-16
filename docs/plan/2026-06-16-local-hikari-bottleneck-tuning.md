# Local Hikari Bottleneck Tuning Plan

## Purpose

Direct API 부하 테스트에서 DB connection pool 병목이 더 명확히 보이도록 local Hikari pool 설정을 줄인다.

## Scope

- `.env.local` 로컬 실험값 변경
- `.env.local.example` 기본 예시값 변경
- Docker/성능 문서에 connection pool 병목 설정 설명 추가

## Out of Scope

- 애플리케이션 로직 변경
- Redis 대기열 구현
- connection timeout 예외 전용 핸들러 구현
- k6 부하 강도 변경

## Steps

1. Hikari maximum pool size를 3으로 낮춘다.
2. Hikari minimum idle을 3으로 낮춘다.
3. Hikari connection timeout을 1000ms로 낮춘다.
4. 문서에 병목 시 connection timeout 가능성을 기록한다.
5. `./gradlew test --no-daemon`으로 설정 변경이 테스트에 영향 없는지 확인한다.

## Verification

- local env 파일에 pool size 3, timeout 1000ms가 반영된다.
- 전체 테스트가 통과한다.
