package com.yuliyuli.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.yuliyuli.filter.LoginInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String[] EXCLUDE_PATH_PATTERNS = {
            // 1. 用户模块公开接口（登录/注册）
            "/user/login", "/user/register",
            // 2. 视频/评论模块公开接口
            "/video/list", "/video/detail", "/comment/list",
            // 3. Knife4j接口文档路径（开发环境必备）
            "/doc.html", "/webjars/**", "/swagger-ui/**", "/v3/api-docs/**",
            // 4. 其他静态资源（可选，若有前端静态文件需放行）
            "/static/**", "/favicon.ico"
    };

    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    EXCLUDE_PATH_PATTERNS
                );
    }
}
