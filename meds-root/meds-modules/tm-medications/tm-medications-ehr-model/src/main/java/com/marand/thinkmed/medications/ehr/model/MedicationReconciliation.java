
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;

/**
 * @author Nejc Korasa
 */

public class MedicationReconciliation extends EhrComposition
{
  @EhrMapped("content[openEHR-EHR-INSTRUCTION.medication_reconciliation_additional_information.v0]")
  private MedicationReconciliationDetails reconciliationDetails;

  @Override
  public String getTemplateId()
  {
    return "OPENeP - Medication Reconciliation";
  }

  public MedicationReconciliationDetails getReconciliationDetails()
  {
    if (reconciliationDetails == null)
    {
      reconciliationDetails = new MedicationReconciliationDetails();
    }
    return reconciliationDetails;
  }

  public void setReconciliationDetails(final MedicationReconciliationDetails reconciliationDetails)
  {
    this.reconciliationDetails = reconciliationDetails;
  }
}
