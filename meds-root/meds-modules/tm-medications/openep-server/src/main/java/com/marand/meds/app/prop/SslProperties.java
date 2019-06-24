package com.marand.meds.app.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Mitja Lapajne
 */
@ConfigurationProperties(prefix = "ssl")
public class SslProperties
{
  //keystore properties are already part of Spring Boot

  private String trustStore;
  private String trustStorePassword;

  public String getTrustStore()
  {
    return trustStore;
  }

  public void setTrustStore(final String trustStore)
  {
    this.trustStore = trustStore;
  }

  public String getTrustStorePassword()
  {
    return trustStorePassword;
  }

  public void setTrustStorePassword(final String trustStorePassword)
  {
    this.trustStorePassword = trustStorePassword;
  }
}
