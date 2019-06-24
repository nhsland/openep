
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Mitja Lapajne
 */
public class IsmTransition
{
  @EhrMapped("current_state")
  private DvCodedText currentState;

  @EhrMapped("careflow_step")
  private DvCodedText careflowStep;

  public DvCodedText getCurrentState()
  {
    return currentState;
  }

  public void setCurrentState(final DvCodedText currentState)
  {
    this.currentState = currentState;
  }

  public DvCodedText getCareflowStep()
  {
    return careflowStep;
  }

  public void setCareflowStep(final DvCodedText careflowStep)
  {
    this.careflowStep = careflowStep;
  }
}
