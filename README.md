# spring-cloud-msa

Spring Boot 4.1.1 + Spring Cloud 2025.1.3 기반 MSA 학습 프로젝트.

## 모듈

| 모듈                | 포트  | 데이터베이스 | 역할                                                     |
|---------------------|-------|--------------|----------------------------------------------------------|
| `apigateway`        | 8000  | -            | 단일 진입점, JWT 엣지 검증, Eureka 라우팅                |
| `service-discovery` | 8761  | -            | Eureka 서버                                              |
| `config-service`    | 8888  | -            | Spring Cloud Config 서버 (native 백엔드)                 |
| `user-service`      | 60000 | `userdb`     | 회원 가입/로그인/JWT 발급, Feign 으로 order-service 조회 |
| `order-service`     | 10000 | `orderdb`    | 주문 생성/조회                                           |
| `product-service`   | 20000 | `productdb`  | 상품 조회                                                |

## 1. 설정 암호화 문제

**[문제]** 

- 학습 차원에서 작성했었던 설정 저장소 (`config-repo/`)의 비밀 값이 평문으로 커밋되어 있었습니다.
- git 히스토리는 지워지지 않으므로, 저장소를 읽을 수 있는 사람 (클론·포크·히스토리 열람)은 누구나 비밀을 획득할 수 있고, 커밋 한 번의 노출이 영구 노출이 됩니다.

**[근거]** 

- Spring Cloud Config 는 `{cipher}` 접두어 값을 서빙 시점에 복호화하는 저장 시점 암호화를 지원합니다.
- 암호문만 git 에 두고 복호화 키를 저장소 밖에 두면 "저장소 열람 = 비밀 획득"이라는 연결 고리가 끊어집니다. 
암호문을 형상 관리에 두고 키를 외부에 두는 이 구조는 실무 도구 (SOPS, Sealed Secrets)와 같은 형태이며, 실무에서는 키 보관처가 KMS/볼트가 됩니다.

**[해결방안]** 

- 비밀 값은 `{cipher}` 암호문으로 저장하고, config-service가 `keystore/config-encrypt-dev.jks`(RSA)로 복호화해 서빙합니다. 
- `/encrypt`·`/decrypt` 는 basic auth 뒤에 있습니다.
- 새 값 암호화 : config-service 기동 후 `curl -u config:config1234 -X POST http://127.0.0.1:8888/encrypt --data-raw '<평문>'`
- 키스토어 재생성: `keytool -genkeypair -alias config-encrypt-key -keyalg RSA -keysize 2048 -keystore keystore/config-encrypt-dev.jks -storepass config-encrypt-pass`
- 이 때, 재생성하면 기존 암호문은 전부 다시 암호화해야 합니다.

**[트레이드오프]**

- 키스토어와 비밀번호를 **개발 전용**으로 저장소에 포함했습니다. 
- 암호문과 복호화 키가 같은 저장소에 있으면 보안 이득은 0 이므로, 이 구성의 목적은 메커니즘 학습과 클론 즉시 실행입니다. 
- 운영에서는 키스토어를 저장소 밖 (볼트/KMS)에 두고 `CONFIG_ENCRYPT_KEYSTORE_PASSWORD`로 비밀번호를 주입해야 실제 보호가 성립합니다.
- 비밀은 소멸하지 않고 이동합니다: git (암호문) → 키스토어 (복호화 키) → 환경변수 (키스토어 비밀번호). 암호화는 노출 범위를 좁히는 것이지 비밀 관리 자체를 없애 주지 않습니다.

## 2. JWT 서명 키 (RS256 + kid)

**[문제]** 

- 기존 HS512는 대칭키라서 검증 키가 곧 서명 키가 됩니다. 
- 검증자 (apigateway 등)가 늘수록 "키를 읽으면 토큰을 위조할 수 있는 지점"이 함께 늘어나, 가장 방어가 약한 서비스 하나가 인증 체계 전체의 방어 수준을 결정합니다. 
- 또한 단일 키 구조에서는 로테이션 시 기존 토큰이 전부 즉시 무효가 되어 (전 사용자 강제 로그아웃), 키 교체가 부담스러운 작업이 됩니다.

**[근거]** 

- RS256은 서명(개인키)과 검증(공개키)을 분리합니다. 
- 공개키는 유출되어도 위조에 쓸 수 없으므로, 검증자가 늘어도 위조 가능 지점은 발급자 한 곳으로 고정됩니다. 
- 토큰 헤더의 `kid` 로 검증 키를 조회하면 여러 키가 공존할 수 있어, 새 키로 전환한 뒤에도 구 키 토큰을 유예 기간 동안 수용하는 무중단 로테이션이 가능합니다.

**[해결방안]** 

- 개인키는 발급자 (user-service)에게만 서빙되고 (`config-repo/user-service.yml`, 암호문), 검증자는 공개키만 받습니다.
- (`config-repo/application.yml`, 평문 — 공개키는 비밀이 아닙니다). 키 설정은 불변 스냅샷 (record) + AtomicReference 교체로 갱신하며, 검증 실패 시 기존 스냅샷을 유지합니다(fail-safe).

> 무중단 키 로테이션 절차 (기존 토큰 강제 로그아웃 없음):

1. 새 키쌍 생성 → 공개키를 `public-keys` 에 추가, 개인키를 암호화해 `private-keys` 에 추가, `active-kid` 를 새 kid 로 변경 → busrefresh 1회 (신규 발급은 새 키, 구 토큰은 유예 검증)
2. 토큰 유효기간 경과 후 구 kid 항목 제거 → busrefresh 1회 (구 키 완전 폐기)

**[트레이드오프]**

- RSA는 HMAC보다 서명·검증이 느리고 토큰이 큽니다. 검증자 확장과 유예 로테이션이라는 구조적 이득을 위해 지불한 비용입니다.
- 유예 기간에는 구 키 토큰이 계속 유효한 상황입니다. 키 유출 대응처럼 즉시 차단이 필요한 상황에서는 유예 없이 구 kid 를 바로 제거하고 전 사용자 재로그인을 감수하는 판단이 필요합니다.
- 공개키 배포가 아직 config-service를 경유합니다. 발급자가 직접 공개키를 노출하는 JWKS 엔드포인트 (`/.well-known/jwks.json`)로 옮기는 것이 다음 단계 포인트로 설정했습니다.
