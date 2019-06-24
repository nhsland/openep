package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Klavdij Lapajne
 */
public class PharmacistReminderTaskDef extends MedsTaskDef
{
  public static final PharmacistReminderTaskDef INSTANCE = new PharmacistReminderTaskDef();
  public static final TaskVariable PHARMACIST_REVIEW_ID = TaskVariable.named("pharmacistReviewId");
  public static final TaskVariable COMMENT = TaskVariable.named("comment");

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "PHARMACIST_REMINDER_TASK_PATIENT_ID";
  }
}
