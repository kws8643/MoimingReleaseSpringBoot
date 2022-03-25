
package com.example.moiming_release.config;

/*
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutFilter;


@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable();

        http.addFilterAfter(jwtAuthenticationFilter, LogoutFilter.class);

        http.authorizeRequests()
                .mvcMatchers("/", "/api/**","/signUp", "/access-denied", "/exception/**").permitAll()
                .anyRequest().authenticated()  // 그 이외는 다 인증이 필요
                .expressionHandler(configExpressionHandler()); //사용자 권한의 계층 구조를 정하는 클래스를 지정

        http.exceptionHandling()
                .authenticationEntryPoint(configAuthenticationEntryPoint()) // 인증되지 않은 사용자가 인증이 필요한 URL에 접근할 경우
                .accessDeniedHandler(configAccessDeniedHandler()); // 인증한 사용자가 추가 권한이 필요한 URL에 접근할 경우 발생


        // OAuth2 인증 과정에서 Authentication을 생성에 필요한 OAuth2User를 반환하는 클래스를 지정
        http.oauth2Login()
                .userInfoEndpoint().userService(customOAuth2UserService)
                .and()
                .successHandler(configSuccessHandler()) // 인증을 성공적으로 마친 경우
                .failureHandler(configFailureHandler()) // 인증에 실패한 경우
                .permitAll();

        http.httpBasic();

        http.logout()
                .deleteCookies("JSESSIONID");
    }

  *//*  private SecurityExpressionHandler<FilterInvocation> configExpressionHandler() {

    }

    private CustomAuthenticationEntryPoint configAuthenticationEntryPoint() {

    }

    private CustomAccessDeniedHandler configAccessDeniedHandler() {
    }

    private CustomAuthenticationSuccessHandler configSuccessHandler() {
    }

    private CustomAuthenticationFailureHandler configFailureHandler() {
    }

    *//*

}*/
