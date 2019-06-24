package com.marand.meds.rest;

import java.lang.reflect.UndeclaredThrowableException;
import javax.persistence.OptimisticLockException;

import com.google.common.base.Throwables;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.exception.ApplicationException;
import com.marand.maf.core.exception.SystemException;
import com.marand.maf.core.exception.TmRestException;
import com.marand.maf.core.exception.UserException;
import com.marand.maf.core.exception.UserWarning;
import com.marand.thinkehr.exception.ConcurrentUpdateException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static com.marand.maf.core.exception.TmRestExceptionType.EXCEPTION;
import static com.marand.maf.core.exception.TmRestExceptionType.WARNING;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Mitja Lapajne
 */

@RestControllerAdvice
@RequestMapping(produces = APPLICATION_JSON_VALUE)
public class ExceptionsHandler extends ResponseEntityExceptionHandler
{
  // TC exceptions

  @ExceptionHandler(value = {UserException.class, ApplicationException.class, SystemException.class})
  public ResponseEntity<Object> handle(final Exception ex, final WebRequest request)
  {
    logger.error(Throwables.getStackTraceAsString(ex));

    final TmRestException exc = new TmRestException(
        ex.getLocalizedMessage(),
        ExceptionUtils.getStackTrace(ex),
        ex.getClass().toString(),
        EXCEPTION);

    return handleExceptionInternal(ex, exc, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  @ExceptionHandler(value = {UserWarning.class})
  protected ResponseEntity<Object> handle(final UserWarning ex, final WebRequest request)
  {
    logger.error(Throwables.getStackTraceAsString(ex));

    final TmRestException exc = new TmRestException(
        Dictionary.getEntry(ex.getErrorMessageOrKey()),
        ExceptionUtils.getStackTrace(ex),
        ex.getClass().toString(),
        WARNING);

    return handleExceptionInternal(ex, exc, new HttpHeaders(), getStatusCodeForReason(ex.getReason()), request);
  }

  // ALL OTHERS

  @ExceptionHandler(value = {Exception.class})
  protected ResponseEntity<Object> handleOthers(final Exception ex, final WebRequest request)
  {
    logger.error(Throwables.getStackTraceAsString(ex));

    final TmRestException exc = new TmRestException(
        ex.getLocalizedMessage(),
        ExceptionUtils.getStackTrace(ex),
        ex.getClass().toString(),
        EXCEPTION);

    return handleExceptionInternal(ex, exc, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  @ExceptionHandler(value = {UndeclaredThrowableException.class})
  public ResponseEntity<Object> handleUndeclaredThrowable(final UndeclaredThrowableException ex, final WebRequest request)
  {
    logger.error(Throwables.getStackTraceAsString(ex));

    final Throwable undeclaredThrowable = ex.getUndeclaredThrowable();

    if (undeclaredThrowable instanceof ConcurrentUpdateException || undeclaredThrowable instanceof ActivitiOptimisticLockingException)
    {
      return handleOptimisticLocking(ex, request);
    }

    final TmRestException exc = new TmRestException(
        undeclaredThrowable.getLocalizedMessage(),
        ExceptionUtils.getStackTrace(undeclaredThrowable),
        undeclaredThrowable.getClass().toString(),
        EXCEPTION);

    return handleExceptionInternal(ex, exc, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  @ExceptionHandler(value = {ActivitiOptimisticLockingException.class, OptimisticLockException.class, ConcurrentUpdateException.class})
  public ResponseEntity<Object> handleOptimisticLock(
      final ActivitiOptimisticLockingException ex,
      final WebRequest request)
  {
    logger.error(Throwables.getStackTraceAsString(ex));
    return handleOptimisticLocking(ex, request);
  }

  private ResponseEntity<Object> handleOptimisticLocking(final Exception ex, final WebRequest request)
  {
    final TmRestException exc = new TmRestException(
        Dictionary.getEntry("data.changed.please.reload"),
        ExceptionUtils.getStackTrace(ex),
        ex.getClass().toString(),
        WARNING);

    return handleExceptionInternal(ex, exc, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  private HttpStatus getStatusCodeForReason(final UserWarning.Reason reason)
  {
    if (reason == UserWarning.Reason.STALE_OBJECT)
    {
      return HttpStatus.CONFLICT;
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  // REST exceptions

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      final MissingServletRequestParameterException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request)
  {
    return buildResponse(ex, headers, status);
  }

  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      final HttpRequestMethodNotSupportedException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request)
  {
    return buildResponse(ex, headers, status);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
      final HttpMediaTypeNotSupportedException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request)
  {
    return buildResponse(ex, headers, status);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
      final HttpMediaTypeNotAcceptableException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request)
  {
    return buildResponse(ex, headers, status);
  }

  @Override
  protected ResponseEntity<Object> handleMissingPathVariable(
      final MissingPathVariableException ex, 
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request)
  {
    return buildResponse(ex, headers, status);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      final MethodArgumentNotValidException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request)
  {
    return buildResponse(ex, headers, status);
  }

  private ResponseEntity<Object> buildResponse(final Exception ex, final HttpHeaders headers, final HttpStatus status)
  {
    return new ResponseEntity<>(ex.getMessage(), headers, status);
  }
}
