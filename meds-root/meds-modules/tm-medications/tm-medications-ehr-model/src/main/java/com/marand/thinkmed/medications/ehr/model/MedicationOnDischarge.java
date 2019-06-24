
package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;

/**
 * @author Mitja Lapajne
 */
public class MedicationOnDischarge extends EhrComposition
{
  @EhrMapped("content[openEHR-EHR-INSTRUCTION.medication_order.v2]")
  private MedicationOrder medicationOrder;

  @EhrMapped("content[openEHR-EHR-ACTION.medication.v1]")
  private List<MedicationManagement> actions = new ArrayList<>();

  @Override
  public String getTemplateId()
  {
    return "OPENeP - Medication on Discharge";
  }

  public static String getMedicationOrderPath()
  {
    return "content[openEHR-EHR-INSTRUCTION.medication_order.v2]";
  }

  public MedicationOrder getMedicationOrder()
  {
    return medicationOrder;
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
