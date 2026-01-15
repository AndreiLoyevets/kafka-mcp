package com.aloievets.ai.mcp.kafka.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

public class McpTestUtils {

    private McpTestUtils() {
    }

    public static void assertTextMcpToolResult(final String expectedText, final CallToolResult result) {
        assertFalse(result.isError());
        final List<Content> contents = result.content();
        assertEquals(1, contents.size());
        final Content first = contents.getFirst();
        assertInstanceOf(TextContent.class, first);
        final TextContent textContent = (TextContent) first;
        final String text = textContent.text();
        assertEquals(expectedText, text);
    }

    public static void assertTextMcpResourceResult(final String expectedUri, final String expectedText,
            final ReadResourceResult result) {
        final List<ResourceContents> contents = result.contents();
        assertEquals(1, contents.size());
        final ResourceContents first = contents.getFirst();
        assertEquals(expectedUri, first.uri());
        assertInstanceOf(TextResourceContents.class, first);
        final TextResourceContents textResourceContents = (TextResourceContents) first;
        assertEquals(expectedText, textResourceContents.text());
    }
}
