package com.aloievets.ai.mcp.kafka.client.service.exceptions;

public class KafkaClientTimeoutException extends RuntimeException {

    public KafkaClientTimeoutException(final String commandDescription, final long timeoutMs) {
        super(String.format("Timeout %s milliseconds elapsed while waiting for %s", timeoutMs, commandDescription));
    }
}
