package com.example.sampleapp;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MySecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
            .oauth2Login(withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/protected").authenticated()
                .anyRequest().permitAll());
        return http.build();
    }
    
}
