package com.marand.thinkmed.medications.ehr.model.pharmacist;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;

/**
 * @author Vid Kumse
 */
public class Miscellaneous
{
  @EhrMapped("items[openEHR-EHR-ACTION.medication_supply_uk.v1]")
  private MedicationSupply inpatientMedicationSupply;

  @EhrMapped("items[openEHR-EHR-INSTRUCTION.request-referral.v1,'Precriber referral']")
  private RequestReferral prescriberReferral;

  @EhrMapped("items[openEHR-EHR-EVALUATION.recommendation_response.v1,'Prescriber referral response']")
  private RecommendationResponse prescriberReferralResponse;

  @EhrMapped("items[openEHR-EHR-INSTRUCTION.medication_order.v2]")
  private MedicationOrder medicationorder;

  @EhrMapped("items[openEHR-EHR-ACTION.medication.v1]")
  private MedicationManagement medicationManagement;

  public MedicationSupply getInpatientMedicationSupply()
  {
    return inpatientMedicationSupply;
  }

  public void setInpatientMedicationSupply(final MedicationSupply inpatientMedicationSupply)
  {
    this.inpatientMedicationSupply = inpatientMedicationSupply;
  }

  public RequestReferral getPrescriberReferral()
  {
    return prescriberReferral;
  }

  public void setPrescriberReferral(final RequestReferral prescriberReferral)
  {
    this.prescriberReferral = prescriberReferral;
  }

  public RecommendationResponse getPrescriberReferralResponse()
  {
    return prescriberReferralResponse;
  }

  public void setPrescriberReferralResponse(final RecommendationResponse prescriberReferralResponse)
  {
    this.prescriberReferralResponse = prescriberReferralResponse;
  }

  public MedicationOrder getMedicationorder()
  {
    return medicationorder;
  }

  public void setMedicationorder(final MedicationOrder medicationorder)
  {
    this.medicationorder = medicationorder;
  }

  public MedicationManagement getMedicationManagement()
  {
    return medicationManagement;
  }

  public void setMedicationManagement(final MedicationManagement medicationManagement)
  {
    this.medicationManagement = medicationManagement;
  }
}
