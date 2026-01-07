package com.aloievets.ai.mcp.kafka.client.service.exceptions;

public class KafkaClientException extends RuntimeException {

    public KafkaClientException(final String message) {
        super(message);
    }
}
