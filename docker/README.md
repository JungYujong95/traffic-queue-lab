# Docker Local Guide

이 문서는 로컬 Docker 환경에서 MySQL, Redis, Spring Boot 백엔드 인스턴스를 실행하는 방법을 설명한다.

## 파일 구성

```text
docker
├── README.md
└── local
    ├── Dockerfile.backend
    └── docker-compose-local.yml
```

- `docker/local/docker-compose-local.yml`: 로컬 실행용 Compose 파일
- `docker/local/Dockerfile.backend`: Spring Boot 백엔드 이미지 빌드 파일
- `.env.local`: 로컬 실행 환경변수
- `.env.local.example`: 공유 가능한 환경변수 예시

## 실행 전 준비

처음 실행한다면 `.env.local.example`을 기준으로 `.env.local`을 준비한다.

```bash
cp .env.local.example .env.local
```

`.env.local`은 Git에 커밋하지 않는다.

## 인프라만 실행

MySQL과 Redis만 실행한다.

```bash
docker compose --env-file .env.local -f docker/local/docker-compose-local.yml up -d
```

이 명령은 Spring Boot 백엔드를 띄우지 않는다. 로컬 IDE에서 애플리케이션을 직접 실행하면서 MySQL/Redis만 Docker로 사용할 때 사용한다.

실행되는 서비스:

- `mysql`
- `redis`

## 인프라와 백엔드 4개 실행

부하 테스트나 대기열 실험을 위해 MySQL, Redis, Spring Boot 백엔드 4개를 모두 실행한다.

```bash
docker compose --env-file .env.local -f docker/local/docker-compose-local.yml --profile backend up --build
```

실행되는 서비스:

- `mysql`
- `redis`
- `app-8080`
- `app-8081`
- `app-8082`
- `app-8083`

접속 포트:

```text
http://localhost:8080
http://localhost:8081
http://localhost:8082
http://localhost:8083
```

## 종료

컨테이너를 종료한다.

```bash
docker compose --env-file .env.local -f docker/local/docker-compose-local.yml down
```

볼륨까지 삭제하려면 아래 명령을 사용한다. MySQL/Redis 데이터도 삭제된다.

```bash
docker compose --env-file .env.local -f docker/local/docker-compose-local.yml down -v
```

## 메모리 제한

이 프로젝트는 작은 Spring Boot 애플리케이션을 4개 띄워 병목과 대기열을 실험한다. 컨테이너별 메모리를 제한하지 않으면 4개의 JVM이 로컬 머신 메모리를 과하게 사용할 수 있다.

기본 제한값:

```env
BACKEND_MEMORY_LIMIT=512m
MYSQL_MEMORY_LIMIT=512m
REDIS_MEMORY_LIMIT=128m
```

- 백엔드 컨테이너 1개당 `512m`
- 백엔드 4개 실행 시 백엔드 컨테이너 제한 합계는 약 `2g`
- MySQL은 `512m`
- Redis는 `128m`

## JVM 힙 제한

컨테이너 메모리 제한만 걸면 JVM이 항상 원하는 방식으로 힙을 잡는다고 가정하기 어렵다. 그래서 백엔드에는 `JAVA_TOOL_OPTIONS`를 함께 설정한다.

기본값:

```env
BACKEND_JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=20.0 -XX:+ExitOnOutOfMemoryError
```

의미:

- `MaxRAMPercentage=60.0`: 컨테이너 메모리 제한의 최대 60%까지 힙으로 사용
- `InitialRAMPercentage=20.0`: 초기 힙을 컨테이너 메모리 제한의 20% 수준으로 시작
- `ExitOnOutOfMemoryError`: OOM 발생 시 프로세스를 종료해서 비정상 상태로 계속 떠 있지 않게 함

현재 기본값 기준으로 백엔드 컨테이너 1개는 `512m` 제한을 받고, JVM 최대 힙은 약 `307m` 수준이다.

```text
512m * 60% = 약 307m
```

## 메모리 조정 방법

`.env.local`에서 값을 바꾼다.

백엔드 컨테이너 메모리를 줄이는 예:

```env
BACKEND_MEMORY_LIMIT=384m
BACKEND_JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=20.0 -XX:+ExitOnOutOfMemoryError
```

백엔드 컨테이너 메모리를 늘리는 예:

```env
BACKEND_MEMORY_LIMIT=768m
BACKEND_JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=20.0 -XX:+ExitOnOutOfMemoryError
```

힙 비율만 줄이는 예:

```env
BACKEND_MEMORY_LIMIT=512m
BACKEND_JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=50.0 -XX:InitialRAMPercentage=15.0 -XX:+ExitOnOutOfMemoryError
```

MySQL과 Redis 메모리를 조정하는 예:

```env
MYSQL_MEMORY_LIMIT=768m
REDIS_MEMORY_LIMIT=256m
```

## 실험 의도

백엔드 인스턴스는 4개를 띄운다. 각 인스턴스는 작은 DB 커넥션 풀을 사용한다.

```env
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=10
```

이 설정은 DB 접근 경로에 의도적인 병목을 만들기 위한 값이다. Redis는 모든 백엔드 인스턴스가 공유하는 대기열 저장소로 사용한다.

## 상태 확인

실행 중인 컨테이너를 확인한다.

```bash
docker compose --env-file .env.local -f docker/local/docker-compose-local.yml ps
```

로그를 확인한다.

```bash
docker compose --env-file .env.local -f docker/local/docker-compose-local.yml logs -f
```

백엔드 로그만 확인한다.

```bash
docker compose --env-file .env.local -f docker/local/docker-compose-local.yml logs -f app-8080 app-8081 app-8082 app-8083
```
