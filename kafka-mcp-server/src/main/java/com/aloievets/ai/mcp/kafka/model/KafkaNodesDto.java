package com.aloievets.ai.mcp.kafka.model;

import java.util.List;

import com.aloievets.ai.mcp.kafka.client.model.KafkaNodeDto;

public record KafkaNodesDto(List<KafkaNodeDto> nodes) {
}
