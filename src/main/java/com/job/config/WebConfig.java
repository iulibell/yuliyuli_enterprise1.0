package com.job.config;

import com.job.util.JwtIntercepter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final JwtIntercepter jwtIntercepter;

    public WebConfig(JwtIntercepter jwtIntercepter) {
        this.jwtIntercepter = jwtIntercepter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtIntercepter)
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
