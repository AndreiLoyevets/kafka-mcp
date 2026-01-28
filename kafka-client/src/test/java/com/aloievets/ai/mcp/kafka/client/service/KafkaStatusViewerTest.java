package com.aloievets.ai.mcp.kafka.client.service;

import com.aloievets.ai.mcp.kafka.client.model.KafkaNodeDto;
import com.aloievets.ai.mcp.kafka.client.model.KafkaTopicDescriptionDto;
import com.aloievets.ai.mcp.kafka.client.service.exceptions.KafkaClientException;
import com.aloievets.ai.mcp.kafka.client.service.exceptions.KafkaClientTimeoutException;
import com.aloievets.ai.mcp.kafka.client.service.exceptions.TopicsNotExistException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaStatusViewerTest {
    private static final long TIMEOUT_MS = 1000L;
    @Mock
    private AdminClient kafkaAdminClient;
    @Mock
    private DescribeClusterResult describeClusterResult;
    @Mock
    private ListTopicsResult listTopicsResult;
    @Mock
    private DescribeTopicsResult describeTopicsResult;
    @Mock
    private KafkaFuture<Node> controllerFuture;
    @Mock
    private KafkaFuture<Collection<Node>> nodesFuture;
    @Mock
    private KafkaFuture<Set<String>> namesFuture;
    @Mock
    private KafkaFuture<Map<String, TopicDescription>> allTopicNamesFuture;
    private KafkaStatusViewer kafkaStatusViewer;

    @BeforeEach
    void setUp() {
        kafkaStatusViewer = new KafkaStatusViewer(kafkaAdminClient, TIMEOUT_MS);
    }

    @Test
    void testDescribeClusterController() throws Exception {
        // Given
        var node = new Node(1, "localhost", 9092);
        when(kafkaAdminClient.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.controller()).thenReturn(controllerFuture);
        when(controllerFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenReturn(node);

        // When
        KafkaNodeDto result = kafkaStatusViewer.describeClusterController();

        // Then
        assertEquals(1, result.id());
        assertEquals("localhost", result.host());
        assertEquals(9092, result.port());
    }

    @Test
    void testDescribeClusterControllerTimeout() throws Exception {
        // Given
        when(kafkaAdminClient.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.controller()).thenReturn(controllerFuture);
        when(controllerFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new TimeoutException());

        // When & Then
        assertThrows(KafkaClientTimeoutException.class, () -> kafkaStatusViewer.describeClusterController());
    }

    @Test
    void testDescribeClusterControllerInterrupted() throws Exception {
        // Given
        when(kafkaAdminClient.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.controller()).thenReturn(controllerFuture);
        when(controllerFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException());

        // When & Then
        assertThrows(KafkaClientException.class, () -> kafkaStatusViewer.describeClusterController());
    }

    @Test
    void testDescribeClusterControllerExecutionException() throws Exception {
        // Given
        when(kafkaAdminClient.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.controller()).thenReturn(controllerFuture);
        when(controllerFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new ExecutionException(new RuntimeException()));

        // When & Then
        assertThrows(KafkaClientException.class, () -> kafkaStatusViewer.describeClusterController());
    }

    @Test
    void testDescribeClusterNodes() throws Exception {
        // Given
        var node1 = new Node(1, "localhost", 9092);
        var node2 = new Node(2, "localhost", 9093);
        var nodes = List.of(node1, node2);
        when(kafkaAdminClient.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.nodes()).thenReturn(nodesFuture);
        when(nodesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenReturn(nodes);

        // When
        List<KafkaNodeDto> result = kafkaStatusViewer.describeClusterNodes();

        // Then
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).id());
        assertEquals("localhost", result.get(0).host());
        assertEquals(9092, result.get(0).port());
        assertEquals(2, result.get(1).id());
        assertEquals("localhost", result.get(1).host());
        assertEquals(9093, result.get(1).port());
    }

    @Test
    void testDescribeClusterNodesTimeout() throws Exception {
        // Given
        when(kafkaAdminClient.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.nodes()).thenReturn(nodesFuture);
        when(nodesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new TimeoutException());

        // When & Then
        assertThrows(KafkaClientTimeoutException.class, () -> kafkaStatusViewer.describeClusterNodes());
    }

    @Test
    void testDescribeClusterNodesInterrupted() throws Exception {
        // Given
        when(kafkaAdminClient.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.nodes()).thenReturn(nodesFuture);
        when(nodesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException());

        // When & Then
        assertThrows(KafkaClientException.class, () -> kafkaStatusViewer.describeClusterNodes());
    }

    @Test
    void testDescribeClusterNodesExecutionException() throws Exception {
        // Given
        when(kafkaAdminClient.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.nodes()).thenReturn(nodesFuture);
        when(nodesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new ExecutionException(new RuntimeException()));

        // When & Then
        assertThrows(KafkaClientException.class, () -> kafkaStatusViewer.describeClusterNodes());
    }

    @Test
    void testListTopics() throws Exception {
        // Given
        var topics = Set.of("topic1", "topic2");
        when(kafkaAdminClient.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(namesFuture);
        when(namesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenReturn(topics);

        // When
        Set<String> result = kafkaStatusViewer.listTopics();

        // Then
        assertEquals(topics, result);
    }

    @Test
    void testListTopicsTimeout() throws Exception {
        // Given
        when(kafkaAdminClient.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(namesFuture);
        when(namesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new TimeoutException());

        // When & Then
        assertThrows(KafkaClientTimeoutException.class, () -> kafkaStatusViewer.listTopics());
    }

    @Test
    void testListTopicsInterrupted() throws Exception {
        // Given
        when(kafkaAdminClient.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(namesFuture);
        when(namesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException());

        // When & Then
        assertThrows(KafkaClientException.class, () -> kafkaStatusViewer.listTopics());
    }

    @Test
    void testListTopicsExecutionException() throws Exception {
        // Given
        when(kafkaAdminClient.listTopics()).thenReturn(listTopicsResult);
        when(listTopicsResult.names()).thenReturn(namesFuture);
        when(namesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new ExecutionException(new RuntimeException()));

        // When & Then
        assertThrows(KafkaClientException.class, () -> kafkaStatusViewer.listTopics());
    }

    @Test
    void testDescribeTopics() throws Exception {
        // Given
        var topicNames = List.of("topic1");
        var node = new Node(1, "localhost", 9092);
        var partition = new TopicPartitionInfo(0, node, List.of(node), List.of(node));
        var topicDescription = new TopicDescription("topic1", false, List.of(partition));
        var topicDescriptions = Map.of("topic1", topicDescription);
        when(kafkaAdminClient.describeTopics(topicNames)).thenReturn(describeTopicsResult);
        when(describeTopicsResult.allTopicNames()).thenReturn(allTopicNamesFuture);
        when(allTopicNamesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenReturn(topicDescriptions);

        // When
        List<KafkaTopicDescriptionDto> result = kafkaStatusViewer.describeTopics(topicNames);

        // Then
        assertEquals(1, result.size());
        assertEquals("topic1", result.get(0).name());
    }

    @Test
    void testDescribeTopicsTimeout() throws Exception {
        // Given
        var topicNames = List.of("topic1");
        when(kafkaAdminClient.describeTopics(topicNames)).thenReturn(describeTopicsResult);
        when(describeTopicsResult.allTopicNames()).thenReturn(allTopicNamesFuture);
        when(allTopicNamesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new TimeoutException());

        // When & Then
        assertThrows(KafkaClientTimeoutException.class, () -> kafkaStatusViewer.describeTopics(topicNames));
    }

    @Test
    void testDescribeTopicsInterrupted() throws Exception {
        // Given
        var topicNames = List.of("topic1");
        when(kafkaAdminClient.describeTopics(topicNames)).thenReturn(describeTopicsResult);
        when(describeTopicsResult.allTopicNames()).thenReturn(allTopicNamesFuture);
        when(allTopicNamesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException());

        // When & Then
        assertThrows(KafkaClientException.class, () -> kafkaStatusViewer.describeTopics(topicNames));
    }

    @Test
    void testDescribeTopicsExecutionException() throws Exception {
        // Given
        var topicNames = List.of("topic1");
        when(kafkaAdminClient.describeTopics(topicNames)).thenReturn(describeTopicsResult);
        when(describeTopicsResult.allTopicNames()).thenReturn(allTopicNamesFuture);
        when(allTopicNamesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new ExecutionException(new RuntimeException()));

        // When & Then
        assertThrows(KafkaClientException.class, () -> kafkaStatusViewer.describeTopics(topicNames));
    }

    @Test
    void testDescribeTopicsTopicAuthorizationException() throws Exception {
        // Given
        var topicNames = List.of("topic1");
        var unauthorizedTopics = Set.of("topic1");
        when(kafkaAdminClient.describeTopics(topicNames)).thenReturn(describeTopicsResult);
        when(describeTopicsResult.allTopicNames()).thenReturn(allTopicNamesFuture);
        when(allTopicNamesFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)).thenThrow(new ExecutionException(new TopicAuthorizationException(unauthorizedTopics)));

        // When & Then
        assertThrows(TopicsNotExistException.class, () -> kafkaStatusViewer.describeTopics(topicNames));
    }
}
