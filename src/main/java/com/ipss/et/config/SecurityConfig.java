package com.ipss.et.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index.html", "/laminas.html",
                    "/js/**", "/css/**", "/img/**",
                    "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml"
                ).permitAll()
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
