package com.marand.thinkmed.medications.dto;

import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.ehr.model.IsmTransition;
import lombok.NonNull;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Mitja Lapajne
 */

public enum ClinicalInterventionEnum
{
  COMPLETED(
      DataValueUtils.getCodedText("local", "at0043", "Procedure has been completed."),
      DataValueUtils.getCodedText("openehr", "532", "completed"));

  private final DvCodedText careflowStep;
  private final DvCodedText currentState;

  ClinicalInterventionEnum(final @NonNull DvCodedText careflowStep, final @NonNull DvCodedText currentState)
  {
    this.careflowStep = careflowStep;
    this.currentState = currentState;
  }

  public DvCodedText getCareflowStep()
  {
    return careflowStep;
  }

  public DvCodedText getCurrentState()
  {
    return currentState;
  }

  public IsmTransition buildIsmTransition()
  {
    final IsmTransition ismTransition = new IsmTransition();
    ismTransition.setCareflowStep(careflowStep);
    ismTransition.setCurrentState(currentState);
    return ismTransition;
  }

  public static ClinicalInterventionEnum getAction(final DvCodedText careflowStep, final DvCodedText currentState)
  {
    for (final ClinicalInterventionEnum medicationAction : values())
    {
      if (medicationAction.getCareflowStep().equals(careflowStep) && medicationAction.getCurrentState().equals(currentState))
      {
        return medicationAction;
      }
    }
    return null;
  }
}
