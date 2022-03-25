package com.example.moiming_release;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class MoimingReleaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoimingReleaseApplication.class, args);
    }


    // Bean 이라는 것에 대해 이해하여야 이해할 수 있을 듯
    // Kakao와 통신이 필요하므로 RestTemplate Bean 추가
    @Bean
    public RestTemplate getRestTemplate(){
        return  new RestTemplate();
    }

}
