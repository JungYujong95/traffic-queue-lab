# JWT Login and Token Issue Plan

## Purpose

실제 계정 기반 JWT 로그인과 토큰 발급을 추가하고, k6가 발급받은 토큰으로 쿠폰 발급 API를 호출하도록 만든다.

이 단계는 Direct API와 Redis Queue API를 비교하기 전에, 인증 비용과 실제 회원 요청 흐름을 포함한 기준선을 만드는 목적이다.

## Scope

- JWT 토큰 발급 API
- Spring Security 인증 설정
- JWT 기반 쿠폰 발급 API
- k6 로그인 후 토큰 재사용
- 인증/권한 관련 테스트
- 실행 문서 업데이트

## Out of Scope

- Redis waiting queue
- Redis 기반 쿠폰 발급
- SMS/이메일 전송 비동기화
- 소셜 로그인

## Steps

1. 기존 `accounts` 테이블을 로그인 주체로 사용한다.
2. JWT 발급과 검증을 위한 Security 설정을 추가한다.
3. JWT를 사용하는 쿠폰 발급 API를 추가한다.
4. k6가 먼저 토큰을 발급받고 이후 요청에 Bearer Token을 사용하도록 수정한다.
5. 인증 관련 테스트와 문서를 추가한다.

## Verification

- 로그인 API가 계정 이메일로 JWT를 발급한다.
- JWT 보호 API는 토큰 없이 접근할 수 없다.
- Direct API는 기존처럼 비교 기준선으로 유지된다.
- k6가 실제 로그인 응답의 토큰으로 쿠폰 발급 API를 호출한다.
- 전체 테스트가 통과한다.
