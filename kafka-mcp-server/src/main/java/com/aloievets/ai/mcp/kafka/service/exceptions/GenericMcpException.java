package com.aloievets.ai.mcp.kafka.service.exceptions;

public class GenericMcpException extends RuntimeException {

    public GenericMcpException() {
        super("Check your configuration, request parameters, retry later or contact the Kafka MCP server development team");
    }

    public GenericMcpException(final String message) {
        super(message);
    }
}
