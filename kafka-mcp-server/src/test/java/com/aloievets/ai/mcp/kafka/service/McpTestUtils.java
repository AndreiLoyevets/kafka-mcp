package com.aloievets.ai.mcp.kafka.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

public class McpTestUtils {

    private McpTestUtils() {
    }

    public static void assertTextMcpResult(final String expectedText, final CallToolResult result) {
        assertFalse(result.isError());
        final List<Content> contents = result.content();
        assertEquals(1, contents.size());
        final Content first = contents.getFirst();
        assertInstanceOf(TextContent.class, first);
        final TextContent textContent = (TextContent) first;
        final String text = textContent.text();
        assertEquals(expectedText, text);
    }
}
