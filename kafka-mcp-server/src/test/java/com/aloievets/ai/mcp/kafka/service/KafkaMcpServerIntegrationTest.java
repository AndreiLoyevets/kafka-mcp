package com.aloievets.ai.mcp.kafka.service;

import static com.aloievets.ai.mcp.kafka.service.McpTestUtils.assertTextMcpResourceResult;
import static com.aloievets.ai.mcp.kafka.service.McpTestUtils.assertTextMcpToolResult;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class KafkaMcpServerIntegrationTest {
    private static final int KAFKA_PORT = 9092;
    @Container
    @ServiceConnection
    private static KafkaContainer kafka = new KafkaContainer("apache/kafka:4.0.1");
    @LocalServerPort
    private int port;
    private McpSyncClient mcpClient;

    @TestConfiguration
    static class KafkaTestConfig {

        @Bean
        public NewTopic topic1() {
            return TopicBuilder.name("topic1")
                    .partitions(1)
                    .build();
        }

        @Bean
        public NewTopic topic2() {
            return TopicBuilder.name("topic2")
                    .partitions(1)
                    .build();
        }
    }

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
    void listTopics() {
        final String expectedText = "{\"topicNames\":[\"topic1\",\"topic2\"]}";

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("listTopics")
                .build());

        assertTextMcpToolResult(expectedText, result);
    }

    @Test
    void describeClusterController() {
        final String expectedText = String.format(
                "{\"id\":1,\"idString\":\"1\",\"host\":\"localhost\",\"port\":%d,\"rack\":\"UNKNOWN\"}",
                kafka.getMappedPort(KAFKA_PORT));

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("describeClusterController")
                .build());

        assertTextMcpToolResult(expectedText, result);
    }

    @Test
    void describeClusterNodes() {
        final String expectedText = String.format(
                "{\"nodes\":[{\"id\":1,\"idString\":\"1\",\"host\":\"localhost\",\"port\":%d,\"rack\":\"UNKNOWN\"}]}",
                kafka.getMappedPort(KAFKA_PORT));

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("describeClusterNodes")
                .build());

        assertTextMcpToolResult(expectedText, result);
    }

    @Test
    void describeTopics() throws ExecutionException, InterruptedException {
        final String topic1 = "topic1";
        final String topic2 = "topic2";
        final String topic1Id;
        final String topic2Id;

        try (final AdminClient admin = AdminClient.create(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()))) {
            topic1Id = getTopicDescription(admin, topic1).topicId().toString();
            topic2Id = getTopicDescription(admin, topic2).topicId().toString();
        }

        final int kafkaPort = kafka.getMappedPort(KAFKA_PORT);
        final String expectedText = """
                {"topicDescriptions":[{"name":"topic1","internal":false,"partitions":[{"partition":0,"partitionLeader":\
                {"id":1,"idString":"1","host":"localhost","port":$port,"rack":"UNKNOWN"},"partitionReplicas":[{"id":1,\
                "idString":"1","host":"localhost","port":$port,"rack":"UNKNOWN"}],"inSyncReplicas":[{"id":1,\
                "idString":"1","host":"localhost","port":$port,"rack":"UNKNOWN"}],"eligibleLeaderReplicas":[],\
                "lastKnownEligibleLeaderReplicas":[]}],"topicId":"$topic1Id"},{"name":"topic2","internal":false,\
                "partitions":[{"partition":0,"partitionLeader":{"id":1,"idString":"1","host":"localhost","port":$port,\
                "rack":"UNKNOWN"},"partitionReplicas":[{"id":1,"idString":"1","host":"localhost","port":$port,\
                "rack":"UNKNOWN"}],"inSyncReplicas":[{"id":1,"idString":"1","host":"localhost","port":$port,\
                "rack":"UNKNOWN"}],"eligibleLeaderReplicas":[],"lastKnownEligibleLeaderReplicas":[]}],"topicId":"$topic2Id"}]}"""
                .replaceAll("\\$port", String.valueOf(kafkaPort))
                .replaceAll("\\$topic1Id", topic1Id)
                .replaceAll("\\$topic2Id", topic2Id);
//        final String expectedText = "{\"topicDescriptions\":[{\"name\":\"topic1\",\"internal\":false,\"partitions\":[{\"partition\":0,\"partitionLeader\":{\"id\":1,\"idString\":\"1\",\"host\":\"localhost\",\"port\":$port,\"rack\":\"UNKNOWN\"},\"partitionReplicas\":[{\"id\":1,\"idString\":\"1\",\"host\":\"localhost\",\"port\":$port,\"rack\":\"UNKNOWN\"}],\"inSyncReplicas\":[{\"id\":1,\"idString\":\"1\",\"host\":\"localhost\",\"port\":$port,\"rack\":\"UNKNOWN\"}],\"eligibleLeaderReplicas\":[],\"lastKnownEligibleLeaderReplicas\":[]}],\"topicId\":\"$topic1Id\"},{\"name\":\"topic2\",\"internal\":false,\"partitions\":[{\"partition\":0,\"partitionLeader\":{\"id\":1,\"idString\":\"1\",\"host\":\"localhost\",\"port\":$port,\"rack\":\"UNKNOWN\"},\"partitionReplicas\":[{\"id\":1,\"idString\":\"1\",\"host\":\"localhost\",\"port\":$port,\"rack\":\"UNKNOWN\"}],\"inSyncReplicas\":[{\"id\":1,\"idString\":\"1\",\"host\":\"localhost\",\"port\":$port,\"rack\":\"UNKNOWN\"}],\"eligibleLeaderReplicas\":[],\"lastKnownEligibleLeaderReplicas\":[]}],\"topicId\":\"$topic2Id\"}]}"
//                .replaceAll("\\$port", String.valueOf(kafkaPort))
//                .replaceAll("\\$topic1Id", topic1Id)
//                .replaceAll("\\$topic2Id", topic2Id);

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("describeTopics")
                .arguments(Map.of("topicNames", List.of(topic1, topic2)))
                .build());

        assertTextMcpToolResult(expectedText, result);
    }

    @Test
    @Disabled("To be implemented")
    void getHistoricalMcpResponses() {
        final String historicalMcpTool = "listTopics";
        final String startDate = "2026-01-01";
        final String endDate = "2026-01-31";

        final String expectedText = """
                [{"id":1,"kafkaClusterName":"testCluster","toolName":"listTopics","jsonResponse":"{\\"topicNames\\":\
                [\\"topic1\\",\\"topic2\\"]}","timestamp":"2026-01-01T00:00:00Z"},{"id":2,"kafkaClusterName":"testCluster",\
                "toolName":"listTopics","jsonResponse":"{\\"topicNames\\":[\\"topic1\\",\\"topic3\\"]}","timestamp":"2026-01-31T00:00:00Z"}]\
                """;

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("getHistoricalMcpResponses")
                .arguments(Map.of("mcpTool", historicalMcpTool, "startDate", startDate, "endDate", endDate))
                .build());

        assertTextMcpToolResult(expectedText, result);
    }

    @Test
    @Disabled("To be implemented")
    void saveProblemsAndRecommendationsSummary() {
        final String summary = "This is a test summary of problems and recommendations from Claude";

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("saveProblemsAndRecommendationsSummary")
                .arguments(Map.of("summary", summary))
                .build());

        assertFalse(result.isError());
    }

    @Test
    @Disabled("To be implemented")
    void getKafkaTerraformConfig() throws IOException {
        final String resourceUri = "file://mcp/kafka-terraform.yaml";
        final String kafkaTestConfig = """
                kafka:
                    clusterName: test-cluster
                    version: 3.4.0
                    multiAZdeployment: false\
                """;

        final ReadResourceResult result = mcpClient.readResource(new ReadResourceRequest(resourceUri));

        assertTextMcpResourceResult(resourceUri, kafkaTestConfig, result);
    }

    private TopicDescription getTopicDescription(final AdminClient admin, final String topicName)
            throws ExecutionException, InterruptedException {
        return admin.describeTopics(singletonList(topicName))
                .allTopicNames()
                .get()
                .entrySet()
                .stream()
                .findFirst()
                .orElseThrow()
                .getValue();
    }
}
