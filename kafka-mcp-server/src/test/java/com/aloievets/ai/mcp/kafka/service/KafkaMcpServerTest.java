package com.aloievets.ai.mcp.kafka.service;

import static com.aloievets.ai.mcp.kafka.service.McpTestUtils.assertTextMcpResourceResult;
import static com.aloievets.ai.mcp.kafka.service.McpTestUtils.assertTextMcpToolResult;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.aloievets.ai.mcp.kafka.client.model.KafkaNodeDto;
import com.aloievets.ai.mcp.kafka.client.model.KafkaTopicDescriptionDto;
import com.aloievets.ai.mcp.kafka.client.model.KafkaTopicPartitionInfoDto;
import com.aloievets.ai.mcp.kafka.client.service.KafkaStatusViewer;
import com.aloievets.ai.mcp.kafka.model.KafkaNodesDto;
import com.aloievets.ai.mcp.kafka.model.KafkaRecommendationsSummaryDto;
import com.aloievets.ai.mcp.kafka.model.KafkaTopicDescriptionsDto;
import com.aloievets.ai.mcp.kafka.model.KafkaTopicNamesDto;
import com.aloievets.ai.mcp.kafka.service.history.McpHistory;
import com.aloievets.ai.mcp.kafka.service.history.McpHistoryConverter;
import com.aloievets.ai.mcp.kafka.service.history.McpHistoryRepository;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mcp-test")
public class KafkaMcpServerTest {
    @LocalServerPort
    private int port;
    @MockitoBean
    private KafkaStatusViewer kafkaStatusViewer;
    @MockitoBean
    private McpHistoryConverter historyConverter;
    @MockitoBean
    private McpHistoryRepository historyRepository;
    @MockitoBean
    private KafkaTerraformConfigReader terraformConfigReader;
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
    void listTopics() {
        final Set<String> topics = new TreeSet<>();
        topics.add("topic1");
        topics.add("topic2");
        when(kafkaStatusViewer.listTopics()).thenReturn(topics);
        final String expectedText = "{\"topicNames\":[\"topic1\",\"topic2\"]}";
        final var expectedDto = new KafkaTopicNamesDto(topics);
        final var mcpHistory = new McpHistory();
        mcpHistory.setId(1L);
        mcpHistory.setToolName("listTopics");
        when(historyConverter.toMcpHistory(eq("listTopics"), eq(expectedDto))).thenReturn(mcpHistory);

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("listTopics")
                .build());

        assertTextMcpToolResult(expectedText, result);
        verify(historyConverter).toMcpHistory(eq("listTopics"), eq(expectedDto));
        verify(historyRepository).save(mcpHistory);
    }

    @Test
    void describeClusterController() {
        final KafkaNodeDto controllerNode = new KafkaNodeDto(1, "1", "localhost", 9092, "rack1");
        when(kafkaStatusViewer.describeClusterController()).thenReturn(controllerNode);
        final String expectedText = "{\"id\":1,\"idString\":\"1\",\"host\":\"localhost\",\"port\":9092,\"rack\":\"rack1\"}";
        final var mcpHistory = new McpHistory();
        mcpHistory.setId(2L);
        mcpHistory.setToolName("describeClusterController");
        when(historyConverter.toMcpHistory(eq("describeClusterController"), eq(controllerNode))).thenReturn(mcpHistory);

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("describeClusterController")
                .build());

        assertTextMcpToolResult(expectedText, result);
        verify(historyConverter).toMcpHistory(eq("describeClusterController"), eq(controllerNode));
        verify(historyRepository).save(mcpHistory);
    }

    @Test
    void describeClusterNodes() {
        final KafkaNodeDto node1 = new KafkaNodeDto(1, "1", "localhost", 9092, "rack1");
        final KafkaNodeDto node2 = new KafkaNodeDto(2, "2", "localhost", 9093, "rack2");
        final List<KafkaNodeDto> nodes = List.of(node1, node2);
        when(kafkaStatusViewer.describeClusterNodes()).thenReturn(nodes);
        final String expectedText = "{\"nodes\":[{\"id\":1,\"idString\":\"1\",\"host\":\"localhost\",\"port\":9092,\"rack\":\"rack1\"},{\"id\":2,\"idString\":\"2\",\"host\":\"localhost\",\"port\":9093,\"rack\":\"rack2\"}]}";
        final var expectedDto = new KafkaNodesDto(nodes);
        final var mcpHistory = new McpHistory();
        mcpHistory.setId(3L);
        mcpHistory.setToolName("describeClusterNodes");
        when(historyConverter.toMcpHistory(eq("describeClusterNodes"), eq(expectedDto))).thenReturn(mcpHistory);

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("describeClusterNodes")
                .build());

        assertTextMcpToolResult(expectedText, result);
        verify(historyConverter).toMcpHistory(eq("describeClusterNodes"), eq(expectedDto));
        verify(historyRepository).save(mcpHistory);
    }

    @Test
    void describeTopics() {
        final String topic1 = "test-topic1";
        final String topic2 = "test-topic2";
        final KafkaNodeDto leaderNode = new KafkaNodeDto(1, "1", "localhost", 9092, "rack1");
        final KafkaNodeDto node2 = new KafkaNodeDto(2, "2", "localhost", 9092, "rack2");
        final KafkaNodeDto node3 = new KafkaNodeDto(3, "3", "localhost", 9093, "rack3");
        final List<KafkaNodeDto> allNodes = List.of(leaderNode, node2, node3);
        final KafkaTopicPartitionInfoDto partitionInfo1 = new KafkaTopicPartitionInfoDto(
                0, leaderNode, allNodes, allNodes, allNodes, allNodes);
        final KafkaTopicPartitionInfoDto partitionInfo2 = new KafkaTopicPartitionInfoDto(
                1, leaderNode, allNodes, allNodes, allNodes, allNodes);
        final KafkaTopicDescriptionDto topicDescription1 = new KafkaTopicDescriptionDto(
                topic1, false, List.of(partitionInfo1, partitionInfo2), "topic1-id-123");
        final KafkaTopicPartitionInfoDto partitionInfo3 = new KafkaTopicPartitionInfoDto(
                2, leaderNode, allNodes, allNodes, allNodes, allNodes);
        final KafkaTopicPartitionInfoDto partitionInfo4 = new KafkaTopicPartitionInfoDto(
                3, leaderNode, allNodes, allNodes, allNodes, allNodes);
        final KafkaTopicDescriptionDto topicDescription2 = new KafkaTopicDescriptionDto(
                topic2, false, List.of(partitionInfo3, partitionInfo4), "topic2-id-456");
        final List<KafkaTopicDescriptionDto> topicDescriptions = List.of(topicDescription1, topicDescription2);

        when(kafkaStatusViewer.describeTopics(List.of(topic1, topic2))).thenReturn(topicDescriptions);

        final String expectedText = """
                {"topicDescriptions":[\
                {"name":"test-topic1","internal":false,"partitions":[\
                {"partition":0,"partitionLeader":{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                "partitionReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}],\
                "inSyncReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}],\
                "eligibleLeaderReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}],\
                "lastKnownEligibleLeaderReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}]},\
                {"partition":1,"partitionLeader":{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                "partitionReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}],\
                "inSyncReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}],\
                "eligibleLeaderReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}],\
                "lastKnownEligibleLeaderReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}]}],\
                "topicId":"topic1-id-123"},\
                {"name":"test-topic2","internal":false,"partitions":[\
                {"partition":2,"partitionLeader":{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                "partitionReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}],\
                "inSyncReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}],\
                "eligibleLeaderReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}],\
                "lastKnownEligibleLeaderReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}]},\
                {"partition":3,"partitionLeader":{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                "partitionReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}],\
                "inSyncReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}],\
                "eligibleLeaderReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}],\
                "lastKnownEligibleLeaderReplicas":[{"id":1,"idString":"1","host":"localhost","port":9092,"rack":"rack1"},\
                {"id":2,"idString":"2","host":"localhost","port":9092,"rack":"rack2"},\
                {"id":3,"idString":"3","host":"localhost","port":9093,"rack":"rack3"}]}],\
                "topicId":"topic2-id-456"}]}\
                """;
        final var expectedDto = new KafkaTopicDescriptionsDto(topicDescriptions);
        final var mcpHistory = new McpHistory();
        mcpHistory.setId(5L);
        mcpHistory.setToolName("describeTopics");
        when(historyConverter.toMcpHistory(eq("describeTopics"), eq(expectedDto))).thenReturn(mcpHistory);

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("describeTopics")
                .arguments(Map.of("topicNames", List.of(topic1, topic2)))
                .build());

        assertTextMcpToolResult(expectedText, result);
        verify(kafkaStatusViewer).describeTopics(List.of(topic1, topic2));
        verify(historyConverter).toMcpHistory(eq("describeTopics"), eq(expectedDto));
        verify(historyRepository).save(mcpHistory);
        verifyNoMoreInteractions(kafkaStatusViewer, historyConverter, historyRepository);
    }

    @Test
    void getHistoricalMcpResponses() {
        final String historicalMcpTool = "listTopics";
        final String startDate = "2026-01-01";
        final String endDate = "2026-01-31";
        final Instant startDateInstant = Instant.parse(startDate + "T00:00:00Z");
        final Instant endDateInstant = Instant.parse(endDate + "T00:00:00Z");

        final McpHistory history1 = new McpHistory();
        history1.setId(1L);
        history1.setKafkaClusterName("testCluster");
        history1.setToolName("listTopics");
        history1.setJsonResponse("{\"topicNames\":[\"topic1\",\"topic2\"]}");
        history1.setTimestamp(startDateInstant);

        final McpHistory history2 = new McpHistory();
        history2.setId(2L);
        history2.setKafkaClusterName("testCluster");
        history2.setToolName("listTopics");
        history2.setJsonResponse("{\"topicNames\":[\"topic1\",\"topic3\"]}");
        history2.setTimestamp(endDateInstant);

        when(historyRepository.findByToolNameAndTimestampBetween(historicalMcpTool, startDateInstant, endDateInstant))
                .thenReturn(List.of(history1, history2));

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
        verify(historyRepository).findByToolNameAndTimestampBetween(historicalMcpTool, startDateInstant, endDateInstant);
        verifyNoMoreInteractions(historyRepository);
    }

    @Test
    void saveProblemsAndRecommendationsSummary() {
        final String summaryKey = "problemsAndRecommendationsSummary";
        final String summary = "This is a test summary of problems and recommendations from Claude";
        final var summaryDto = new KafkaRecommendationsSummaryDto(summary);
        final var history = new McpHistory();
        history.setId(1L);
        history.setKafkaClusterName("testCluster");
        history.setToolName(summaryKey);
        history.setJsonResponse("{\"summary\":\"" + summary + "\"}");
        history.setTimestamp(Instant.now());

        when(historyConverter.toMcpHistory(summaryKey, summaryDto)).thenReturn(history);

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("saveProblemsAndRecommendationsSummary")
                .arguments(Map.of("summary", summary))
                .build());

        assertFalse(result.isError());
        verify(historyConverter).toMcpHistory(summaryKey, summaryDto);
        verify(historyRepository).save(history);
        verifyNoMoreInteractions(historyConverter, historyRepository);
    }

    @Test
    void getKafkaTerraformConfig() throws IOException {
        final String resourceUri = "file://mcp/kafka-terraform.yaml";
        final String kafkaTestConfig = """
                kafka:
                    clusterName: test-cluster
                    version: 3.4.0
                    multiAZdeployment: false\
                """;
        when(terraformConfigReader.getKafkaTerraformConfig()).thenReturn(kafkaTestConfig);

        final ReadResourceResult result = mcpClient.readResource(new ReadResourceRequest(resourceUri));

        assertTextMcpResourceResult(resourceUri, kafkaTestConfig, result);
        verify(terraformConfigReader).getKafkaTerraformConfig();
        verifyNoMoreInteractions(terraformConfigReader, historyConverter, historyRepository);
    }
}
