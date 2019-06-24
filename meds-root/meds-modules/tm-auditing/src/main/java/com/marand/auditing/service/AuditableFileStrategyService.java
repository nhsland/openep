package com.marand.auditing.service;

import care.better.auditing.AuditDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @author Primoz Delopst
 */

@SuppressWarnings("ClassWithMultipleLoggers")
@Service
@ConditionalOnProperty(name = "auditing.strategy", havingValue = "file")
public class AuditableFileStrategyService implements AuditableService
{
  private static final Logger AUDIT_LOG = LoggerFactory.getLogger("audit-log");
  private static final Logger LOG = LoggerFactory.getLogger(AuditableFileStrategyService.class);

  private final ObjectMapper objectMapper;

  @Autowired
  public AuditableFileStrategyService(final ObjectMapper objectMapper)
  {
    this.objectMapper = objectMapper;
  }

  @Override
  public void logAuditData(final AuditDto auditDto)
  {
    try
    {
      AUDIT_LOG.info(objectMapper.writeValueAsString(auditDto));
    }
    catch (final JsonProcessingException e)
    {
      LOG.error("Error occurred when auditing to file", e);
    }
  }
}
