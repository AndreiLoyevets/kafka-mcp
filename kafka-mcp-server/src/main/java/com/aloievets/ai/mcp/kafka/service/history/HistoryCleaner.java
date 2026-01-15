package com.aloievets.ai.mcp.kafka.service.history;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class HistoryCleaner {
    private static final Logger LOG = LoggerFactory.getLogger(HistoryCleaner.class);
    private final McpHistoryRepository repository;
    private final int keepHistoryDays;
    private final boolean cleanupEnabled;

    public HistoryCleaner(final McpHistoryRepository repository,
            @Value("${kafka-mcp.history.days}") final int keepHistoryDays,
            @Value("${kafka-mcp.history.cleanup-enabled}") final boolean cleanupEnabled) {
        this.repository = repository;
        this.keepHistoryDays = keepHistoryDays;
        this.cleanupEnabled = cleanupEnabled;
    }

    @PostConstruct
    public void cleanupOldHistory() {
        if (!cleanupEnabled) {
            LOG.info("MCP history cleanup is disabled");
            return;
        }

        final Instant threshold = Instant.now().minus(keepHistoryDays, ChronoUnit.DAYS);
        LOG.info("Deleting all MCP history entries older than {}", threshold);
        repository.deleteAllByTimestampBefore(threshold);
    }
}
