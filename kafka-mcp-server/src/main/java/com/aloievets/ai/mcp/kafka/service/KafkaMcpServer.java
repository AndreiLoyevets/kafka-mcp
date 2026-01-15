package com.aloievets.ai.mcp.kafka.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;

import com.aloievets.ai.mcp.kafka.client.model.KafkaNodeDto;
import com.aloievets.ai.mcp.kafka.client.model.KafkaTopicDescriptionDto;
import com.aloievets.ai.mcp.kafka.client.service.KafkaStatusViewer;
import com.aloievets.ai.mcp.kafka.exceptions.GenericMcpException;
import com.aloievets.ai.mcp.kafka.model.KafkaNodesDto;
import com.aloievets.ai.mcp.kafka.model.KafkaRecommendationsSummaryDto;
import com.aloievets.ai.mcp.kafka.model.KafkaTopicDescriptionsDto;
import com.aloievets.ai.mcp.kafka.model.KafkaTopicNamesDto;
import com.aloievets.ai.mcp.kafka.service.history.McpHistory;
import com.aloievets.ai.mcp.kafka.service.history.McpHistoryConverter;
import com.aloievets.ai.mcp.kafka.service.history.McpHistoryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class KafkaMcpServer {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaMcpServer.class);
    private final KafkaStatusViewer kafkaStatusViewer;
    private final KafkaTerraformConfigReader kafkaTerraformConfigReader;
    private final McpHistoryConverter historyConverter;
    private final McpHistoryRepository historyRepository;

    public KafkaMcpServer(final KafkaStatusViewer kafkaStatusViewer,
            final KafkaTerraformConfigReader kafkaTerraformConfigReader, final McpHistoryConverter historyConverter,
            final McpHistoryRepository historyRepository) {
        this.kafkaStatusViewer = kafkaStatusViewer;
        this.kafkaTerraformConfigReader = kafkaTerraformConfigReader;
        this.historyConverter = historyConverter;
        this.historyRepository = historyRepository;
    }

    @McpTool(title = "Describe Kafka cluster controller", description = "Get Kafka cluster controller node",
            generateOutputSchema = true)
    @Cacheable(value = "mcp-responses", key = "'describeClusterController'")
    public KafkaNodeDto describeClusterController() {
        LOG.debug("Requested to describe cluster controller");
        final KafkaNodeDto node = kafkaStatusViewer.describeClusterController();
        saveMcpResponseToHistory("describeClusterController", node);

        return node;
    }

    @McpTool(title = "Describe Kafka cluster nodes", description = "Get all Kafka cluster nodes details",
            generateOutputSchema = true)
    @Cacheable(value = "mcp-responses", key = "'describeClusterNodes'")
    public KafkaNodesDto describeClusterNodes() {
        LOG.debug("Requested to describe cluster nodes");
        final List<KafkaNodeDto> nodes = kafkaStatusViewer.describeClusterNodes();
        final KafkaNodesDto nodesDto = new KafkaNodesDto(nodes);
        saveMcpResponseToHistory("describeClusterNodes", nodesDto);

        return nodesDto;
    }

    @McpTool(title = "List topics", description = "List Kafka topics in my cluster",
            generateOutputSchema = true)
    @Cacheable(value = "mcp-responses", key = "'listTopics'")
    public KafkaTopicNamesDto listTopics() {
        LOG.debug("Requested to list topics");
        final KafkaTopicNamesDto topicNamesDto = new KafkaTopicNamesDto(kafkaStatusViewer.listTopics());
        saveMcpResponseToHistory("listTopics", topicNamesDto);

        return topicNamesDto;
    }

    @McpTool(title = "Describe topics", description = "Get Kafka topic details for the provided collection of topic names",
            generateOutputSchema = true)
    @Cacheable(value = "mcp-responses", key = "'describeTopics-' + (#topicNames?.hashCode() ?: 'null')")
    public KafkaTopicDescriptionsDto describeTopics(
            @McpToolParam(description = "Collection of Kafka topic names to describe") final Collection<String> topicNames) {
        LOG.debug("Requested to describe topics: {}", topicNames);
        final List<KafkaTopicDescriptionDto> topicDescriptions = kafkaStatusViewer.describeTopics(topicNames);
        final KafkaTopicDescriptionsDto topicDescriptionsDto = new KafkaTopicDescriptionsDto(topicDescriptions);
        saveMcpResponseToHistory("describeTopics", topicDescriptionsDto);

        return topicDescriptionsDto;
    }

    @McpTool(title = "Get historical MCP responses about Kafka cluster or topics state",
            description = "Provide a tool name and start and end dates range to get the historical MCP responses from the past")
    @Cacheable(value = "mcp-responses",
            key = "'getHistoricalMcpResponses-' + (#mcpTool + '-' + #startDate + '-' + #endDate)")
    public List<McpHistory> getHistoricalMcpResponses(
            @McpToolParam(description = "Kafka MCP tool name") final String mcpTool,
            @McpToolParam(description = "start date, string in format yyyy-MM-dd") final String startDate,
            @McpToolParam(description = "end date, string in format yyyy-MM-dd") final String endDate) {
        LOG.debug("Requested to get historical MCP responses for {} from {} to {}", mcpTool, startDate, endDate);

        final Instant startDateInstant = parseDate(startDate);
        final Instant endDateInstant = parseDate(endDate);

        final List<McpHistory> history = historyRepository.findByToolNameAndTimestampBetween(mcpTool,
                startDateInstant, endDateInstant);
        LOG.debug("Found {} historical MCP responses for {} from {} to {}", history.size(), mcpTool, startDate, endDate);

        return history;
    }

    @McpTool(title = "Save key problems and recommendations",
            description = "Saves the summary of the Kafka cluster problems and recommended fixes. Before saving the summary, it is required to get user's consent first")
    public void saveProblemsAndRecommendationsSummary(
            @McpToolParam(description = "Summary of the Kafka cluster problems and recommended fixes") final String summary) {
        LOG.debug("Requested to save problems and recommendations summary: {}", summary);
        final KafkaRecommendationsSummaryDto summaryDto = new KafkaRecommendationsSummaryDto(summary);
        saveMcpResponseToHistory("problemsAndRecommendationsSummary", summaryDto);
    }

    @McpResource(uri = "file://mcp/kafka-terraform.yaml", mimeType = "text/yaml", title = "Kafka cluster terraform configuration", description = "Kafka cluster terraform configuration. Use it to compare the current cluster state vs the terraform configuration.")
    @Cacheable(value = "mcp-responses", key = "'getKafkaTerraformConfig'")
    public String getKafkaTerraformConfig() {
        try {
            return kafkaTerraformConfigReader.getKafkaTerraformConfig();
        } catch (final Exception e) {
            final String message = "Failed to load Kafka terraform config, the resource is unavailable. Contact the development team if the problem remains";
            LOG.error(message, e);
            throw new GenericMcpException(message);
        }
    }

    private void saveMcpResponseToHistory(final String methodName, final Object result) {
        final McpHistory history = historyConverter.toMcpHistory(methodName, result);
        LOG.debug("Saving MCP history: {}", history);
        historyRepository.save(history);
    }

    private Instant parseDate(final String dateString) {
        final LocalDate localDate = LocalDate.  parse(dateString);
        final Instant instant = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();

        LOG.debug("Parsed date {} to {}", dateString, instant);
        return instant;
    }
}
