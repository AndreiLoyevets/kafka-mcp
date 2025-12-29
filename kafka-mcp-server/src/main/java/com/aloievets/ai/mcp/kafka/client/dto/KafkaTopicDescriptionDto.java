package com.aloievets.ai.mcp.kafka.client.dto;

import java.util.Collections;
import java.util.List;

import org.apache.kafka.clients.admin.TopicDescription;

public record KafkaTopicDescriptionDto(String name, boolean internal, List<KafkaTopicPartitionInfoDto> partitions,
                                       String topicId) {

    public static KafkaTopicDescriptionDto fromKafkaTopicDescription(final TopicDescription desc) {
        final List<KafkaTopicPartitionInfoDto> topicPartitions;
        if (desc.partitions() == null) {
            topicPartitions = Collections.emptyList();
        } else {
            topicPartitions = desc.partitions()
                    .stream()
                    .map(KafkaTopicPartitionInfoDto::fromTopicPartitionInfo)
                    .toList();
        }

        return new KafkaTopicDescriptionDto(desc.name(), desc.isInternal(), topicPartitions, desc.topicId().toString());
    }
}
