package com.ipss.et.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
           .authorizeHttpRequests(auth -> auth
               .requestMatchers(
                   "/", "/index.html", "/laminas.html",
                   "/js/**", "/css/**", "/img/**",
                   "/swagger-ui/**", "/v3/api-docs/**",
                   "/api/v1/**"
               ).permitAll()
               .anyRequest().permitAll()
           );
        return http.build();
    }
}
