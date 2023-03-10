package io.security.oauth2.core;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 * SecurityConfig (스프링 시큐리티 설정클래스)
 * (1) SecurityConfigurer를 커스텀한 클래스로 사용하여 커스텀한 기능의 인증 및 인가처리가 진행되도록 한다
 * (2) 커스텀 설정에 의한 초기화 과정
 * (3) 커스텀 설정한 AuthenticationEntryPoint를 설정할 경우
 * (4) HTTPBasic 인증 API 설정
 * */
public class SecurityConfig {

    /*
    * (1)
    * - SecurityFilterChain을 반환하는 HttpSecurity(SecurityBuilder)를 빈으로 생성
    * - 내부에서 SecurityConfigurer를 생성 및 기타 초기화 작업 설정
    * - apply() 메소드의 파라미터에 생성할 SecurityConfigurer 구현체를 주입
    *
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().authenticated();
        http.formLogin();
        http.apply(new CustomSecurityConfigurer().setFlag(true));

        (3)
        - 익명 내부 클래스를 통해 AuthenticationEntryPoint의 구현체를 직접 커스텀하여 설정

        http.exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                System.out.println("cutom entryPoint..");
            }
        });

        return http.build();
    }*/

    /*
    * (4)
    * - HttpSecurity 를 통해 SecurityFilterChain이 생성될 때 API를 통해 HTTPBasic 인증 방식 설정
    * - HttpAuthenticationEntryPoint 커스텀하여 적용
    * - Session 사용여부에 따라 요청시마다 인증과정을 다시 거칠 것인지 여부가 결정된다(세션이 있다면 SecurityContext 내부의 인증객체에 접근가능)
    * */
   @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().authenticated();
        http.httpBasic().authenticationEntryPoint(new CustomAuthenticationEntryPoint());
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }
}
