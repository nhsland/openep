package com.marand.meds.app.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Mitja Lapajne
 */
@ConfigurationProperties(prefix = "openep")
public class OpenepProperties
{
  private String locale;
  private String country;

  public String getLocale()
  {
    return locale;
  }

  public void setLocale(final String locale)
  {
    this.locale = locale;
  }

  public String getCountry()
  {
    return country;
  }

  public void setCountry(final String country)
  {
    this.country = country;
  }
}
