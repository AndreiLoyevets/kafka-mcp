package com.aloievets.ai.mcp.kafka.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.aloievets.ai.mcp.kafka.client.service.KafkaStatusViewer;
import com.aloievets.ai.mcp.kafka.client.model.KafkaNodeDto;
import com.aloievets.ai.mcp.kafka.client.model.KafkaTopicDescriptionDto;
import com.aloievets.ai.mcp.kafka.model.KafkaNodesDto;
import com.aloievets.ai.mcp.kafka.model.KafkaTopicDescriptionsDto;
import com.aloievets.ai.mcp.kafka.model.KafkaTopicNamesDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class KafkaMcpServer {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaMcpServer.class);
    private final KafkaStatusViewer kafkaStatusViewer;

    public KafkaMcpServer(final KafkaStatusViewer kafkaStatusViewer) {
        this.kafkaStatusViewer = kafkaStatusViewer;
    }

    @McpTool(title = "Describe Kafka cluster controller", description = "Get Kafka cluster controller node",
            generateOutputSchema = true)
    public KafkaNodeDto describeClusterController() {
        LOG.debug("Requested to describe cluster controller");
        return kafkaStatusViewer.describeClusterController();
    }

    @McpTool(title = "Describe Kafka cluster nodes", description = "Get all Kafka cluster nodes details",
            generateOutputSchema = true)
    public KafkaNodesDto describeClusterNodes() {
        LOG.debug("Requested to describe cluster nodes");
        final List<KafkaNodeDto> nodes = kafkaStatusViewer.describeClusterNodes();
        return new KafkaNodesDto(nodes);
    }

    @McpTool(title = "List topics", description = "List Kafka topics in my cluster",
            generateOutputSchema = true)
    public KafkaTopicNamesDto listTopics() {
        LOG.debug("Requested to list topics");
        final Set<String> topicNames = kafkaStatusViewer.listTopics();
        return new KafkaTopicNamesDto(topicNames);
    }

    @McpTool(title = "Describe topics", description = "Get Kafka topic details for the provided collection of topic names",
            generateOutputSchema = true)
    public KafkaTopicDescriptionsDto describeTopics(
            @McpToolParam(description = "Collection of Kafka topic names to describe") final Collection<String> topicNames) {
        LOG.debug("Requested to describe topics: {}", topicNames);
        final List<KafkaTopicDescriptionDto> topicDescriptions = kafkaStatusViewer.describeTopics(topicNames);
        return new KafkaTopicDescriptionsDto(topicDescriptions);
    }
}
