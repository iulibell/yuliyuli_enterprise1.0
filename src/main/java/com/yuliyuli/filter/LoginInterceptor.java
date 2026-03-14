package com.yuliyuli.filter;

import com.alibaba.fastjson2.JSON;
import com.yuliyuli.common.Result;
import com.yuliyuli.entity.User;
import com.yuliyuli.entity.UserHolder;
import com.yuliyuli.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

  private static final String REDIS_TOKEN_KEY_PREFIX = "login:token:";

  private final JwtUtil jwtUtil;

  private final RedisTemplate<String, Object> redisTemplate;

  public LoginInterceptor(JwtUtil jwtUtil, RedisTemplate<String, Object> redisTemplate) {
    this.jwtUtil = jwtUtil;
    this.redisTemplate = redisTemplate;
  }

  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    try {
      String authorization = request.getHeader("Authorization");
      String token = null;
      if (authorization != null && authorization.startsWith("Bearer ")) {
        token = authorization.substring(7);
      }
      log.info("【登录拦截器】校验请求Token,请求路径:{},Token:{}", request.getRequestURI(), token);
      if (token == null || !jwtUtil.validateToken(token)) {
        return handleError(response, Result.fail(401, "Token校验失败,请重新登录"));
      }
      String key = REDIS_TOKEN_KEY_PREFIX + token;
      User user = (User) redisTemplate.opsForValue().get(key);
      if (user == null) {
        log.warn("【登录拦截器】Token在Redis中不存在,可能已注销，请求路径:{}", request.getRequestURI());
        return handleError(response, Result.fail(401, "登录已失效,请重新登录"));
      }

      UserHolder.setUser(user);
      try {
        Claims claims = jwtUtil.parseToken(token);
        request.setAttribute("userId", claims.get("userId"));
        log.info("【登录拦截器】Token校验成功,用户ID:{}，请求路径:{}", claims.get("userId"), request.getRequestURI());
        return true;
      } catch (Exception e) {
        log.error("【登录拦截器】解析Token失败,请求路径:{}，异常信息:{}", request.getRequestURI(), e.getMessage(), e);
        return handleError(response, Result.fail(401, "Token解析失败,请重新登录"));
      }
    } catch (Exception e) {
      log.error("【登录拦截器】处理请求时发生异常,请求路径:{}，异常信息:{}", request.getRequestURI(), e.getMessage(), e);
      return handleError(response, Result.fail(500, "服务器内部错误"));
    }
  }

  private boolean handleError(HttpServletResponse response, Result<?> result) throws Exception {
    // 设置响应头，防止乱码
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401未授权
    // 写入JSON结果
    try (PrintWriter writer = response.getWriter()) {
      writer.write(JSON.toJSONString(result));
      writer.flush();
    }
    return false;
  }

  /**
   * 请求处理完成后，清除用户信息
   * @param request 请求
   * @param response 响应
   * @param handler 处理程序
   * @param ex 异常
   */
  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
        Object handler, Exception e) {
    // 请求处理完成后，清除用户信息
    UserHolder.removeUser();
  }
}
