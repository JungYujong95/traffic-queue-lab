# Direct Coupon k6 Load Test

## Purpose

이 부하 테스트는 Redis 대기열 없이 Direct 쿠폰 발급 API에 요청을 집중시킨다.

목적은 DB 커넥션 풀 대기, coupon row 비관락 경합, 매진 이후의 실패 응답을 baseline으로 관찰하는 것이다.

## Target API

```http
POST /api/v1/coupons/{couponId}/issue/direct
X-Account-Id: {accountId}
```

## Local Bottleneck Setting

Direct API 병목을 쉽게 관찰하기 위해 local Hikari pool은 작게 둔다.

```env
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=3
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=3
SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=1000
```

커넥션 풀이 모두 사용 중이고 요청이 `1000ms` 안에 커넥션을 얻지 못하면 Hikari connection timeout이 발생할 수 있다.

이 timeout은 direct flow에서 DB 진입량을 제어하지 않았을 때 나타나는 병목 신호다.

애플리케이션은 DB 커넥션 획득 실패를 다음 공통 에러로 반환한다.

```json
{
  "success": false,
  "code": "DB_503_001",
  "message": "DB 커넥션을 확보하지 못했습니다. 잠시 후 다시 시도해주세요."
}
```

## Script

```text
load-test/k6/direct-coupon-issue.js
```

기본 프로파일은 `constant-arrival-rate` 기반이며, 초당 유입량을 직접 고정한다.

## Run

인프라와 애플리케이션을 먼저 실행한다.

```bash
docker compose --env-file .env.local -f docker/local/docker-compose-local.yml up -d
./gradlew bootRun
```

더미 데이터를 초기화한다.

```bash
sg docker -c 'docker compose --env-file .env.local -f docker/local/docker-compose-local.yml exec -T mysql mysql -uroot -proot_password traffic_queue_lab < src/main/resources/db/local/seed-coupon-lab.sql'
```

k6를 실행한다.

```bash
k6 run load-test/k6/direct-coupon-issue.js
```

Dockerfile로 따로 실행할 수도 있다.

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

환경변수로 대상과 부하 조건을 일부 조정할 수 있다.

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

## Script Meaning

### options

```javascript
export const options = {
  scenarios: {
    direct_coupon_issue: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.KTX_RPS || 500),
      timeUnit: '1s',
      duration: __ENV.KTX_DURATION || '2m',
      preAllocatedVUs: Number(__ENV.KTX_PREALLOCATED_VUS || 500),
      maxVUs: Number(__ENV.KTX_MAX_VUS || 1500),
    },
  },
};
```

`constant-arrival-rate`는 초당 요청 수를 고정한다.

- 기본값은 초당 500건
- 실행 시간은 기본 2분
- `preAllocatedVUs` 기본값은 500
- `maxVUs` 기본값은 1,500

이 프로파일은 500 VU 같은 정적인 동시성 수치보다 훨씬 직접적으로 “명절 KTX” 같은 밀집 상황을 만든다.
Direct API는 `X-Account-Id`로 더미 계정을 식별하므로 JWT 로그인 비용 없이 DB 직행 baseline을 관찰한다.

### thresholds

```javascript
thresholds: {
  http_req_duration: ['p(95)<5000'],
}
```

테스트가 통과했다고 볼 최소 기준이다.

- p95 응답 시간은 5초 미만

쿠폰 수량이 100,000개라서 짧은 로컬 테스트에서는 매진보다 DB 병목을 먼저 관찰하기 쉽다.
DB 병목 관찰 중에는 `503 DB_503_001`도 의도한 관찰 대상이므로 커스텀 카운터로 따로 본다.

### Environment Variables

```javascript
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const COUPON_ID = Number(__ENV.COUPON_ID || 1);
const ACCOUNT_COUNT = Number(__ENV.ACCOUNT_COUNT || 100000);
const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || 0.1);
```

스크립트를 수정하지 않고 실행 대상을 바꾸기 위한 값이다.

- `BASE_URL`: 테스트할 Spring Boot 서버 주소
- `COUPON_ID`: 발급할 쿠폰 ID
- `ACCOUNT_COUNT`: 더미 account 개수
- `THINK_TIME_SECONDS`: 요청 사이 대기 시간

## Dockerfile Meaning

`load-test/k6/Dockerfile`은 k6 공식 이미지를 기반으로 한다.

```dockerfile
FROM grafana/k6:0.57.0
WORKDIR /scripts
ARG K6_SCRIPT=direct-coupon-issue.js
COPY ${K6_SCRIPT} /scripts/load-test.js
ENTRYPOINT ["k6", "run", "/scripts/load-test.js"]
```

- `FROM grafana/k6:0.57.0`: k6 실행 환경을 그대로 사용한다.
- `WORKDIR /scripts`: 스크립트 위치를 고정한다.
- `ARG K6_SCRIPT`: 빌드할 스크립트를 바꾼다.
- `COPY ...`: 선택한 부하 테스트 스크립트를 이미지에 포함한다.
- `ENTRYPOINT ...`: 컨테이너를 실행하면 바로 선택한 k6 스크립트가 시작된다.

JWT 스크립트를 이미지로 실행하고 싶으면 이렇게 빌드한다.

```bash
docker build -t traffic-queue-k6 -f load-test/k6/Dockerfile --build-arg K6_SCRIPT=jwt-coupon-issue.js load-test/k6
```

이 방식은 로컬에 k6를 설치하지 않고도 `docker build` / `docker run`만으로 별도 실행이 가능하다는 장점이 있다.

### Account Id Generation

```javascript
function nextAccountId() {
  return ((__VU * 10007) + __ITER) % ACCOUNT_COUNT + 1;
}
```

k6의 `__VU`는 현재 가상 사용자 번호이고, `__ITER`는 해당 가상 사용자의 반복 횟수다.

이 둘을 이용해 `1`부터 `ACCOUNT_COUNT` 사이의 account id를 만든다.

더미 account가 100,000명 있으므로 account id도 `1..100000` 범위로 제한한다.

### Request

```javascript
http.post(
  `${BASE_URL}/api/v1/coupons/${COUPON_ID}/issue/direct`,
  null,
  {
    headers: {
      'X-Account-Id': String(accountId),
    },
  }
);
```

Direct 쿠폰 발급 API를 호출한다.

body는 없고, `X-Account-Id` 헤더로 더미 사용자를 식별한다.

### Response Classification

```javascript
if (response.status === 200) return 'success';
if (response.status === 404) return 'not_found';
if (response.status === 409) return classifyConflict(response);
```

응답을 의미별로 나눈다.

- `200`: 발급 성공
- `404`: account 또는 coupon 없음
- `409 + COUPON_409_001`: 쿠폰 매진
- `409 + COUPON_409_002`: 중복 발급
- `503 + DB_503_001`: DB 커넥션 획득 timeout
- 그 외: 예상하지 못한 응답

### Custom Metrics

```javascript
const successCount = new Counter('direct_issue_success_count');
const soldOutCount = new Counter('direct_issue_sold_out_count');
const duplicateCount = new Counter('direct_issue_duplicate_count');
const dbTimeoutCount = new Counter('direct_issue_db_timeout_count');
const unexpectedCount = new Counter('direct_issue_unexpected_count');
```

k6 기본 지표 외에 쿠폰 발급 도메인 기준 지표를 만든다.

부하 테스트 결과에서 단순 HTTP status만 보는 것이 아니라, 성공/매진/중복/DB timeout/예상 밖 실패를 분리해서 볼 수 있다.

## Expected Result

Direct API에서는 요청이 바로 DB로 들어간다.

부하가 증가하면 다음 현상을 관찰해야 한다.

- DB connection pool 대기 증가
- coupon row pessimistic lock 경합 증가
- p95/p99 응답 시간 증가
- 쿠폰 100,000개 소진 후 `COUPON_409_001` 증가

이 결과를 Redis 대기열 적용 후 queued API 결과와 비교한다.
