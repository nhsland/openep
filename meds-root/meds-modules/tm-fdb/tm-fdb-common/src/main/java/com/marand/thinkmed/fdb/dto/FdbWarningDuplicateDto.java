package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Vid Kumse
 */
@SuppressWarnings("InstanceVariableNamingConvention")
public class FdbWarningDuplicateDto implements JsonSerializable
{
  private String FullAlertMessage;
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
