package com.aloievets.ai.mcp.kafka.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public AdminClient adminClient(final KafkaProperties kafkaProperties) {
        return AdminClient.create(kafkaProperties.buildAdminProperties());
    }
}
