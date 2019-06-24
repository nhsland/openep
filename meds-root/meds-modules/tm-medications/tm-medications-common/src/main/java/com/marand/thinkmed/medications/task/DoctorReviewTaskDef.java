package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Mitja Lapajne
 */
public class DoctorReviewTaskDef extends TherapyTaskDef
{
  public static final DoctorReviewTaskDef INSTANCE = new DoctorReviewTaskDef();
  public static final TaskVariable PATIENT_ID = TaskVariable.named("patientId");
  public static final TaskVariable COMMENT = TaskVariable.named("comment");

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "DOCTOR_REVIEW_TASK_PATIENT_ID";
  }
}
