package com.aloievets.ai.mcp.kafka.mcp;

import java.util.Set;

import com.aloievets.ai.mcp.kafka.client.KafkaStatusViewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class KafkaMcpServer {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaMcpServer.class);
    private final KafkaStatusViewer kafkaStatusViewer;

    public KafkaMcpServer(final KafkaStatusViewer kafkaStatusViewer) {
        this.kafkaStatusViewer = kafkaStatusViewer;
    }

    @McpTool(title = "List topics", description = "List Kafka topics in my cluster", generateOutputSchema = true)
    public Set<String> listTopics() {
        LOG.debug("Requested to list topics");
        return kafkaStatusViewer.listTopics();
    }
}
