package com.job.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.job.filter.LoginIntercepter;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoginIntercepter loginIntercepter;

    public WebConfig(LoginIntercepter loginIntercepter) {
        this.loginIntercepter = loginIntercepter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginIntercepter)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/login", 
                    "/register",
                    "/video/list",
                    "/video/detail",
                    "/comment/list"
                );
    }
}
