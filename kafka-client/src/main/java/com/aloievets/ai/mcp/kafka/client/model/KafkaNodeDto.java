package com.aloievets.ai.mcp.kafka.client.model;

import org.apache.kafka.common.Node;

public record KafkaNodeDto(int id, String idString, String host, int port, String rack) {
    private static final KafkaNodeDto EMPTY_NODE = new KafkaNodeDto(-1, "UNKNOWN", "UNKNOWN", -1, "UNKNOWN");

    public static KafkaNodeDto fromKafkaNode(final Node kafkaNode) {
        if (kafkaNode == null) {
            return EMPTY_NODE;
        }

        final String idString = kafkaNode.idString() == null ? "UNKNOWN" : kafkaNode.idString();
        final String host = kafkaNode.host() == null ? "UNKNOWN" : kafkaNode.host();
        final String rack = kafkaNode.rack() == null ? "UNKNOWN" : kafkaNode.rack();

        return new KafkaNodeDto(kafkaNode.id(), idString, host, kafkaNode.port(), rack);
    }
}
