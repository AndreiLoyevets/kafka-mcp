package com.aloievets.ai.mcp.kafka.service.history;

import java.time.Instant;
import java.util.Objects;
import java.util.StringJoiner;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class McpHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String toolName;
    private String jsonResponse;
    private Instant timestamp;

    public McpHistory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(final String toolName) {
        this.toolName = toolName;
    }

    public String getJsonResponse() {
        return jsonResponse;
    }

    public void setJsonResponse(final String jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final McpHistory that = (McpHistory) o;
        return Objects.equals(id, that.id) && Objects.equals(toolName, that.toolName)
                && Objects.equals(jsonResponse, that.jsonResponse) && Objects.equals(timestamp,
                that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(toolName);
        result = 31 * result + Objects.hashCode(jsonResponse);
        result = 31 * result + Objects.hashCode(timestamp);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", McpHistory.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("toolName='" + toolName + "'")
                .add("jsonResponse='" + jsonResponse + "'")
                .add("timestamp=" + timestamp)
                .toString();
    }
}