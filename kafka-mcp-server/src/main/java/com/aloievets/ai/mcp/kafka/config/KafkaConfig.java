package com.aloievets.ai.mcp.kafka.config;

import com.aloievets.ai.mcp.kafka.client.service.KafkaStatusViewer;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@Profile("!mcp-test")
public class KafkaConfig {

    @Bean
    public AdminClient adminClient(final KafkaAdmin kafkaAdmin) {
        return AdminClient.create(kafkaAdmin.getConfigurationProperties());
    }

    @Bean
    public KafkaStatusViewer kafkaStatusViewer(final AdminClient kafkaAdminClient,
            @Value("${kafka-mcp.kafka.client.timeout-ms}") final long timeoutMs) {
        return new KafkaStatusViewer(kafkaAdminClient, timeoutMs);
    }
}
