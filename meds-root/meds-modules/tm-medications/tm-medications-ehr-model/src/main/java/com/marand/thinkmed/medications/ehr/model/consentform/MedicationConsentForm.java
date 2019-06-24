package com.marand.thinkmed.medications.ehr.model.consentform;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;

/**
 * @author Vid Kumse
 */
public class MedicationConsentForm extends EhrComposition
{
  @EhrMapped("content[openEHR-EHR-ADMIN_ENTRY.medication_consent.v0]")
  private MedicationConsent medicationConsent;

  @EhrMapped("content[openEHR-EHR-SECTION.ispek_dialog.v1,'Medication list']")
  private MedicationList medicationList;

  @Override
  public String getTemplateId()
  {
    return "OPENeP - Consent Form";
  }

  public MedicationConsent getMedicationConsent()
  {
    if (medicationConsent == null)
    {
      medicationConsent = new MedicationConsent();
    }
    return medicationConsent;
  }

  public void setMedicationConsent(final MedicationConsent medicationConsent)
  {
    this.medicationConsent = medicationConsent;
  }

  public MedicationList getMedicationList()
  {
    return medicationList;
  }

  public void setMedicationList(final MedicationList medicationList)
  {
    this.medicationList = medicationList;
  }
}
