package com.marand.thinkmed.medications.ehr.model.pharmacist;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;

/**
 * @author Vid Kumse
 */
public class PharmacyReviewReport extends EhrComposition
{
  @EhrMapped("content[openEHR-EHR-EVALUATION.pharmacy_meds_review.v1]")
  private PharmacistMedicationReview pharmacistMedicationReview;

  @EhrMapped("content[openEHR-EHR-SECTION.adhoc.v1,'Miscellaneous']")
  private Miscellaneous miscellaneous;

  @Override
  public String getTemplateId()
  {
    return "OPENeP - Pharmacy Review Report";
  }

  public static String getMedicationOrderPath()
  {
    return "content[openEHR-EHR-SECTION.adhoc.v1]/items[openEHR-EHR-INSTRUCTION.medication_order.v2]";
  }

  public PharmacistMedicationReview getPharmacistMedicationReview()
  {
    return pharmacistMedicationReview;
  }

  public PharmacistMedicationReview getOrCreatePharmacistMedicationReview()
  {
    if (pharmacistMedicationReview == null)
    {
      pharmacistMedicationReview = new PharmacistMedicationReview();
    }

    return pharmacistMedicationReview;
  }

  public void setPharmacistMedicationReview(final PharmacistMedicationReview pharmacistMedicationReview)
  {
    this.pharmacistMedicationReview = pharmacistMedicationReview;
  }

  public Miscellaneous getMiscellaneous()
  {
    if (miscellaneous == null)
    {
      miscellaneous = new Miscellaneous();
    }

    return miscellaneous;
  }

  public void setMiscellaneous(final Miscellaneous miscellaneous)
  {
    this.miscellaneous = miscellaneous;
  }
}
