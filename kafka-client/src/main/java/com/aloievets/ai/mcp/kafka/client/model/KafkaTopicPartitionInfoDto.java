package com.aloievets.ai.mcp.kafka.client.model;

import static com.aloievets.ai.mcp.kafka.client.model.KafkaNodeDto.fromKafkaNode;

import java.util.Collections;
import java.util.List;

import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;

public record KafkaTopicPartitionInfoDto(int partition, KafkaNodeDto partitionLeader,
                                         List<KafkaNodeDto> partitionReplicas,
                                         List<KafkaNodeDto> inSyncReplicas, List<KafkaNodeDto> eligibleLeaderReplicas,
                                         List<KafkaNodeDto> lastKnownEligibleLeaderReplicas) {

    public static KafkaTopicPartitionInfoDto fromTopicPartitionInfo(final TopicPartitionInfo info) {
        return new KafkaTopicPartitionInfoDto(info.partition(),
                fromKafkaNode(info.leader()),
                toKafkaNodeDtoList(info.replicas()),
                toKafkaNodeDtoList(info.isr()),
                toKafkaNodeDtoList(info.elr()),
                toKafkaNodeDtoList(info.lastKnownElr()));
    }

    private static List<KafkaNodeDto> toKafkaNodeDtoList(final List<Node> nodes) {
        if (nodes == null) {
            return Collections.emptyList();
        }

        return nodes.stream()
                .map(KafkaNodeDto::fromKafkaNode)
                .toList();
    }
}
