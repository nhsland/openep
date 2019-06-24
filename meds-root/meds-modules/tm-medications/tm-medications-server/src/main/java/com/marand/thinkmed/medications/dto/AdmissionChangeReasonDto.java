package com.marand.thinkmed.medications.dto;

import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */

public class AdmissionChangeReasonDto
{
  private TherapyChangeReasonDto changeReason;
  private DateTime time;
  private MedicationActionEnum actionEnum;

  public AdmissionChangeReasonDto(
      final TherapyChangeReasonDto changeReason,
      final DateTime time,
      final MedicationActionEnum actionEnum)
  {
    this.changeReason = changeReason;
    this.time = time;
    this.actionEnum = actionEnum;
  }

  public TherapyChangeReasonDto getChangeReason()
  {
    return changeReason;
  }

  public void setChangeReason(final TherapyChangeReasonDto changeReason)
  {
    this.changeReason = changeReason;
  }

  public DateTime getTime()
  {
    return time;
  }

  public void setTime(final DateTime time)
  {
    this.time = time;
  }

  public MedicationActionEnum getActionEnum()
  {
    return actionEnum;
  }

  public void setActionEnum(final MedicationActionEnum actionEnum)
  {
    this.actionEnum = actionEnum;
  }
}
