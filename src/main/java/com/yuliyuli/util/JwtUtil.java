package com.yuliyuli.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  // 密钥
  private final SecretKey SECRET_KEY = Jwts.SIG.HS512.key().build();

  // 过期时间
  private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24小时

  // 生成token
  public String generateToken(Long userId) {
    return Jwts.builder()
        .subject(userId.toString())
        .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .signWith(SECRET_KEY)
        .compact();
  }

  // 解析token
  public Claims parseToken(String token) {
    return Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
  }

  // 验证token
  public boolean validateToken(String token) {
    try {
      parseToken(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
