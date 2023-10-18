package com.kusitms.mainservice.domain.user.security.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private String secretKey;
    private long validityInMilliseconds;
    private final long ACCESS_TOKEN_VALID_TIME = (1000*60*60*24); //day
    private final long REFRESH_TOKEN_VALID_TIME = (1000*60*60*24*7); //week

    public JwtTokenProvider(@Value("&{security.jwt.token.secret-key}") String secretKey, @Value("${security.jwt.token.expire-length}") long validityInMilliseconds) {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
    }
    public TokenInfo createAccessToken(Long id) {
        return createToken(id , ACCESS_TOKEN_VALID_TIME);
    }

    public TokenInfo createRefreshToken(Long id) {
        return createToken(id, REFRESH_TOKEN_VALID_TIME);
    }

    // 토큰 생성
    public TokenInfo createToken(Long id, long validTime) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(id));

        Date now = new Date();
        Date validity = new Date(now.getTime() + validTime); // 유효기간 계산 (지금으로부터 + 유효시간)
        logger.info("now: {}", now);
        logger.info("validity: {}", validity);

        String token = Jwts.builder()
                .setClaims(claims) // sub 설정
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화방식?
                .compact();

        token = "Bearer " + token;
        return new TokenInfo(token, validity.getTime()- now.getTime());
    }

    // 토큰에서 값 추출
    public Long getSubject(String token) {
        return Long.valueOf(Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }

    // 유효한 토큰인지 확인
    public boolean validateToken(String token) {
        Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
        return !claims.getBody().getExpiration().before(new Date());
    }
}