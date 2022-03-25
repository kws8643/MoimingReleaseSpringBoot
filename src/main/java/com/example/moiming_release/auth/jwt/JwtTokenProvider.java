package com.example.moiming_release.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtTokenProvider implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String AUTHORIZATION_KEY = "auth";

    private final String secret; // ="bW9pbWluZy1yZWxlYXNlLXZlcnNpb24tMjEwNjA2LWp3dC1zZWNyZXQta2V5LWNyZWF0ZWQtdG9kdGpkV0stUmtkRG5UanIK"; // 생성시 받는 secret = BASE64 로 Encoded
    private final long tokenValidationTime; //= 259200; // 토큰은 유효기간 30일로 설정.

    private Key key;

    // JwtTokenProvider 빈을 생성하고, Value 들로 의존성들을 주입받는다.
    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.token-validity-in-seconds}") long tokenValidationTime
    ) {

        this.secret = secret;
        this.tokenValidationTime = tokenValidationTime * 1000L;

    }

    // 빈이 생성후에 시행되며, 시크릿 키를 Base64로 Decode 한다.
    @Override
    public void afterPropertiesSet() throws Exception {

        byte[] keyBytes = Decoders.BASE64.decode(secret); // BASE64 Decode 한 후 Key 생성으로 사용된다.
        this.key = Keys.hmacShaKeyFor(keyBytes);

    }


    // TODO: Authorities, Claim의 관계. Jwts.setSubject(authentication.getName())의 역할?
    //       Authentication과 Token.
    public String createToken(String uuid) { // Spring Authentication의 권한 정보.

        // Header = 해당 토큰에 대한 설명
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("alg", "HS512");
        headerMap.put("typ", "JWT");

        // Payload = 담을 Data (Subject, User Data, Scope_claim (유저가 가진 권한),
        Claims payloadClaims = Jwts.claims().setSubject(uuid);
//        payloadClaims.put("roles", roles);


        long curTime = (new Date()).getTime();
        Date validationTime = new Date(this.tokenValidationTime + curTime);

        return Jwts.builder()
                .setHeader(headerMap)
                .setClaims(payloadClaims)
                .signWith(key, SignatureAlgorithm.HS512) // 지정 시크릿을 통해 생성한 Key = Signature Key.
                .setExpiration(validationTime)
                .compact();

    }


    // 토큰에서 회원 UUID 추출
    public String getUserPk(String jwtToken) {

        return Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(jwtToken).getBody().getSubject();
    }


    // 토큰의 유효성 + 만료일자 확인
    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
