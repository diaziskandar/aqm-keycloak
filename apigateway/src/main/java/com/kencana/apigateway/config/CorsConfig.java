package com.kencana.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Izinkan asal (origin) dari frontend Anda
        corsConfig.addAllowedOrigin("http://localhost:8080");
        corsConfig.addAllowedOrigin("http://localhost:5173"); // Untuk mode dev Vite di lokal jika diperlukan
        corsConfig.addAllowedOrigin("http://localhost:3000");         

        // Izinkan semua method HTTP yang digunakan
        corsConfig.addAllowedMethod("*");
        
        // Izinkan semua header (Diubah dari addAllowedHeaders menjadi addAllowedHeader)
        corsConfig.addAllowedHeader("*");
        
        // Izinkan kredensial (seperti cookies atau token auth)
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}