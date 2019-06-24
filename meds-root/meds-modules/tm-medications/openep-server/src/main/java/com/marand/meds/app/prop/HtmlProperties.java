package com.marand.meds.app.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Boris Marn.
 */
@ConfigurationProperties(prefix = "html")
public class HtmlProperties
{
  private boolean developmentMode;
  private String developmentUser;
  private boolean testMode;
  private Integer resourceCacheMaxAge;

  public boolean isDevelopmentMode()
  {
    return developmentMode;
  }

  public void setDevelopmentMode(final boolean developmentMode)
  {
    this.developmentMode = developmentMode;
  }

  public String getDevelopmentUser()
  {
    return developmentUser;
  }

  public void setDevelopmentUser(final String developmentUser)
  {
    this.developmentUser = developmentUser;
  }

  public boolean isTestMode()
  {
    return testMode;
  }

  public void setTestMode(final boolean testMode)
  {
    this.testMode = testMode;
  }

  public Integer getResourceCacheMaxAge()
  {
    return resourceCacheMaxAge;
  }

  public void setResourceCacheMaxAge(final Integer resourceCacheMaxAge)
  {
    this.resourceCacheMaxAge = resourceCacheMaxAge;
  }

}
