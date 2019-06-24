package com.marand.auditing.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;

import care.better.auditing.AuditDto;
import care.better.auditing.action.RestActionExecutor;
import care.better.auditing.auditor.Auditor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.REDIRECTION;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

/**
 * @author Primoz Delopst
 */

@Service
@ConditionalOnProperty(name = "auditing.strategy", havingValue = "rest")
public class AuditableRestStrategyService implements AuditableService
{
  private static final Logger LOG = LoggerFactory.getLogger(AuditableRestStrategyService.class);

  private final Auditor auditor;
  private final RestTemplate restTemplate;

  @Autowired
  public AuditableRestStrategyService(
      @Value("${auditing.server}") final String auditingServer,
      final ObjectMapper objectMapper)
  {
    restTemplate = new RestTemplate();
    restTemplate.setErrorHandler(new AuditExceptionHandler());

    final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
        1,
        15,
        1L, TimeUnit.MINUTES,
        new LinkedBlockingQueue<>(1000),
        new CustomizableThreadFactory("audit-"));

    final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(
        10,
        new CustomizableThreadFactory("sch-audit-"));

    final List<Pair<String, String>> headers = Lists.newArrayList(Pair.of(
        "Content-Type",
        MediaType.APPLICATION_JSON_UTF8_VALUE));

    auditor = new Auditor(
        "auditQueue",
        objectMapper,
        threadPoolExecutor,
        scheduledThreadPoolExecutor,
        new RestActionExecutor(restTemplate, auditingServer, headers),
        throwable -> LOG.error(throwable.getMessage()),
        30,
        10000L,
        8,
        1000L,
        60000L,
        1);
  }

  @Override
  public void logAuditData(final AuditDto auditDto)
  {
    CompletableFuture.runAsync(() -> auditor.push(auditDto));
  }

  @PreDestroy
  public void destroy()
  {
    auditor.close();
  }

  private static class AuditExceptionHandler implements ResponseErrorHandler
  {
    @Override
    public boolean hasError(final ClientHttpResponse response) throws IOException
    {
      return response.getStatusCode().series() == CLIENT_ERROR
          || response.getStatusCode().series() == SERVER_ERROR
          || response.getStatusCode().series() == REDIRECTION;
    }

    @Override
    public void handleError(final ClientHttpResponse response) throws IOException
    {
      final String stackTrace = new String(FileCopyUtils.copyToByteArray(response.getBody()));
      throw new HttpClientErrorException(response.getStatusCode(), stackTrace);
    }
  }
}
