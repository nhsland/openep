package com.marand.auditing.auditing;

import java.lang.reflect.Parameter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.servlet.http.HttpServletRequest;

import care.better.auditing.AuditDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.marand.auditing.service.AuditableService;
import com.marand.thinkmed.request.user.RequestUser;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Primoz Delopst
 */

@Aspect
@Component
public class AuditableAspect
{
  private final Tracer tracer;
  private final AuditableService auditableService;
  private final String applicationName;
  private final boolean ignoreOutput;
  private final ObjectMapper objectMapper;

  @Autowired
  public AuditableAspect(
      final Tracer tracer,
      final AuditableService auditableService,
      @Value("${spring.application.name}") final String applicationName,
      @Value("${auditing.ignore.output:false}") final boolean ignoreOutput,
      final ObjectMapper objectMapper)
  {
    this.tracer = tracer;
    this.auditableService = auditableService;
    this.applicationName = applicationName;
    this.ignoreOutput = ignoreOutput;
    this.objectMapper = objectMapper;
  }

  @Pointcut("within(@org.springframework.stereotype.Controller *)")
  private void controller() { }

  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
  private void restController() { }

  @AfterReturning(value = "@annotation(auditable) && (restController() || controller())", returning = "result")
  public Object logActivity(final JoinPoint joinPoint, final Object result, final Auditable auditable) throws Throwable
  {
    final Span span = tracer.getCurrentSpan();

    final Set<AuditableType> auditableTypes = ImmutableSet.copyOf(auditable.value());
    final MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
    final Map<String, String> methodArguments = extractMethodArguments(auditableTypes, joinPoint, methodSignature);
    final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    final String methodResult = getMethodResult(result, auditableTypes);

    auditableService.logAuditData(buildAuditDto(span, methodSignature, methodArguments, methodResult, null, request));

    return result;
  }

  @Nullable
  public String getMethodResult(final Object result, final Set<AuditableType> auditableTypes) throws JsonProcessingException
  {
    if (auditableTypes.contains(AuditableType.WITHOUT_RESULT) || ignoreOutput)
    {
      return null;
    }
    if (result instanceof String)
    {
      return (String)result;
    }
    return objectMapper.writeValueAsString(result);
  }

  @AfterThrowing(pointcut = "@annotation(auditable) && (restController() || controller())", throwing = "exception")
  public void logExceptions(final JoinPoint joinPoint, final Auditable auditable, final Throwable exception) throws Throwable
  {
    final Span span = tracer.getCurrentSpan();

    final Set<AuditableType> auditableTypes = ImmutableSet.copyOf(auditable.value());
    final MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
    final HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
    final Map<String, String> methodArguments = extractMethodArguments(auditableTypes, joinPoint, methodSignature);

    auditableService.logAuditData(buildAuditDto(span, methodSignature, methodArguments, null, exception, request));
  }

  AuditDto buildAuditDto(
      final Span span,
      final MethodSignature methodSignature,
      final Map<String, String> methodArguments,
      final String methodResult,
      final Throwable throwable,
      final HttpServletRequest request)
  {
    final String patientId = methodArguments.get("patientId");
    final String ehrId = methodArguments.get("ehrId");

    return new AuditDto.Builder(applicationName,
                                ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()),
                                methodSignature.getMethod().getName(),
                                methodArguments,
                                RequestUser.getUser().getUsername(),
                                request != null ? request.getRemoteAddr() : null)
        .withUserId(RequestUser.getUser().getId())
        .withResponse(methodResult)
        .withErrorMessage(throwable != null ? Throwables.getStackTraceAsString(throwable) : null)
        .withDuration(span.getAccumulatedMicros() / 1000)
        .withUserFullName(RequestUser.getUser().getFullName())
        .withRequestId(String.valueOf(span.getSpanId()))
        .withPatientId(patientId)
        .withEhrId(ehrId)
        .build();
  }

  private Map<String, String> extractMethodArguments(
      final Set<AuditableType> auditableTypes,
      final JoinPoint joinPoint,
      final MethodSignature methodSignature)
  {
    if (auditableTypes.contains(AuditableType.WITHOUT_PARAMETERS))
    {
      return Collections.emptyMap();
    }

    final List<String> argumentNames = ImmutableList.copyOf(methodSignature.getParameterNames());
    final List<String> argumentValues = Arrays
        .stream(joinPoint.getArgs())
        .map(a -> a != null ? a.toString() : "null")
        .collect(Collectors.toList());

    if (auditableTypes.contains(AuditableType.FULL))
    {
      return IntStream
          .range(0, argumentNames.size())
          .boxed()
          .collect(Collectors.toMap(argumentNames::get, argumentValues::get));
    }

    return getAuditParameters(argumentNames, argumentValues, methodSignature);
  }

  private Map<String, String> getAuditParameters(
      final List<String> names,
      final List<String> argTypes,
      final MethodSignature methodSignature)
  {
    final List<Parameter> parameters = ImmutableList.copyOf(methodSignature.getMethod().getParameters());

    final boolean auditParamsExist = parameters.stream().anyMatch(this::hasAuditAnnotation);

    return IntStream
        .range(0, names.size())
        .boxed()
        .filter(i -> !hasNotAuditAnnotation(parameters.get(i)))
        .filter(i -> !auditParamsExist || hasAuditAnnotation(parameters.get(i)))
        .collect(Collectors.toMap(names::get, argTypes::get));
  }

  private boolean hasAuditAnnotation(final Parameter parameter)
  {
    return Arrays.stream(parameter.getDeclaredAnnotations()).anyMatch(Audit.class::isInstance);
  }

  private boolean hasNotAuditAnnotation(final Parameter parameter)
  {
    return Arrays.stream(parameter.getDeclaredAnnotations()).anyMatch(NoAudit.class::isInstance);
  }
}