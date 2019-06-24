package com.marand.thinkmed.request.time;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class RequestDateTimeHolder
{
  private static final ThreadLocal<DateTime> THREAD_TIME = ThreadLocal.withInitial(DateTime::new);

  private final RequestScopeTime requestScopeTime;

  @Autowired
  public RequestDateTimeHolder(final RequestScopeTime requestScopeTime)
  {
    this.requestScopeTime = requestScopeTime;
  }

  public DateTime getRequestTimestamp()
  {
    try
    {
      return requestScopeTime.getRequestTimestamp();
    }
    catch (final RuntimeException ignored)
    {
      return THREAD_TIME.get();
    }
  }
}
