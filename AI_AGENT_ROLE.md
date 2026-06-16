# AGENTS.md

# Development Constitution

본 프로젝트의 모든 설계, 구현, 테스트 및 문서화 작업은 아래 원칙을 반드시 준수한다.

---

## 1. Object-Oriented Design Principles

* 모든 설계와 구현은 SOLID 원칙을 준수한다.
* 객체는 단일 책임(SRP)을 가져야 한다.
* 변경에는 닫혀 있고 확장에는 열려 있도록(OCP) 설계한다.
* 상위 타입과 하위 타입의 계약을 준수한다(LSP).
* 클라이언트는 자신이 사용하지 않는 인터페이스에 의존하지 않는다(ISP).
* 구체 클래스가 아닌 추상화에 의존하도록 설계한다(DIP).
* 객체 간 결합도(Coupling)는 낮추고 응집도(Cohesion)는 높인다.
* 비즈니스 로직은 객체가 자신의 상태와 행위를 스스로 관리하도록 캡슐화한다.

---

## 2. Clean Code Principles

* 코드는 사람이 읽기 쉬워야 한다.
* 의미 있는 이름(Naming)을 사용한다.
* 함수는 하나의 책임만 수행한다.
* 함수와 클래스는 가능한 작게 유지한다.
* 중복(Duplication)을 제거한다.
* 매직 넘버와 하드코딩을 지양한다.
* 불필요한 추상화와 복잡성을 만들지 않는다.
* 코드보다 의도가 드러나는 설계를 우선한다.

---

## 3. Test Driven Development

* 모든 기능 구현은 반드시 TDD(Test Driven Development) 방식을 따른다.

### Development Cycle

1. 실패하는 테스트 작성 (Red)
2. 테스트를 통과하는 최소한의 코드 작성 (Green)
3. 리팩토링 수행 (Refactor)

* 테스트가 없는 기능은 구현이 완료된 것으로 간주하지 않는다.
* 비즈니스 로직은 반드시 테스트 가능하도록 설계한다.
* 테스트는 문서이자 명세(Specification)로 취급한다.
* 테스트 코드는 작성 후 테스트를 돌리고 컴파일을 진행한다.

---

## 4. Database Query Principles

* N+1 Query는 절대 허용하지 않는다.
* 연관 관계 조회 시 항상 SQL 실행 계획을 확인한다.
* Fetch Join, EntityGraph, Batch Size 등을 상황에 맞게 사용한다.
* 성능 문제가 의심되는 경우 SQL 로그 및 실행 계획을 검증한다.
* ORM이 생성하는 쿼리를 항상 신뢰하지 않고 직접 확인한다.

---

## 5. Concurrency Principles

* 동시성 환경에서는 항상 Race Condition 발생 가능성을 고려한다.
* 공유 자원에 대한 접근은 반드시 검증한다.
* 데이터 정합성이 요구되는 경우 아래 전략을 적극 검토한다.

### Lock Strategy

* Optimistic Lock (@Version)

* Pessimistic Lock (for update)

* Distributed Lock (Redis, Redisson)

* Atomic Operation

* 성능보다 데이터 정합성이 우선되는 상황에서는 명시적인 Lock 사용을 허용한다.

* 동시성 로직 구현 후에는 반드시 부하 테스트 및 경쟁 상황 테스트를 수행한다.

---

## 6. Documentation Principles

모든 기능 개발이 완료되면 반드시 문서화를 수행한다.

### API Documentation

* Swagger(OpenAPI)
* Request/Response Example
* Error Specification

### Code Documentation

* JavaDoc
* Architecture Description
* Sequence Diagram (필요 시)

문서가 존재하지 않는 기능은 완료된 기능으로 간주하지 않는다.

---

## 7. Technical Proposal Review

사용자가 제안한 설계, 아키텍처 또는 구현 방식이 아래와 같은 경우에는 그대로 수행하지 않는다.

* 성능 저하가 예상되는 경우
* 유지보수성이 떨어지는 경우
* 확장성이 부족한 경우
* 보안 또는 데이터 정합성 문제가 발생할 수 있는 경우
* 오버엔지니어링 또는 과소설계가 발생하는 경우

더 나은 대안이 존재하면 반드시 근거와 Trade-off를 설명하고 역제안한다.

---

## 8. Avoid Over Engineering

* 현재 요구사항을 해결하기 위한 최소한의 설계를 우선한다.
* YAGNI(You Aren't Gonna Need It) 원칙을 따른다.
* 불필요한 추상화, Generic, Design Pattern 남용을 지양한다.
* 미래의 불확실한 요구사항을 위해 과도한 구조를 만들지 않는다.
* 단순한 해결책(Simple Solution)을 우선 선택한다.

---
## 9. Business Exception Principles

비즈니스 예외 처리는 단순히 예외를 발생시키는 것이 아니라, 도메인 규칙과 실패 원인을 명확하게 표현할 수 있어야 한다.

### Exception Design Rules

- RuntimeException을 직접 사용하지 않는다.
- 비즈니스 상황별 Custom Exception을 정의한다.
- 예외 이름만 보고도 실패 원인을 이해할 수 있어야 한다.
- 예외 메시지는 사용자와 개발자가 모두 이해할 수 있도록 명확하게 작성한다.
- HTTP Status와 Error Code를 일관성 있게 관리한다.
- Exception Message를 하드코딩하지 않고 ErrorCode Enum으로 중앙 관리한다.
- 예외 발생은 최대한 도메인 계층에서 수행하여 비즈니스 규칙을 명확히 드러낸다.
- Controller에서는 비즈니스 로직을 처리하지 않으며, Global Exception Handler를 통해 예외 응답을 일관되게 처리한다.

---
## 10. Interface and Implementation Separation Principles

인터페이스와 구현체 분리는 기본 원칙이 아니라, 변경 가능성(Changeability), 확장 가능성(Extensibility), 테스트 가능성(Testability)이 존재하는 경우에만 적용한다.

### Interface Separation Criteria

다음 조건 중 하나라도 만족하는 경우 인터페이스와 구현체를 분리한다.

- 구현체가 2개 이상 존재하거나 추가될 가능성이 있는 경우
- 외부 인프라(Redis, S3, Message Queue, OAuth, SMTP 등)에 의존하는 경우
- 테스트를 위해 Mock, Stub, Fake 객체 주입이 필요한 경우
- 기술 스택 변경 가능성이 존재하는 경우
- 비즈니스 정책 또는 알고리즘 교체 가능성이 존재하는 경우
- 동시성 처리, Queue 처리, Lock 처리 등 테스트 격리가 중요한 경우

### Examples

- Redis List → Redis Stream 변경 가능
- Local Storage → S3 변경 가능
- Kakao OAuth → Google OAuth 추가 가능
- Email Sender → Slack Sender 추가 가능
- Single Consumer → Multi Consumer 전략 변경 가능
- Optimistic Lock → Distributed Lock 전략 변경 가능

위와 같은 경우에는 반드시 Port(Interface)와 Adapter(Implementation)를 분리한다.

### Recommended Structure

```text
infra
├── port
│   └── WaitingQueuePort.java
└── adapter
    └── RedisWaitingQueueAdapter.java
```

```text
application
├── WaitingQueueService.java
└── RedisWaitingQueueService.java
```

### Service Interface Rule

Service 계층 역시 테스트 대역(Mock, Stub, Fake) 주입이 필요하거나 정책 교체 가능성이 존재하는 경우 인터페이스와 구현체를 분리한다.

인터페이스 분리 대상:

- 외부 인프라와 강하게 연결되는 Service
- 테스트에서 Fake 구현체를 주입해야 하는 Service
- 정책, 알고리즘, 처리 전략이 교체될 가능성이 있는 Service
- 구현체가 2개 이상 존재하거나 확장 가능성이 높은 Service
- 동시성 제어, Queue 처리, Lock 처리처럼 테스트 격리가 중요한 Service

예시:

```text
WaitingQueueService
├── RedisListWaitingQueueService
└── RedisStreamWaitingQueueService

DistributedLockService
├── RedisDistributedLockService
└── DatabaseDistributedLockService
```

### Interface Separation Exclusions

다음과 같은 경우에는 인터페이스를 생성하지 않는다.

- 구현체가 하나뿐이며 변경 가능성이 없는 경우
- 단순 CRUD Service
- 단순 Utility Class
- 단순 Domain Service
- Controller와 Repository를 단순 연결하는 Application Service
- 미래에 사용할 수도 있다는 추측만으로 생성하는 경우

다음과 같은 코드는 금지한다.

```java
public interface OrderService {}

@Service
public class OrderServiceImpl implements OrderService {
}
```

구현체가 하나이고 변경 가능성이 없다면 불필요한 추상화(Over Engineering)로 간주한다.

### Principle

인터페이스는 DIP를 위한 도구이지, 계층마다 의무적으로 생성해야 하는 템플릿이 아니다.

추상화는 실제 변화가 예상되는 지점에만 적용한다.

YAGNI(You Aren't Gonna Need It) 원칙을 우선하며, 변화 가능성이 없는 곳에는 단순한 구조를 선택한다.

목표는 Interface의 개수를 늘리는 것이 아니라, 변경 비용(Change Cost)을 낮추고 테스트 가능성(Testability)을 높이는 것이다.

---

## 11. Documentation Synchronization

모든 작업이 종료되면 반드시 아래 문서를 최신 상태로 유지한다.

### Required Update Targets

* `/docs/pattern`
* `/docs/architecture`

다음 항목을 반영한다.

* Architecture 변경 사항
* 신규 Pattern 적용 사항
* Trade-off 및 의사결정 이유
* 성능 개선 및 부하 테스트 결과
* 동시성 처리 전략
* 예외 처리 정책

코드와 문서 간 불일치는 허용하지 않는다.


---

## 12. Session Bootstrap Rule

모든 작업 시작 전 반드시 본 AGENTS.md를 처음부터 끝까지 다시 읽고 핵심 원칙을 상기한다.

특히 아래 항목을 작업 내내 지속적으로 준수한다.

- SOLID 원칙 준수
- Clean Code 원칙 준수
- TDD 기반 개발
- N+1 Query 방지
- 동시성 및 Race Condition 검토
- 문서화 의무
- 더 나은 설계에 대한 적극적인 역제안
- Over Engineering 지양
- Pattern 및 Architecture 문서 최신화

작업 중 설계, 구현, 테스트 또는 리팩토링 방향이 본 강령과 충돌하는 경우, 구현을 진행하기 전에 반드시 강령을 우선 기준으로 재검토하고 수정한다.

본 AI_AGENT_ROLE.md는 단순 참고 문서가 아니라 프로젝트의 헌법(Constitution)이며, 모든 의사결정의 최상위 기준으로 취급한다.

---



# Final Rule

> Readability > Cleverness
> Simplicity > Over Engineering
> Correctness > Premature Optimization
> Data Integrity > Performance
> Testability and Documentation are not optional.
