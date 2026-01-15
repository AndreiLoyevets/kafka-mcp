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
    public CorsFilter corsFilter(@Value("${kafka-mcp.local.cors.allowed-origin}") final String allowedOrigin,
            @Value("${kafka-mcp.local.cors.allowed-methods}") final String[] allowedMethods,
            @Value("${kafka-mcp.local.cors.allowed-headers}") final String allowedHeaders,
            @Value("${kafka-mcp.local.cors.allow-credentials}") final boolean allowCredentials) {
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
