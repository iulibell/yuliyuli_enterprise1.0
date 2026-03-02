package com.job.util;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtIntercepter implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtIntercepter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (token == null || !jwtUtil.validateToken(token)) {
            response.setContentType("application/json;charset=utf-8");
            try{
                response.getWriter().write("{\"msg\":\"未授权\"}");
                return false;
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }
        }
        
        Claims claims = jwtUtil.parseToken(token);
        request.setAttribute("userId", claims.get("userId"));
        return true;
    }
}
