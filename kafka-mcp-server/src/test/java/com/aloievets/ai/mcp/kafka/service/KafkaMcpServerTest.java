package com.aloievets.ai.mcp.kafka.service;

import static com.aloievets.ai.mcp.kafka.service.McpTestUtils.assertTextMcpResult;
import static org.mockito.Mockito.when;

import java.util.Set;

import com.aloievets.ai.mcp.kafka.client.service.KafkaStatusViewer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"})
@ActiveProfiles("mcp-test")
public class KafkaMcpServerTest {
    @LocalServerPort
    private int port;
    @MockitoBean
    private KafkaStatusViewer kafkaStatusViewer;
    private McpSyncClient mcpClient;

    @BeforeEach
    void setUp() {
        final var transport = HttpClientStreamableHttpTransport.builder("http://localhost:" + port)
                .build();
        mcpClient = McpClient.sync(transport)
                .build();
        mcpClient.initialize();
    }

    @AfterEach
    void tearDown() {
        mcpClient.close();
    }

    @Test
    void simulateClaudeToolCall() {
        when(kafkaStatusViewer.listTopics()).thenReturn(Set.of("topic1"));
        final String expectedText = "{\"topicNames\":[\"topic1\"]}";

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("listTopics")
                .build());

        assertTextMcpResult(expectedText, result);
    }
}
