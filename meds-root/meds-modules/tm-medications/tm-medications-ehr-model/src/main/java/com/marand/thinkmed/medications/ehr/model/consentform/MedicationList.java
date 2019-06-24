package com.marand.thinkmed.medications.ehr.model.consentform;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;

/**
 * @author Vid Kumse
 */
public class MedicationList
{
  @EhrMapped("items[openEHR-EHR-ADMIN_ENTRY.medication_consent_item.v0]")
  private List<MedicationConsentItem> medicationConsentItem = new ArrayList<>();

  public List<MedicationConsentItem> getMedicationConsentItem()
  {
    return medicationConsentItem;
  }

  public void setMedicationConsentItem(final List<MedicationConsentItem> medicationConsentItem)
  {
    this.medicationConsentItem = medicationConsentItem;
  }
}
