package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvDateTime;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public abstract class Action extends Entry
{
  @EhrMapped("time")
  private DvDateTime time;

  @EhrMapped("ism_transition")
  private IsmTransition ismTransition;

  @EhrMapped("instruction_details")
  private InstructionDetails instructionDetails;

  public DvDateTime getTime()
  {
    return time;
  }

  public void setTime(final DvDateTime time)
  {
    this.time = time;
  }

  public IsmTransition getIsmTransition()
  {
    return ismTransition;
  }

  public void setIsmTransition(final IsmTransition ismTransition)
  {
    this.ismTransition = ismTransition;
  }

  public InstructionDetails getInstructionDetails()
  {
    return instructionDetails;
  }

  public void setInstructionDetails(final InstructionDetails instructionDetails)
  {
    this.instructionDetails = instructionDetails;
  }
}
