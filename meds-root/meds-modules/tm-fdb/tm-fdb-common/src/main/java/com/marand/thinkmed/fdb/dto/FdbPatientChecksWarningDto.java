package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
public class FdbPatientChecksWarningDto implements JsonSerializable
{
  private String FullAlertMessage;
  private FdbDrug Drug;
  private FdbNameValue ConditionAlertSeverity;
  private FdbNameValue AlertRelevanceType;
  private FdbPatientCheckTriggersDto PatientCheckTriggers;

  public FdbNameValue getAlertRelevanceType()
  {
    return AlertRelevanceType;
  }

  public void setAlertRelevanceType(final FdbNameValue alertRelevanceType)
  {
    AlertRelevanceType = alertRelevanceType;
  }

  public FdbNameValue getConditionAlertSeverity()
  {
    return ConditionAlertSeverity;
  }

  public void setConditionAlertSeverity(final FdbNameValue conditionAlertSeverity)
  {
    ConditionAlertSeverity = conditionAlertSeverity;
  }

  public String getFullAlertMessage()
  {
    return FullAlertMessage;
  }

  public void setFullAlertMessage(final String fullAlertMessage)
  {
    FullAlertMessage = fullAlertMessage;
  }

  public FdbDrug getDrug()
  {
    return Drug;
  }

  public void setDrug(final FdbDrug drug)
  {
    Drug = drug;
  }

  public FdbPatientCheckTriggersDto getPatientCheckTriggers()
  {
    return PatientCheckTriggers;
  }

  public void setPatientCheckTriggers(final FdbPatientCheckTriggersDto patientCheckTriggers)
  {
    PatientCheckTriggers = patientCheckTriggers;
  }
}
