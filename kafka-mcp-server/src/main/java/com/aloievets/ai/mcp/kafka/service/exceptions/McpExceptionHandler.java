package com.aloievets.ai.mcp.kafka.service.exceptions;

import com.aloievets.ai.mcp.kafka.client.service.exceptions.KafkaClientException;
import com.aloievets.ai.mcp.kafka.client.service.exceptions.KafkaClientTimeoutException;
import com.aloievets.ai.mcp.kafka.client.service.exceptions.TopicsNotExistException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class McpExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(McpExceptionHandler.class);

    @Around("@annotation(org.springaicommunity.mcp.annotation.McpTool)"
            + " || @annotation(org.springaicommunity.mcp.annotation.McpResource)")
    public Object handleMcpToolExceptions(final ProceedingJoinPoint joinPoint) throws Throwable {
        LOG.debug("Executing aspect around method {}", joinPoint.getSignature().getName());

        try {
            return joinPoint.proceed();
        } catch (final TopicsNotExistException | KafkaClientTimeoutException | KafkaClientException e) {
            LOG.warn(String.format("Failed to execute %s", joinPoint.getSignature()
                    .getName()), e);
            throw e;
        } catch (final GenericMcpException | McpTimeoutException e) {
            LOG.debug("Propagating the exception in aspect since it was already handled", e);
            throw e;
        } catch (final Exception e) {
            LOG.error(String.format("Unexpected failure in %s", joinPoint.getSignature().getName()), e);
            throw new GenericMcpException();
        }
    }
}
