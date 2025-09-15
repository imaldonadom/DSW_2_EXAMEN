package com.ipss.et.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/**").permitAll()
      )
      .httpBasic(b -> b.disable())      // <— desactiva el challenge Basic
      .formLogin(f -> f.disable());     // <— por si acaso
    return http.build();
  }
}
