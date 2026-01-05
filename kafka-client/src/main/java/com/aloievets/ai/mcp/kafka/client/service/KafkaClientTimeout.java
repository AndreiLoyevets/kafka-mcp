package com.aloievets.ai.mcp.kafka.client.service;

public class KafkaClientTimeout extends RuntimeException {

    public KafkaClientTimeout(final String commandDescription, final long timeoutMs, final Throwable cause) {
        super(String.format("Timeout %s milliseconds elapsed while waiting for %s", timeoutMs, commandDescription), cause);
    }
}
