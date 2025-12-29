package com.aloievets.ai.mcp.kafka.mcp;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.aloievets.ai.mcp.kafka.client.KafkaStatusViewer;
import com.aloievets.ai.mcp.kafka.client.dto.KafkaNodeDto;
import com.aloievets.ai.mcp.kafka.client.dto.KafkaTopicDescriptionDto;

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

    @McpTool(title = "Describe Kafka cluster controller", description = "Get Kafka cluster controller node")
    public KafkaNodeDto describeClusterController() {
        LOG.debug("Requested to describe cluster controller");
        return kafkaStatusViewer.describeClusterController();
    }

    @McpTool(title = "Describe Kafka cluster nodes", description = "Get all Kafka cluster nodes details")
    public List<KafkaNodeDto> describeClusterNodes() {
        LOG.debug("Requested to describe cluster nodes");
        return kafkaStatusViewer.describeClusterNodes();
    }

    @McpTool(title = "List topics", description = "List Kafka topics in my cluster")
    public Set<String> listTopics() {
        LOG.debug("Requested to list topics");
        return kafkaStatusViewer.listTopics();
    }

    @McpTool(title = "Describe topics", description = "Get Kafka topic details for the provided collection of topic names")
    public List<KafkaTopicDescriptionDto> describeTopics(
            @McpToolParam(description = "Collection of Kafka topic names to describe") final Collection<String> topicNames) {
        LOG.debug("Requested to describe topics: {}", topicNames);
        return kafkaStatusViewer.describeTopics(topicNames);
    }
}
