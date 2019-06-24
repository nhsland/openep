package com.marand.thinkmed.medications.connector.impl.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Boris Marn
 */
@ConfigurationProperties(prefix = "tc")
public class TcProperties
{
  private String url;

  public String getUrl()
  {
    return url;
  }

  public void setUrl(final String url)
  {
    this.url = url;
  }
}
