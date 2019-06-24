
package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public class InpatientPrescription extends EhrComposition
{
  @EhrMapped("content[openEHR-EHR-INSTRUCTION.medication_order.v2]")  // if changes, also change getMedicationOrderPath()
  private MedicationOrder medicationOrder;

  @EhrMapped("content[openEHR-EHR-ACTION.medication.v1]")
  private List<MedicationManagement> actions = new ArrayList<>();

  public MedicationOrder getMedicationOrder()
  {
    return medicationOrder;
  }

  @Override
  public String getTemplateId()
  {
    return "OPENeP - Inpatient Prescription";
  }

  public static String getMedicationOrderPath()
  {
    return "content[openEHR-EHR-INSTRUCTION.medication_order.v2]";
  }

  public void setMedicationOrder(final MedicationOrder medicationOrder)
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
