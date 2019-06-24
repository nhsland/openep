package com.marand.thinkmed.medispan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Vid Kumse
 */
@Component
@ConfigurationProperties(prefix="medispan")
public class MedispanProperties
{
  private boolean preloadCache;

  public boolean isPreloadCache()
  {
    return preloadCache;
  }

  public void setPreloadCache(final boolean preloadCache)
  {
    this.preloadCache = preloadCache;
  }
}
