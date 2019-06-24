
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;

/**
 * @author Mitja Lapajne
 */
public class MedicationAdministration extends EhrComposition
{
  @EhrMapped("content[openEHR-EHR-ACTION.medication.v1]")
  private MedicationManagement medicationManagement;

  @EhrMapped("content[openEHR-EHR-ACTION.procedure.v1]")
  private Procedure procedure;

  @Override
  public String getTemplateId()
  {
    return "OPENeP - Medication Administration";
  }

  public MedicationManagement getMedicationManagement()
  {
    return medicationManagement;
  }

  public void setMedicationManagement(final MedicationManagement medicationManagement)
  {
    this.medicationManagement = medicationManagement;
  }

  public Procedure getProcedure()
  {
    return procedure;
  }

  public void setProcedure(final Procedure procedure)
  {
    this.procedure = procedure;
  }

  public InstructionDetails getInstructionDetails()
  {
    final Action action = medicationManagement != null ? medicationManagement : procedure;
    return action != null ? action.getInstructionDetails() : null;
  }
}
