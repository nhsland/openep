package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Bostjan Vester
 */
public class AdministrationTaskDef extends MedsTaskDef
{
  public static final AdministrationTaskDef INSTANCE = new AdministrationTaskDef();

  public static final TaskVariable THERAPY_ID = TaskVariable.named("therapyId");
  public static final TaskVariable THERAPY_ADMINISTRATION_ID = TaskVariable.named("therapyAdministrationId");

  public static final TaskVariable ADMINISTRATION_TYPE = TaskVariable.named("administrationType");
  public static final TaskVariable DOSE_TYPE = TaskVariable.named("doseType");
  public static final TaskVariable DOSE_NUMERATOR = TaskVariable.named("doseNumerator");
  public static final TaskVariable DOSE_NUMERATOR_UNIT = TaskVariable.named("doseNumeratorUnit");
  public static final TaskVariable DOSE_DENOMINATOR = TaskVariable.named("doseDenominator");
  public static final TaskVariable DOSE_DENOMINATOR_UNIT = TaskVariable.named("doseDenominatorUnit");

  public static final TaskVariable DOCTOR_CONFIRMATION = TaskVariable.named("doctorConfirmation");
  public static final TaskVariable DOCTORS_COMMENT = TaskVariable.named("doctorsComment");
  public static final TaskVariable DELETE_COMMENT = TaskVariable.named("deleteReason");
  public static final TaskVariable GROUP_UUID = TaskVariable.named("groupUUId");
  public static final TaskVariable THERAPY_END = TaskVariable.named("therapyEnd");

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "ADMINISTRATION_TASK_PATIENT_ID";
  }
}
