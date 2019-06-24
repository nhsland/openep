package com.marand.thinkmed.medications;

/**
 * @author Mitja Lapajne
 */
public enum TherapyTemplateContextEnum
{
  INPATIENT(TherapyTemplateModeEnum.INPATIENT, TherapyAuthorityEnum.MEDS_MANAGE_INPATIENT_PRESCRIPTIONS),
  OUTPATIENT(TherapyTemplateModeEnum.OUTPATIENT, TherapyAuthorityEnum.MEDS_MANAGE_OUTPATIENT_PRESCRIPTIONS),
  ADMISSION(TherapyTemplateModeEnum.INPATIENT, TherapyAuthorityEnum.MEDS_MANAGE_MEDICATION_ON_ADMISSION),
  DISCHARGE(TherapyTemplateModeEnum.OUTPATIENT, TherapyAuthorityEnum.MEDS_MANAGE_MEDICATION_ON_DISCHARGE);

  private final TherapyTemplateModeEnum therapyTemplateMode;
  private final TherapyAuthorityEnum therapyAuthorityEnum;

  TherapyTemplateContextEnum(
      final TherapyTemplateModeEnum therapyTemplateMode,
      final TherapyAuthorityEnum therapyAuthorityEnum)
  {
    this.therapyTemplateMode = therapyTemplateMode;
    this.therapyAuthorityEnum = therapyAuthorityEnum;
  }

  public TherapyTemplateModeEnum getTherapyTemplateMode()
  {
    return therapyTemplateMode;
  }

  public TherapyAuthorityEnum getTherapyAuthorityEnum()
  {
    return therapyAuthorityEnum;
  }
}
