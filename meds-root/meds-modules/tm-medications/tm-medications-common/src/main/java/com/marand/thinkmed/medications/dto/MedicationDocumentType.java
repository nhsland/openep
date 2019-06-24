package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.medications.MedicationExternalSystemType;

/**
 * @author Mitja Lapajne
 */
public enum MedicationDocumentType
{
  PDF,
  URL;

  public static MedicationDocumentType fromMedicationExternalSystemType(final MedicationExternalSystemType medicationExternalSystemType)
  {
    if (medicationExternalSystemType == MedicationExternalSystemType.DOCUMENTS_PROVIDER)
    {
      return PDF;
    }
    if (medicationExternalSystemType == MedicationExternalSystemType.URL)
    {
      return URL;
    }
    return null;
  }
}
