package com.aloievets.ai.mcp.kafka.exceptions;

import com.aloievets.ai.mcp.kafka.client.service.exceptions.KafkaClientException;
import com.aloievets.ai.mcp.kafka.client.service.exceptions.KafkaClientTimeoutException;
import com.aloievets.ai.mcp.kafka.client.service.exceptions.TopicsNotExistException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class McpExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(McpExceptionHandler.class);

    @AfterThrowing(pointcut = "@annotation(org.springaicommunity.mcp.annotation.McpTool)"
            + " || @annotation(org.springaicommunity.mcp.annotation.McpResource)", throwing = "e")
    public Object handleMcpToolExceptions(final JoinPoint joinPoint, final Exception e) throws Exception {
        LOG.debug("Executing aspect after exception thrown by method {}", joinPoint.getSignature()
                .getName());

        if (e instanceof TopicsNotExistException
                || e instanceof KafkaClientTimeoutException
                || e instanceof KafkaClientException) {
            LOG.warn(String.format("Failed to execute %s", joinPoint.getSignature().getName()), e);
            throw e;
        } else if (e instanceof GenericMcpException) {
            LOG.debug("Propagating the exception in aspect since it was already handled", e);
            throw e;
        } else {
            LOG.error(String.format("Unexpected failure in %s", joinPoint.getSignature().getName()), e);
            throw new GenericMcpException();
        }
    }
}
