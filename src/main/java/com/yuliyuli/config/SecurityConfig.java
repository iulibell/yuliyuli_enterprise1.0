package com.yuliyuli.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .
        // 禁用Session（JWT不需要Session，避免会话固定攻击）
        sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(CorsUtils::isPreFlightRequest)
                    .permitAll()
                    .requestMatchers("/user/login", "/user/register")
                    .permitAll()
                    .requestMatchers("/video/list", "/video/detail", "/comment/list")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                        (request, response, authException) -> {
                          // 未登录/Token失效时，返回统一的401结果
                          response.setContentType("application/json;charset=UTF-8");
                          response
                              .getWriter()
                              .write("{\"code\":401,\"msg\":\"未登录或Token已过期\",\"data\":null}");
                        })
                    .accessDeniedHandler(
                        (request, response, accessDeniedException) -> {
                          // 无权限时，返回统一的403结果
                          response.setContentType("application/json;charset=UTF-8");
                          response
                              .getWriter()
                              .write("{\"code\":403,\"msg\":\"无操作权限\",\"data\":null}");
                        }))
        // ========== 退出登录 ==========
        .logout(
            logout ->
                logout
                    .logoutUrl("/user/logout") // 退出登录接口
                    .logoutSuccessHandler(
                        (request, response, authentication) -> {
                          // 退出成功返回JSON
                          response.setContentType("application/json;charset=UTF-8");
                          response
                              .getWriter()
                              .write("{\"code\":200,\"msg\":\"退出登录成功\",\"data\":null}");
                        }));
    return httpSecurity.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    // 允许的源（开发环境可设为*，生产环境需指定具体域名）
    config.addAllowedOriginPattern("*");
    // 允许的请求头
    config.addAllowedHeader("*");
    // 允许的请求方法
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    // 允许携带Cookie（JWT不需要，但若有其他场景可开启）
    config.setAllowCredentials(true);
    // 跨域缓存时间（减少预检请求）
    config.setMaxAge(3600L);

    // 应用到所有路径
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
