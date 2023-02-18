package io.security.oauth2.core;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/*
* CustomSecurityConfigurer (SecurityConfigurer 구현체)
* - SecurityConfigurer 를 커스텀한 구현체를 생성하여 내부에서 init() & configure() 메소드를 커스텀함으로써 커스텀한 기능의 인증 및 인가처리가 되도록 초기화한다
* */
public class CustomSecurityConfigurer extends AbstractHttpConfigurer<CustomSecurityConfigurer, HttpSecurity> {

    private boolean isSecured;

    @Override
    public void init(HttpSecurity builder) throws Exception {
        super.init(builder);
        System.out.println("custom init method started..");
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        super.configure(builder);
        System.out.println("custom configure method started..");
        if (isSecured) {
            System.out.println("http is required..");
        } else {
            System.out.println("http is optional..");
        }
    }

    public CustomSecurityConfigurer setFlag(boolean isSecured) {
        this.isSecured = isSecured;
        return this;
    }
}
