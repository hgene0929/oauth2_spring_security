package io.security.oauth2.core;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/*
 * SecurityConfig (스프링 시큐리티 설정클래스)
 * (1) SecurityConfigurer를 커스텀한 클래스로 사용하여 커스텀한 기능의 인증 및 인가처리가 진행되도록 한다
 * */
public class SecurityConfig {

    /*
    * (1)
    * - SecurityFilterChain을 반환하는 HttpSecurity(SecurityBuilder)를 빈으로 생성
    * - 내부에서 SecurityConfigurer를 생성 및 기타 초기화 작업 설정
    * - apply() 메소드의 파라미터에 생성할 SecurityConfigurer 구현체를 주입
    * */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().authenticated();
        http.formLogin();
        http.apply(new CustomSecurityConfigurer().setFlag(true));

        return http.build();
    }
}
