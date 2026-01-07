package com.aloievets.ai.mcp.kafka.service;

import java.util.Collection;
import java.util.List;

import com.aloievets.ai.mcp.kafka.client.model.KafkaNodeDto;
import com.aloievets.ai.mcp.kafka.client.model.KafkaTopicDescriptionDto;
import com.aloievets.ai.mcp.kafka.client.service.KafkaStatusViewer;
import com.aloievets.ai.mcp.kafka.model.KafkaNodesDto;
import com.aloievets.ai.mcp.kafka.model.KafkaTopicDescriptionsDto;
import com.aloievets.ai.mcp.kafka.model.KafkaTopicNamesDto;
import com.aloievets.ai.mcp.kafka.service.exceptions.GenericMcpException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class KafkaMcpServer {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaMcpServer.class);
    private final KafkaStatusViewer kafkaStatusViewer;
    private final KafkaTerraformConfigReader kafkaTerraformConfigReader;

    public KafkaMcpServer(final KafkaStatusViewer kafkaStatusViewer,
            final KafkaTerraformConfigReader kafkaTerraformConfigReader) {
        this.kafkaStatusViewer = kafkaStatusViewer;
        this.kafkaTerraformConfigReader = kafkaTerraformConfigReader;
    }

    @McpTool(title = "Describe Kafka cluster controller", description = "Get Kafka cluster controller node",
            generateOutputSchema = true)
    @Cacheable(value = "mcp-responses", key = "'describeClusterController'")
    public KafkaNodeDto describeClusterController() {
        LOG.debug("Requested to describe cluster controller");
        return kafkaStatusViewer.describeClusterController();
    }

    @McpTool(title = "Describe Kafka cluster nodes", description = "Get all Kafka cluster nodes details",
            generateOutputSchema = true)
    @Cacheable(value = "mcp-responses", key = "'describeClusterNodes'")
    public KafkaNodesDto describeClusterNodes() {
        LOG.debug("Requested to describe cluster nodes");
        final List<KafkaNodeDto> nodes = kafkaStatusViewer.describeClusterNodes();
        return new KafkaNodesDto(nodes);
    }

    @McpTool(title = "List topics", description = "List Kafka topics in my cluster",
            generateOutputSchema = true)
    @Cacheable(value = "mcp-responses", key = "'listTopics'")
    public KafkaTopicNamesDto listTopics() {
        LOG.debug("Requested to list topics");
        return new KafkaTopicNamesDto(kafkaStatusViewer.listTopics());
    }

    @McpTool(title = "Describe topics", description = "Get Kafka topic details for the provided collection of topic names",
            generateOutputSchema = true)
    @Cacheable(value = "mcp-responses", key = "'describeTopics-' + (#topicNames?.hashCode() ?: 'null')")
    public KafkaTopicDescriptionsDto describeTopics(
            @McpToolParam(description = "Collection of Kafka topic names to describe") final Collection<String> topicNames) {
        LOG.debug("Requested to describe topics: {}", topicNames);
        final List<KafkaTopicDescriptionDto> topicDescriptions = kafkaStatusViewer.describeTopics(topicNames);
        return new KafkaTopicDescriptionsDto(topicDescriptions);
    }

    @McpResource(uri = "file://mcp/kafka-terraform.yaml", mimeType = "text/yaml", title = "Kafka cluster terraform configuration", description = "Kafka cluster terraform configuration. Use it to compare the current cluster state vs the terraform configuration.")
    @Cacheable(value = "mcp-responses", key = "'getKafkaTerraformConfig'")
    public String getKafkaTerraformConfig() {
        try {
            return kafkaTerraformConfigReader.getKafkaTerraformConfig();
        } catch (final Exception e) {
            final String message = "Failed to load Kafka terraform config, the resource is unavailable. Contact the development team if the problem remains";
            LOG.error(message, e);
            throw new GenericMcpException(message);
        }
    }
}
