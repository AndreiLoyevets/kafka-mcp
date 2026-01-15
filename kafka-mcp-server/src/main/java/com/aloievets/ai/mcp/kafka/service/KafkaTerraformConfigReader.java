package com.aloievets.ai.mcp.kafka.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class KafkaTerraformConfigReader {
    private static final String TERRAFORM_FILE_PATH_TEMPLATE = "mcp/kafka-terraform-%s.yaml";
    private final String kafkaClusterName;
    private final ResourceLoader resourceLoader;

    public KafkaTerraformConfigReader(
            @Value("${kafka-mcp.kafka.cluster.name}") final String kafkaClusterName,
            final ResourceLoader resourceLoader) {
        this.kafkaClusterName = kafkaClusterName;
        this.resourceLoader = resourceLoader;
    }

    public String getKafkaTerraformConfig() throws IOException {
        final String filename = String.format(TERRAFORM_FILE_PATH_TEMPLATE, kafkaClusterName);
        final Resource resource = resourceLoader.getResource("classpath:" + filename);
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}
