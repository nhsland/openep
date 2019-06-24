package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
public class FdbDrugSensitivityWarningDto implements JsonSerializable
{
  private String FullAlertMessage;
  private FdbDrug Allergen;
  private FdbDrug Drug;

  public String getFullAlertMessage()
  {
    return FullAlertMessage;
  }

  public void setFullAlertMessage(final String fullAlertMessage)
  {
    FullAlertMessage = fullAlertMessage;
  }

  public FdbDrug getAllergen()
  {
    return Allergen;
  }

  public void setAllergen(final FdbDrug allergen)
  {
    Allergen = allergen;
  }

  public FdbDrug getDrug()
  {
    return Drug;
  }

  public void setDrug(final FdbDrug drug)
  {
    Drug = drug;
  }
}
