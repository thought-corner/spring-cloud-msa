# spring-cloud-msa

Spring Boot 4.1.1 + Spring Cloud 2025.1.3 기반 MSA 학습 프로젝트.
[joneconsulting/toy-msa](https://github.com/joneconsulting/toy-msa) 의 `springboot3.2` 브랜치를
Gradle 멀티모듈로 이관하면서 최신 스택에 맞게 정리한 것이다.

## 모듈

| 모듈 | 포트 | 데이터베이스 | 역할 |
|---|---|---|---|
| `service-discovery` | 8761 | - | Eureka 서버 |
| `config-service` | 8888 | - | Spring Cloud Config 서버 (native 백엔드) |
| `user-service` | 60000 | `userdb` | 회원 가입/로그인/JWT 발급, Feign 으로 order-service 조회 |
| `order-service` | 10000 | `orderdb` | 주문 생성/조회 |
| `product-service` | 20000 | `productdb` | 상품 조회 |

설정 저장소는 저장소 루트의 [`config-repo/`](config-repo) 디렉터리다.
현재는 JWT 서명 키를 담고 있다. 토큰을 검증하는 모든 서비스가 같은 값을 알아야 하므로
서비스별 설정이 아니라 config-service 를 단일 출처로 둔다.

## 사전 준비

MySQL 은 Docker 로 띄운다. 서비스별로 DB 를 분리해 두었고, 컨테이너가 처음 뜰 때
[`docker/mysql/init`](docker/mysql/init) 의 스크립트가 세 개를 생성한다.

```bash
docker compose up -d
```

호스트 3306 이 이미 쓰이고 있다면 포트를 바꿔서 띄운다. 이 경우 각 서비스도 같은 값으로 실행해야 한다.

```bash
MYSQL_PORT=3307 docker compose up -d
```

접속 정보는 환경변수로 바꿀 수 있다. 기본값은 `MYSQL_HOST=127.0.0.1`, `MYSQL_PORT=3306`,
`MYSQL_USERNAME=msa`, `MYSQL_PASSWORD=msa1234` 이다.

## 기동 순서

`config-service` → `service-discovery` → 나머지 순으로 띄운다.
user-service 는 JWT 서명 키를 config-service 에서 받으므로, config-service 가 없으면 기동에 실패한다.

```bash
./gradlew :config-service:bootRun
```

```bash
./gradlew :service-discovery:bootRun
```

```bash
./gradlew :product-service:bootRun
```

```bash
./gradlew :order-service:bootRun
```

```bash
./gradlew :user-service:bootRun
```

## 테스트

테스트는 Testcontainers 로 MySQL 컨테이너를 띄우므로 Docker 가 실행 중이어야 한다.
`docker compose` 로 띄운 MySQL 과는 별개의 일회용 컨테이너를 쓰기 때문에 개발용 데이터에 영향을 주지 않는다.

```bash
./gradlew test
```

## 동작 확인

```bash
curl -X POST http://127.0.0.1:60000/users -H 'Content-Type: application/json' -d '{"email":"tester@example.com","name":"Tester","pwd":"password123"}'
```

```bash
curl -i -X POST http://127.0.0.1:60000/login -H 'Content-Type: application/json' -d '{"email":"tester@example.com","password":"password123"}'
```

```bash
curl -X POST http://127.0.0.1:10000/orders/{userId} -H 'Content-Type: application/json' -d '{"productId":"PRODUCT-001","qty":3,"unitPrice":1500}'
```

```bash
curl http://127.0.0.1:60000/users/{userId}
```

각 서비스의 상태는 actuator 로 확인한다.

```bash
curl http://127.0.0.1:60000/actuator/health
```

## 원본과 달라진 점

- **빌드**: Maven 개별 프로젝트 → Gradle 멀티모듈. 패키지는 `com.example.*` → `com.study.*`.
- **버전**: Spring Boot 3.2 → 4.1.1, Spring Cloud 2023.0.x → 2025.1.3, Java 17 → 21.
  `bootstrap.yml` 방식은 폐기되어 `spring.config.import` 로 대체했다.
- **도메인**: `catalog` → `product`. 테이블 `catalog` → `products`, 데이터 `CATALOG-00x` → `PRODUCT-00x`.
- **데이터베이스**: H2 인메모리 → Docker MySQL 8.4. H2 의존성은 제거했고,
  테스트는 Testcontainers 로 운영과 같은 엔진에서 돌린다.
- **생성 시각**: DB 기본값(`DEFAULT CURRENT_TIMESTAMP`) 대신 `@CreationTimestamp` 로 채운다.
  MySQL 은 `datetime(6)` 컬럼의 기본값도 정밀도가 일치해야 해서 H2 에서는 통과하던 DDL 이 깨진다.
- **config-service**: 외부 GitHub 저장소 대신 저장소 안의 `config-repo/` 를 native 백엔드로 읽는다.
  JKS 기반 암호화 설정은 키스토어를 저장소에 넣지 않기 위해 제외했다.
- **Kafka 제거**: order → product 재고 차감 연동과 관련 클래스를 전부 제외했다.
- **HATEOAS 제거**: 링크가 정적이라 얻는 것이 없어 `EntityModel` 래핑과 `/users/hateoas` 를 걷어냈다.
- **데모용 코드 제거**: 튜토리얼 확인용이던 `/welcome`, `/health-check`(actuator 로 대체),
  `greeting.message`, DiscoveryClient 인스턴스 목록 로깅을 제거했다.
  `FeignErrorDecoder` 도 함께 걷어냈다. 서킷 브레이커가 Feign 예외를 모두 빈 목록 폴백으로
  삼키기 때문에 이 디코더의 결과는 호출자에게 도달하지 못한다.
- **죽은 코드 정리**: 주석으로만 남아 있던 코드를 남기지 않았다.
  Circuit Breaker 는 실제로 연결해 활성화했고, RestTemplate 직접 호출·`OrderProducer`·
  `EncryptDemo`·`CustomContainer` 는 삭제했다. Feign 의 `/orders_wrong` 경로도 정상 경로로 고쳤다.
- **order-service 패키지 구조**: 계층 이름(`controller`/`dto`/`jpa`/`vo`)으로 나뉘어 있던 것을
  `domain` / `application` / `presentation` / `infrastructure` 로 재구성했다. `Order` 애그리거트가
  총액 계산과 불변식을 직접 갖고, `Money`·`Quantity`·`ProductId`·`OrderId`·`UserId` 값 객체(record)가
  잘못된 값의 생성 자체를 막는다. setter 가 없으므로 총액이 단가×수량과 어긋난 상태를 만들 방법이 없다.
  영속 모델(`OrderJpaEntity`)은 도메인 모델과 분리되어 있고, `OrderRepository` 포트를
  `OrderRepositoryAdapter` 가 구현한다. 생성 시각은 주입된 `Clock` 으로 도메인이 기록한다.
  리치 도메인 객체에는 ModelMapper 를 쓸 수 없어 명시적 매핑으로 바꿨고, 해당 의존성을 제거했다.
- **금액 타입**: `int` 는 약 21.5억이 상한이라 고가 주문을 담지 못한다. `Money` 를 `long` 으로 바꾸고
  DB 컬럼도 `bigint` 로 넓혔다. 곱셈은 `Math.multiplyExact` 로 오버플로를 잡아 잘못된 입력으로 거부한다.
- **오류 계약**: 오류는 `ErrorCode` 인터페이스(`code`/`message`/`httpStatus`)와 이를 구현한
  `OrderErrorCode` enum 하나로 도메인 집약적으로 표현한다. 예외 클래스를 규칙마다 늘리는 대신
  `OrderException` 하나가 `ErrorCode` 를 들고 다니고, 각 코드가 자신의 HTTP 상태와 정적 메시지를
  직접 선언하므로 규칙이 늘어도 예외 핸들러는 바뀌지 않는다. 응답은 RFC 7807 `ProblemDetail` 에
  `code` 를 실어 클라이언트가 메시지 문자열이 아니라 코드로 분기할 수 있게 했다.
  코드 유일성과 계약(400 분류, `ORDER-` 접두어, 메시지 존재)은 테스트로 강제한다.

  | code | 의미 |
  |---|---|
  | `ORDER-4000` | 요청 형식 오류 (필수값 누락) |
  | `ORDER-4001` | 수량이 0 이하 |
  | `ORDER-4002` | 금액이 음수 |
  | `ORDER-4003` | 총액이 표현 범위 초과 |
  | `ORDER-4004` | 식별자가 비어 있음 |
  | `ORDER-4005` | 단가가 0 이하 |
- **설정 외부화**: 서킷 브레이커 임계값, 허용 IP 대역, Eureka·MySQL 주소를 설정으로 옮겼다.
  JWT 설정은 `TokenProperties` 로 바인딩하며 512비트 미만 키는 기동 시 거부한다.

## 알려진 제약

- `config-repo/application.yml` 의 JWT 서명 키는 평문 개발용 값이다. 운영에서는 교체가 필요하다.
- `docker-compose.yml` 의 계정/비밀번호도 로컬 개발용 기본값이다.
