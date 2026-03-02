package com.job.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import io.jsonwebtoken.Claims;
import com.job.dto.User;
import com.job.dto.UserHolder;
import com.job.util.JwtUtil;

import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginIntercepter implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    private RedisTemplate<String, Object> redisTemplate;

    public LoginIntercepter(JwtUtil jwtUtil, RedisTemplate<String, Object> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }
    
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (token == null || !jwtUtil.validateToken(token)) {
            return false;
        }
        String key = "login:token:" + token;
        User user = (User) redisTemplate.opsForValue().get(key);
        if (user == null) {
            return false;
        }

        UserHolder.setUser(user);
        Claims claims = jwtUtil.parseToken(token);
        request.setAttribute("userId", claims.get("userId"));
        return true;
    }
}
