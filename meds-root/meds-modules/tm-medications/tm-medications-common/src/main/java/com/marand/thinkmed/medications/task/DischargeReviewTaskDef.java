package com.marand.thinkmed.medications.task;

/**
 * @author Mitja Lapajne
 */

public class DischargeReviewTaskDef extends ReconciliationReviewTaskDef
{
  public static final DischargeReviewTaskDef INSTANCE = new DischargeReviewTaskDef();

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "DISCHARGE_REVIEW_TASK_PATIENT_ID";
  }
}
