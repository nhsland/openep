package com.marand.thinkmed.fdb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Boris Marn.
 */
@Component
@ConfigurationProperties(prefix = "fdb")
public class FdbProperties
{
  private String restUri;

  public String getRestUri()
  {
    return restUri;
  }

  public void setRestUri(final String restUri)
  {
    this.restUri = restUri;
  }
}
