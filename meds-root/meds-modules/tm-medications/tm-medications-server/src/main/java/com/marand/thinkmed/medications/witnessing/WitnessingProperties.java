package com.marand.thinkmed.medications.witnessing;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Nejc Korasa
 */

@ConfigurationProperties(prefix = "witnessing")
public class WitnessingProperties
{
  public static final String WITNESSING_ENABLED_CLIENT_PROPERTY = "administrationWitnessingEnabled";
  public static final String WITNESSING_MOCKED_CLIENT_PROPERTY = "administrationWitnessingMocked";
  public static final String WITNESSING_IV_REQUIRED_CLIENT_PROPERTY = "administrationWitnessingIvRequired";

  private boolean enabled = false;
  private boolean ivRequired = false;
  private boolean mocked = false;
  private Integer ageLimit = null;

  public boolean isEnabled()
  {
    return enabled;
  }

  public void setEnabled(final boolean enabled)
  {
    this.enabled = enabled;
  }

  public boolean isIvRequired()
  {
    return ivRequired;
  }

  public void setIvRequired(final boolean ivRequired)
  {
    this.ivRequired = ivRequired;
  }

  public Integer getAgeLimit()
  {
    return ageLimit;
  }

  public void setAgeLimit(final Integer ageLimit)
  {
    this.ageLimit = ageLimit;
  }

  public boolean isMocked()
  {
    return mocked;
  }

  public void setMocked(final boolean mocked)
  {
    this.mocked = mocked;
  }
}
