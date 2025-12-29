package com.aloievets.ai.mcp.kafka.client.dto;

import org.apache.kafka.common.Node;

public record KafkaNodeDto(int id, String idString, String host, int port, String rack) {

    public static KafkaNodeDto fromKafkaNode(final Node kafkaNode) {
        return new KafkaNodeDto(kafkaNode.id(), kafkaNode.idString(), kafkaNode.host(), kafkaNode.port(), kafkaNode.rack());
    }
}
