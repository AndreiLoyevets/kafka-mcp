package com.aloievets.ai.mcp.kafka.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@Profile("local")
public class LocalMcpInspectorConfig {

    @Bean
    public CorsFilter corsFilter(@Value("${mcp.cors.allowed-origin}") final String allowedOrigin,
            @Value("${mcp.cors.allowed-methods}") final String[] allowedMethods,
            @Value("${mcp.cors.allowed-headers}") final String allowedHeaders,
            @Value("${mcp.cors.allow-credentials}") final boolean allowCredentials) {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin(allowedOrigin);
        Arrays.stream(allowedMethods).forEach(config::addAllowedMethod);
        config.addAllowedHeader(allowedHeaders);
        config.setAllowCredentials(allowCredentials);
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
