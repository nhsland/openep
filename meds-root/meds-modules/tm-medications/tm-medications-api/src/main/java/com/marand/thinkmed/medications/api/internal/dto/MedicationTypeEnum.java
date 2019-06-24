package com.marand.thinkmed.medications.api.internal.dto;

/**
 * @author Mitja Lapajne
 */

public enum MedicationTypeEnum
{
  MEDICATION,
  DILUENT,
  BLOOD_PRODUCT,
  MEDICINAL_GAS,

  //TODO remove once applied to all databases
  SOLUTION;

  public static MedicationRole toMedicationRole(final MedicationTypeEnum medicationType)
  {
    if (medicationType == MEDICATION)
    {
      return MedicationRole.THERAPEUTIC;
    }
    return MedicationRole.EXCIPIENT;
  }

  public static MedicationTypeEnum fromMedicationRole(final MedicationRole medicationRole)
  {
    if (medicationRole == null)
    {
      return null;
    }
    if (medicationRole == MedicationRole.THERAPEUTIC)
    {
      return MEDICATION;
    }
    return DILUENT;
  }
}
