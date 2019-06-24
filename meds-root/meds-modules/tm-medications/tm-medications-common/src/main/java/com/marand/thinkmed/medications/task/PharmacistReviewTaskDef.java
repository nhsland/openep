package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Mitja Lapajne
 */
public class PharmacistReviewTaskDef extends MedsTaskDef
{
  public static final PharmacistReviewTaskDef INSTANCE = new PharmacistReviewTaskDef();
  public static final TaskVariable LAST_EDITOR_NAME = TaskVariable.named("lastEditorName");
  public static final TaskVariable LAST_EDIT_TIMESTAMP_MILLIS = TaskVariable.named("lastEditTimestampMillis");
  public static final TaskVariable CHANGE_TYPE = TaskVariable.named("changeType");
  public static final TaskVariable STATUS = TaskVariable.named("status");

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "PHARMACIST_REVIEW_TASK_PATIENT_ID";
  }
}
