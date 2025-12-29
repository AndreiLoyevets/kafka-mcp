package com.aloievets.ai.mcp.kafka.mcp.dto;

import java.util.List;

import com.aloievets.ai.mcp.kafka.client.dto.KafkaTopicDescriptionDto;

public record KafkaTopicDescriptionsDto(List<KafkaTopicDescriptionDto> topicDescriptions) {
}
