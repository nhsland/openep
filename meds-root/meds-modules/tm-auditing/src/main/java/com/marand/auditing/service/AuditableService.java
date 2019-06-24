package com.marand.auditing.service;

import care.better.auditing.AuditDto;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author Primoz Delopst
 */

public interface AuditableService
{
  void logAuditData(AuditDto auditDto) throws JsonProcessingException;
}
