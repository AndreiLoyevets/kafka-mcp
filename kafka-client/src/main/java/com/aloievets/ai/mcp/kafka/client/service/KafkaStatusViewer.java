package com.aloievets.ai.mcp.kafka.client.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import com.aloievets.ai.mcp.kafka.client.model.KafkaNodeDto;
import com.aloievets.ai.mcp.kafka.client.model.KafkaTopicDescriptionDto;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaStatusViewer {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaStatusViewer.class);
    private static final String GENERIC_ERROR_TEMPLATE = "Failed to %s";
    private final AdminClient kafkaAdminClient;
    private final long timeoutMs;

    public KafkaStatusViewer(final AdminClient kafkaAdminClient, final long timeoutMs) {
        this.kafkaAdminClient = kafkaAdminClient;
        this.timeoutMs = timeoutMs;
    }

    public KafkaNodeDto describeClusterController() {
        LOG.debug("Requested to describe cluster controller");
        final Node node = executeSyncAndSafely(() -> kafkaAdminClient.describeCluster()
                .controller(), "describe cluster controller", timeoutMs);
        LOG.debug("Found cluster controller: {}", node);
        return KafkaNodeDto.fromKafkaNode(node);
    }

    public List<KafkaNodeDto> describeClusterNodes() {
        LOG.debug("Requested to describe cluster nodes");
        final Collection<Node> nodes = executeSyncAndSafely(() -> kafkaAdminClient.describeCluster()
                .nodes(), "describe cluster nodes", timeoutMs);
        LOG.debug("Found {} cluster nodes: {}", nodes.size(), nodes);
        return nodes.stream()
                .map(KafkaNodeDto::fromKafkaNode)
                .toList();
    }

    public Set<String> listTopics() {
        LOG.debug("Requested to list topics");
        final Set<String> topics = executeSyncAndSafely(() -> kafkaAdminClient.listTopics()
                .names(), "list topics", timeoutMs);
        LOG.debug("Found {} topics: {}", topics.size(), topics);
        return topics;
    }

    public List<KafkaTopicDescriptionDto> describeTopics(final Collection<String> topicNames) {
        LOG.debug("Requested to describe topics: {}", topicNames);
        final Map<String, TopicDescription> topicDescriptions = executeSyncAndSafely(
                () -> kafkaAdminClient.describeTopics(topicNames)
                        .allTopicNames(), "describe topics", timeoutMs);
        LOG.debug("Found {} topic descriptions", topicDescriptions.size());
        return topicDescriptions.values()
                .stream()
                .map(KafkaTopicDescriptionDto::fromKafkaTopicDescription)
                .toList();
    }

    private <T> T executeSyncAndSafely(final Supplier<KafkaFuture<T>> supplier,
            final String commandDescription, final long timeoutMs) {
        try {
            return supplier.get()
                    .get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread()
                    .interrupt();
            throw new RuntimeException(String.format(GENERIC_ERROR_TEMPLATE, commandDescription), e);
        } catch (final ExecutionException e) {
            throw new RuntimeException(String.format(GENERIC_ERROR_TEMPLATE, commandDescription), e);
        } catch (final TimeoutException e) {
            throw new KafkaClientTimeout(commandDescription, timeoutMs, e);
        }
    }
}
