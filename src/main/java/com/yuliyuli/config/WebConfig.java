package com.yuliyuli.config;

import com.yuliyuli.filter.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private static final String[] EXCLUDE_PATH_PATTERNS = {
    // 1. 用户模块公开接口（登录/注册）
    "/api/user/login",
    "/api/user/register",
    "/api/user/logout",
    "/api/user/getCode",
    // 2. 视频/评论模块公开接口
    "/api/video/list",
    "/api/video/detail",
    "/api/comment/list",
    // 3. Knife4j接口文档路径（开发环境必备）
    "/doc.html",
    "/webjars/**",
    "/swagger-ui/**",
    "/v3/api-docs/**",
    // 4. 其他静态资源（可选，若有前端静态文件需放行）
    "/static/**",
    "/favicon.ico"
  };

  private final LoginInterceptor loginInterceptor;

  public WebConfig(LoginInterceptor loginInterceptor) {
    this.loginInterceptor = loginInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(loginInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(EXCLUDE_PATH_PATTERNS);
  }
}
