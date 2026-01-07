package com.aloievets.ai.mcp.kafka.service.exceptions;

public class McpTimeoutException extends RuntimeException {

    public McpTimeoutException(final String message) {
        super(message);
    }

    public McpTimeoutException(final String commandDescription, final long timeoutMs) {
        super(String.format("Timeout %s milliseconds elapsed while waiting for %s", timeoutMs, commandDescription));
    }
}
