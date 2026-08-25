package com.study.common.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * 검증자 서비스가 이 모듈을 의존하기만 하면 토큰 검증 빈이 자동 등록되도록 한다.
 * 컴포넌트 스캔 범위를 넓히지 않고(auto-configuration) 공유 보안 코드를 주입한다.
 */
@AutoConfiguration
public class TokenVerificationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TokenKeyHolder tokenKeyHolder(Environment environment) {
        return new TokenKeyHolder(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenVerifier tokenVerifier(TokenKeyHolder tokenKeyHolder) {
        return new TokenVerifier(tokenKeyHolder);
    }
}
