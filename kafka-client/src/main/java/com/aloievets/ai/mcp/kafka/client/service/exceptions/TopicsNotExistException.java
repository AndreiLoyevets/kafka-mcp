package com.aloievets.ai.mcp.kafka.client.service.exceptions;

import java.util.Set;

public class TopicsNotExistException extends RuntimeException {

    public TopicsNotExistException(final Set<String> unauthorizedTopics) {
        super(String.format(
                "Failed to connect to the following topics: %s. Make sure the topics exist and credentials are correct",
                unauthorizedTopics));
    }
}
