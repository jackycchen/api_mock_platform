package com.apimock.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig {
    // Disable WebMvc CORS configuration to avoid conflicts with Spring Security CORS
}