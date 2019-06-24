
package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public class OutpatientPrescription extends EhrComposition
{
  @EhrMapped("content[openEHR-EHR-INSTRUCTION.medication_order.v2]")
  private List<MedicationOrder> medicationOrder = new ArrayList<>();

  @EhrMapped("content[openEHR-EHR-ACTION.medication.v1]")
  private List<MedicationManagement> actions = new ArrayList<>();

  @Override
  public String getTemplateId()
  {
    return "OPENeP - Outpatient Prescription";
  }

  public static String getMedicationOrderPath()
  {
    return "content[openEHR-EHR-INSTRUCTION.medication_order.v2]";
  }

  public List<MedicationOrder> getMedicationOrder()
  {
    return medicationOrder;
  }

  public void setMedicationOrder(final List<MedicationOrder> medicationOrder)
  {
    this.medicationOrder = medicationOrder;
  }

  public List<MedicationManagement> getActions()
  {
    return actions;
  }

  public void setActions(final List<MedicationManagement> actions)
  {
    this.actions = actions;
  }
}
