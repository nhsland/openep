package com.marand.thinkmed.medications.ehr.model.pharmacist;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Vid Kumse
 */
public class PharmacistMedicationReview
{
  @EhrMapped("data[at0001]/items[at0052]/value")
  private DvCodedText medicationListCategory;

  @EhrMapped("data[at0001]/items[at0002]")
  private MedicationItemAssessment medicationItemAssessment;

  public DvCodedText getMedicationListCategory()
  {
    return medicationListCategory;
  }

  public void setMedicationListCategory(final DvCodedText medicationListCategory)
  {
    this.medicationListCategory = medicationListCategory;
  }

  public MedicationItemAssessment getMedicationItemAssessment()
  {
    if (medicationItemAssessment == null)
    {
      medicationItemAssessment = new MedicationItemAssessment();
    }

    return medicationItemAssessment;
  }

  public void setMedicationItemAssessment(final MedicationItemAssessment medicationItemAssessment)
  {
    this.medicationItemAssessment = medicationItemAssessment;
  }
}
