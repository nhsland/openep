package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("InstanceVariableNamingConvention")
public class FdbWarningDto implements JsonSerializable
{
  private String FullAlertMessage;
  private FdbNameValue AlertSeverity;
  private FdbDrug PrimaryDrug;
  private FdbDrug SecondaryDrug;

  public String getFullAlertMessage()
  {
    return FullAlertMessage;
  }

  public void setFullAlertMessage(final String fullAlertMessage)
  {
    FullAlertMessage = fullAlertMessage;
  }

  public FdbNameValue getAlertSeverity()
  {
    return AlertSeverity;
  }

  public void setAlertSeverity(final FdbNameValue alertSeverity)
  {
    AlertSeverity = alertSeverity;
  }

  public FdbDrug getPrimaryDrug()
  {
    return PrimaryDrug;
  }

  public void setPrimaryDrug(final FdbDrug primaryDrug)
  {
    PrimaryDrug = primaryDrug;
  }

  public FdbDrug getSecondaryDrug()
  {
    return SecondaryDrug;
  }

  public void setSecondaryDrug(final FdbDrug secondaryDrug)
  {
    SecondaryDrug = secondaryDrug;
  }
}
