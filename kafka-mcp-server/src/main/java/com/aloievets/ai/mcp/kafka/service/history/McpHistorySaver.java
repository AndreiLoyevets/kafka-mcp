package com.aloievets.ai.mcp.kafka.service.history;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class McpHistorySaver {
    private static final Logger LOG = LoggerFactory.getLogger(McpHistorySaver.class);
    private final McpHistoryConverter historyConverter;
    private final McpHistoryRepository historyRepository;

    public McpHistorySaver(final McpHistoryConverter historyConverter, final McpHistoryRepository historyRepository) {
        this.historyConverter = historyConverter;
        this.historyRepository = historyRepository;
    }

    @AfterReturning(pointcut = "@annotation(org.springaicommunity.mcp.annotation.McpTool)", returning = "result")
    public Object saveMcpResponseToHistory(final JoinPoint joinPoint, final Object result) {
        LOG.debug("Executing aspect after returning from method {}", joinPoint.getSignature().getName());

        final McpHistory history = historyConverter.toMcpHistory(joinPoint.getSignature()
                .getName(), result);
        LOG.debug("Saving MCP history: {}", history);
        historyRepository.save(history);
        return result;
    }
}
