
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.LocatableRef;

/**
 * @author Nejc Korasa
 */

public class InstructionDetails
{
  @EhrMapped("instruction_id")
  private LocatableRef instructionId;

  @EhrMapped("activity_id")
  private String activityId;

  public LocatableRef getInstructionId()
  {
    return instructionId;
  }

  public void setInstructionId(final LocatableRef instructionId)
  {
    this.instructionId = instructionId;
  }

  public String getActivityId()
  {
    return activityId;
  }

  public void setActivityId(final String activityId)
  {
    this.activityId = activityId;
  }
}
