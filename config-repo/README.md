# config-repo

config-service(Spring Cloud Config Server)가 `native` 백엔드로 읽어가는 설정 저장소입니다.

- `application.yml` : 모든 서비스가 공유하는 설정
- `{application-name}.yml` : 해당 서비스 전용 설정
- `{application-name}-{profile}.yml` : 프로파일별 설정

파일을 수정한 뒤 클라이언트의 `POST /actuator/refresh` 를 호출하면 재배포 없이 값이 다시 바인딩됩니다.
단, `record` 처럼 생성자 바인딩으로 만든 `@ConfigurationProperties` 는 기존 인스턴스를 재사용하므로
갱신되지 않습니다. 갱신이 필요한 값은 setter 를 가진 클래스로 바인딩해야 합니다.

git 백엔드로 바꾸려면 `config-service/src/main/resources/application.yml` 의
`spring.profiles.active` 를 지우고 `spring.cloud.config.server.git.uri` 를 지정하면 됩니다.
