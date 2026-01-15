package com.aloievets.ai.mcp.kafka.service.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface McpHistoryRepository extends JpaRepository<McpHistory, Long> {

    List<McpHistory> findByToolNameAndTimestampBetween(final String toolName, final Instant start, final Instant end);

    void deleteAllByTimestampBefore(final Instant timestamp);
}