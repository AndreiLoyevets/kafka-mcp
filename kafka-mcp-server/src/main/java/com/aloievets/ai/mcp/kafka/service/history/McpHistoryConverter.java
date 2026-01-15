package com.aloievets.ai.mcp.kafka.service.history;

import java.time.Instant;

import com.aloievets.ai.mcp.kafka.exceptions.GenericMcpException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class McpHistoryConverter {
	private static final Logger LOG = LoggerFactory.getLogger(McpHistoryConverter.class);
	private final ObjectMapper objectMapper;
	private final String kafkaClusterName;

	public McpHistoryConverter(final ObjectMapper objectMapper, @Value("${kafka-mcp.kafka.cluster.name}") final String kafkaClusterName) {
		this.objectMapper = objectMapper;
        this.kafkaClusterName = kafkaClusterName;
    }

	public <T> McpHistory toMcpHistory(final String toolName, final T responseDto) {
		LOG.debug("Converting MCP response DTO to MCP history");

		try {
			final String jsonResponse = objectMapper.writeValueAsString(responseDto);
			final McpHistory history = new McpHistory();
			history.setKafkaClusterName(kafkaClusterName);
			history.setToolName(toolName);
			history.setJsonResponse(jsonResponse);
			history.setTimestamp(Instant.now());
			return history;
		}
		catch (final JsonProcessingException e) {
			LOG.error(String.format("Failed to serialize MCP response DTO for tool '%s'", toolName), e);
			throw new GenericMcpException("Failed to convert MCP response to JSON and save the results to the history. Contact the development team");
		}
	}
}
