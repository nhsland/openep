package com.marand.thinkmed.medications.task;

/**
 * @author Nejc Korasa
 */

public class AdmissionReviewTaskDef extends ReconciliationReviewTaskDef
{
  public static final AdmissionReviewTaskDef INSTANCE = new AdmissionReviewTaskDef();

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "ADMISSION_REVIEW_TASK_PATIENT_ID";
  }
}
