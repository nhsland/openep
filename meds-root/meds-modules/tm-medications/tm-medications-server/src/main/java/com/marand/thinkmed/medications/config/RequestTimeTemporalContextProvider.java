package com.marand.thinkmed.medications.config;

import com.marand.maf.core.server.entity.context.TemporalContext;
import com.marand.maf.core.server.entity.context.TemporalContextProvider;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class RequestTimeTemporalContextProvider implements TemporalContextProvider
{
  private final RequestDateTimeHolder requestDateTimeHolder;

  @Autowired
  public RequestTimeTemporalContextProvider(final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  @Override
  public TemporalContext getTemporalContext(final Class type)
  {
    return new TemporalContext(requestDateTimeHolder.getRequestTimestamp());
  }
}
