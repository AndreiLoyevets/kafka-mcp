package com.aloievets.ai.mcp.kafka.mcp.dto;

import java.util.List;

import com.aloievets.ai.mcp.kafka.client.dto.KafkaNodeDto;

public record KafkaNodesDto(List<KafkaNodeDto> nodes) {
}
