package com.marand.thinkmed.request.time;

import org.joda.time.DateTime;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.annotation.RequestScope;

/**
 * @author Nejc Korasa
 */

@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestScopeTime
{
  private final DateTime dateTime = new DateTime();

  public DateTime getRequestTimestamp()
  {
    return dateTime;
  }
}
