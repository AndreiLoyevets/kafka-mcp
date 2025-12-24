package com.aloievets.ai.mcp.kafka.mcp;

import com.aloievets.ai.mcp.kafka.client.KafkaStatusViewer;

import org.springframework.stereotype.Component;

@Component
public class KafkaViewerMcpServer {
    private final KafkaStatusViewer kafkaStatusViewer;

    public KafkaViewerMcpServer(final KafkaStatusViewer kafkaStatusViewer) {
        this.kafkaStatusViewer = kafkaStatusViewer;
    }
}
