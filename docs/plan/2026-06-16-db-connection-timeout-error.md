# DB Connection Timeout Error Plan

## Purpose

Direct API 부하 테스트 중 Hikari connection timeout이 발생했을 때 일반 500이 아니라 명확한 공통 에러 응답을 반환한다.

## Scope

- DB connection acquisition failure error code 추가
- `CannotGetJdbcConnectionException` 글로벌 예외 처리 추가
- 글로벌 예외 핸들러 테스트 추가
- Direct k6 문서에 에러 코드 설명 추가

## Out of Scope

- Redis 대기열 구현
- Hikari metric 수집
- DB lock wait timeout 전용 처리
- retry 정책

## Steps

1. `ErrorCode`에 DB connection timeout 계열 에러 코드를 추가한다.
2. `GlobalExceptionHandler`에서 `CannotGetJdbcConnectionException`을 처리한다.
3. MockMvc 테스트로 503 응답과 error code를 검증한다.
4. 문서에 부하 테스트 중 관찰 가능한 에러로 기록한다.
5. `./gradlew test --no-daemon`으로 검증한다.

## Verification

- DB connection acquisition failure는 HTTP 503을 반환한다.
- 응답 body는 공통 `ErrorResponse` 형식을 따른다.
- 전체 테스트가 통과한다.
