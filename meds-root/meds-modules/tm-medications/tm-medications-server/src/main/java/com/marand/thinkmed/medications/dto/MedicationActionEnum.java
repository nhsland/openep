package com.marand.thinkmed.medications.dto;

import java.util.EnumSet;
import java.util.Set;
import lombok.NonNull;

import com.google.common.base.Preconditions;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.ehr.model.IsmTransition;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Bostjan Vester
 */
public enum MedicationActionEnum
{
  //therapy options
  SCHEDULE(
      DataValueUtils.getCodedText("local", "at0016", "Medication start date/condition set"),
      DataValueUtils.getCodedText("openehr", "529", "scheduled")),
  START(
      DataValueUtils.getCodedText("local", "at0004", "Medication course commenced"),
      DataValueUtils.getCodedText("openehr", "245", "active")),
  REVIEW(
      DataValueUtils.getCodedText("local", "at0005", "Medication reassessed"),
      DataValueUtils.getCodedText("openehr", "245", "active")),
  SUSPEND(
      DataValueUtils.getCodedText("local", "at0009", "Administrations suspended"),
      DataValueUtils.getCodedText("openehr", "530", "suspended")),
  REISSUE(
      DataValueUtils.getCodedText("local", "at0010", "Prescription re-issued"),
      DataValueUtils.getCodedText("openehr", "245", "active")),
  CANCEL(
      DataValueUtils.getCodedText("local", "at0012", "Medication course cancelled"),
      DataValueUtils.getCodedText("openehr", "528", "cancelled")),
  MODIFY_EXISTING(
      DataValueUtils.getCodedText("local", "at0041", "Minor change to order"),
      DataValueUtils.getCodedText("openehr", "245", "active")),
  COMPLETE(
      DataValueUtils.getCodedText("local", "at0039", "Major change to order"),
      DataValueUtils.getCodedText("openehr", "531", "aborted")),
  ABORT(
      DataValueUtils.getCodedText("local", "at0015", "Medication course stopped"),
      DataValueUtils.getCodedText("openehr", "531", "aborted")),

  //pharmacists options
  RECOMMEND(
      DataValueUtils.getCodedText("local", "at0109", "Medication recommended"),
      DataValueUtils.getCodedText("openehr", "526", "planned")),

  //administration options
  ADMINISTER(
      DataValueUtils.getCodedText("local", "at0006", "Dose administered"),
      DataValueUtils.getCodedText("openehr", "245", "active")),
  WITHHOLD(
      DataValueUtils.getCodedText("local", "at0018", "Dose administration omitted"),
      DataValueUtils.getCodedText("openehr", "245", "active")),
  DEFER(
      DataValueUtils.getCodedText("local", "at0044", "Dose administration deferred"),
      DataValueUtils.getCodedText("openehr", "245", "active"));

  public static final Set<MedicationActionEnum> THERAPY_FINISHED = EnumSet.of(ABORT, CANCEL, COMPLETE);
  public static final Set<MedicationActionEnum> THERAPY_REVIEW_ACTIONS =
      EnumSet.of(SCHEDULE, SUSPEND, REISSUE, REVIEW, CANCEL, MODIFY_EXISTING, COMPLETE, ABORT);

  private final DvCodedText careflowStep;
  private final DvCodedText currentState;

  MedicationActionEnum(final DvCodedText careflowStep, final DvCodedText currentState)
  {
    this.careflowStep = Preconditions.checkNotNull(careflowStep);
    this.currentState = Preconditions.checkNotNull(currentState);
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

  private static MedicationActionEnum getActionEnum(final DvCodedText careflowStep, final DvCodedText currentState)
  {
    for (final MedicationActionEnum medicationAction : values())
    {
      if (medicationAction.getCareflowStep().equals(careflowStep) && medicationAction.getCurrentState().equals(currentState))
      {
        return medicationAction;
      }
    }
    return null;
  }

  public static MedicationActionEnum getActionEnum(final @NonNull MedicationManagement action)
  {
    return getActionEnum(action.getIsmTransition().getCareflowStep(), action.getIsmTransition().getCurrentState());
  }

  public static MedicationActionEnum fromAdministrationResult(final AdministrationResultEnum result)
  {
    if (result == AdministrationResultEnum.NOT_GIVEN)
    {
      return WITHHOLD;
    }
    if (result == AdministrationResultEnum.DEFER)
    {
      return DEFER;
    }
    return ADMINISTER;
  }
}
