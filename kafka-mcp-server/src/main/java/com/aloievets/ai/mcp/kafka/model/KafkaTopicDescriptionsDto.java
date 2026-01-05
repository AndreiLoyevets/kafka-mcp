package com.aloievets.ai.mcp.kafka.model;

import java.util.List;

import com.aloievets.ai.mcp.kafka.client.model.KafkaTopicDescriptionDto;

public record KafkaTopicDescriptionsDto(List<KafkaTopicDescriptionDto> topicDescriptions) {
}
