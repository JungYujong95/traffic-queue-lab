# Coupon Account Schema Plan

## Purpose

쿠폰 발급 부하 테스트를 위한 최소 도메인 테이블과 JPA 엔티티를 만든다.

Redis 대기열과 서비스 로직은 아직 구현하지 않고, DB 병목 실험에서 사용할 기본 데이터 구조와 로컬 더미 데이터 SQL만 준비한다.

## Scope

- `Account` 엔티티와 `accounts` 테이블 매핑
- `Coupon` 엔티티와 `coupons` 테이블 매핑
- `CouponIssue` 엔티티와 `coupon_issues` 테이블 매핑
- 쿠폰 중복 발급 방지를 위한 `coupon_id + account_id` 유니크 제약
- 로컬 부하 테스트용 더미 `accounts`, `coupons` 생성 SQL
- 엔티티 매핑 검증 테스트
- schema 및 seed data 문서화

## Out of Scope

- 쿠폰 발급 서비스 로직
- Redis 대기열 로직
- API 컨트롤러
- 인증/JWT/회원가입
- 부하 테스트 스크립트

## Steps

1. 도메인 패키지 구조를 만든다.
2. Account, Coupon, CouponIssue 엔티티를 작성한다.
3. CouponIssueStatus enum을 작성한다.
4. 로컬 더미 데이터 SQL 파일을 작성한다.
5. JPA 매핑 테스트를 작성한다.
6. docs에 테이블 구조와 더미 데이터 사용 방법을 문서화한다.
7. Gradle test를 실행한다.

## Verification

- `./gradlew test --no-daemon`
- 엔티티 매핑 테스트에서 account, coupon, coupon_issue 저장 검증
- SQL 파일 문법은 MySQL 8 기준으로 작성
