package com.marand.thinkmed.medications.hl7;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Mitja Lapajne
 */
@ConfigurationProperties(prefix = "hl7")
public class Hl7Properties
{
  private boolean listenerEnabled;
  private Integer port;

  public boolean isListenerEnabled()
  {
    return listenerEnabled;
  }

  public void setListenerEnabled(final boolean listenerEnabled)
  {
    this.listenerEnabled = listenerEnabled;
  }

  public Integer getPort()
  {
    return port;
  }

  public void setPort(final Integer port)
  {
    this.port = port;
  }
}
