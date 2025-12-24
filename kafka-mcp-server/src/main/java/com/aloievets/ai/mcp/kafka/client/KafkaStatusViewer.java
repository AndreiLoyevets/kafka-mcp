package com.aloievets.ai.mcp.kafka.client;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.KafkaFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaStatusViewer {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaStatusViewer.class);
    private static final String GENERIC_ERROR_TEMPLATE = "Failed to %s";
    private final AdminClient kafkaAdminClient;
    private final long timeoutMs;

    @Autowired
    public KafkaStatusViewer(final AdminClient kafkaAdminClient,
            @Value("${kafka.client.timeout-ms}") final long timeoutMs) {
        this.kafkaAdminClient = kafkaAdminClient;
        this.timeoutMs = timeoutMs;
    }

    public Set<String> listTopics() {
        LOG.debug("Requested to list topics");
        final Set<String> topics = executeSyncAndSafely(() -> kafkaAdminClient.listTopics()
                .names(), "list topics", timeoutMs);
        LOG.debug("Found {} topics: {}", topics.size(), topics);
        return topics;
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
