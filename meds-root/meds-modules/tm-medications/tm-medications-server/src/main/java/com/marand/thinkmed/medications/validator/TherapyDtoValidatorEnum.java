package com.marand.thinkmed.medications.validator;

public enum TherapyDtoValidatorEnum
{
  MEDICATION_IS_MISSING("Medication is missing"),
  DOSE_ELEMENT_IS_MISSING("Dose element is missing"),
  DOSE_ELEMENT_AND_TITRATION_EXISTS("Dose element and titration exists"),
  ROUTE_IS_MISSING("Route is missing"),
  DOSING_INTERVAL_IS_MISSING("Dosing interval is missing"),
  INDICATION_IS_MISSING("Indication is missing"),
  COMMENT_IS_MISSING("Comment is missing"),
  REVIEW_REMINDER_DAYS_IS_MISSING("Review reminder days is missing"),
  TARGET_SATURATION_IS_MISSING("Target saturation is missing");

  private final String text;

  TherapyDtoValidatorEnum(final String text)
  {
    this.text = text;
  }

  public String getText()
  {
    return text;
  }
}
