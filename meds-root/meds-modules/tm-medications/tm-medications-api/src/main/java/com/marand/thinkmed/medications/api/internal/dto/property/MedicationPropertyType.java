package com.marand.thinkmed.medications.api.internal.dto.property;

/**
 * @author Nejc Korasa
 */

public enum MedicationPropertyType
{
  // NO VALUE PROPERTIES

  SUGGEST_SWITCH_TO_ORAL(true, false, false),
  REVIEW_REMINDER(true, false, false),
  CONTROLLED_DRUG(true, false, false),
  CRITICAL_DRUG(true, false, false),
  MENTAL_HEALTH_DRUG(true, false, false),
  EXPENSIVE_DRUG(true, false, false),
  HIGH_ALERT_MEDICATION(true, false, false),
  BLACK_TRIANGLE_MEDICATION(true, false, false),
  CLINICAL_TRIAL_MEDICATION(true, false, false),
  UNLICENSED_MEDICATION(false, false, false),
  NOT_FOR_PRN(true, false, false),
  ANTIBIOTIC(true, false, false),
  ANTICOAGULANT(true, false, false),
  INSULIN(true, false, false),
  FLUID(true, false, false),
  IGNORE_DUPLICATION_WARNINGS(true, false, false),
  ANTIPSYCHOTIC_TAG(true, false, false),
  WITNESSING(true, false, false),

  // VALUE PROPERTIES

  PRICE(true, false, false),
  ADD_INFO_INPATIENT(false, false, false),
  ADD_INFO_OUTPATIENT(false, false, false),
  TRADE_FAMILY(false, false, false),

  MODIFIED_RELEASE_TIME(true, true, true),
  MODIFIED_RELEASE(true, true, false),
  GASTRO_RESISTANT(true, true, false);

  private final boolean vmpToVtm; // copy VMP property to it's VTM
  private final boolean ampToVtm; // copy AMP property to it's VTM
  private final boolean ampToVmp; // copy AMP property to it's VMP

  MedicationPropertyType(final boolean vmpToVtm, final boolean ampToVtm, final boolean ampToVmp)
  {
    this.vmpToVtm = vmpToVtm;
    this.ampToVtm = ampToVtm;
    this.ampToVmp = ampToVmp;
  }

  public boolean isVmpToVtm()
  {
    return vmpToVtm;
  }

  public boolean isAmpToVtm()
  {
    return ampToVtm;
  }

  public boolean isAmpToVmp()
  {
    return ampToVmp;
  }

  // returns null if MedicationPropertyType does not contain value with name
  public static MedicationPropertyType valueOfOrNull(final String name)
  {
    if (name == null)
    {
      return null;
    }
    try
    {
      return valueOf(name);
    }
    catch (final IllegalArgumentException e)
    {
      return null;
    }
  }
}
