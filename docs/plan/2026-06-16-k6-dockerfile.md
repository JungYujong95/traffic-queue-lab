# K6 Dockerfile Plan

## Purpose

`k6`를 로컬에 따로 설치하지 않고, 저장소 안의 Dockerfile로 독립 실행할 수 있게 만든다.

## Scope

- `load-test/k6/Dockerfile` 추가
- Docker build/run 방법 문서화
- k6 스크립트와 Dockerfile 연결 방식 설명

## Out of Scope

- k6 스크립트 로직 변경
- Docker Compose 서비스 추가
- CI 자동 실행

## Steps

1. k6 공식 이미지를 기반으로 Dockerfile을 작성한다.
2. 테스트 스크립트를 이미지에 복사한다.
3. 기본 실행 명령을 direct coupon issue script로 맞춘다.
4. 문서에 build/run 예시를 추가한다.
5. `docker build`와 `docker run` 예시를 검증한다.

## Verification

- `docker build`가 성공한다.
- 컨테이너가 `direct-coupon-issue.js`를 실행할 수 있다.
