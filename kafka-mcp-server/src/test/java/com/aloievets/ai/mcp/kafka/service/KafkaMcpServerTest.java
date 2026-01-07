package com.aloievets.ai.mcp.kafka.service;

import static com.aloievets.ai.mcp.kafka.service.McpTestUtils.assertTextMcpResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.aloievets.ai.mcp.kafka.client.model.KafkaNodeDto;
import com.aloievets.ai.mcp.kafka.client.model.KafkaTopicDescriptionDto;
import com.aloievets.ai.mcp.kafka.client.model.KafkaTopicPartitionInfoDto;
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
    void listTopics() {
        final Set<String> topics = new TreeSet<>();
        topics.add("topic1");
        topics.add("topic2");
        when(kafkaStatusViewer.listTopics()).thenReturn(topics);
        final String expectedText = "{\"topicNames\":[\"topic1\",\"topic2\"]}";

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("listTopics")
                .build());

        assertTextMcpResult(expectedText, result);
    }

    @Test
    void describeClusterController() {
        final KafkaNodeDto controllerNode = new KafkaNodeDto(1, "1", "localhost", 9092, "rack1");
        when(kafkaStatusViewer.describeClusterController()).thenReturn(controllerNode);
        final String expectedText = "{\"id\":1,\"idString\":\"1\",\"host\":\"localhost\",\"port\":9092,\"rack\":\"rack1\"}";

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("describeClusterController")
                .build());

        assertTextMcpResult(expectedText, result);
    }

    @Test
    void describeClusterNodes() {
        final KafkaNodeDto node1 = new KafkaNodeDto(1, "1", "localhost", 9092, "rack1");
        final KafkaNodeDto node2 = new KafkaNodeDto(2, "2", "localhost", 9093, "rack2");
        when(kafkaStatusViewer.describeClusterNodes()).thenReturn(List.of(node1, node2));
        final String expectedText = "{\"nodes\":[{\"id\":1,\"idString\":\"1\",\"host\":\"localhost\",\"port\":9092,\"rack\":\"rack1\"},{\"id\":2,\"idString\":\"2\",\"host\":\"localhost\",\"port\":9093,\"rack\":\"rack2\"}]}";

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("describeClusterNodes")
                .build());

        assertTextMcpResult(expectedText, result);
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

        when(kafkaStatusViewer.describeTopics(any())).thenReturn(topicDescriptions);
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

        final CallToolResult result = mcpClient.callTool(CallToolRequest.builder()
                .name("describeTopics")
                .arguments(java.util.Map.of("topicNames", List.of(topic1, topic2)))
                .build());

        assertTextMcpResult(expectedText, result);
    }
}
