package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Nejc Korasa
 */
public class CheckNewAllergiesTaskDef extends MedsTaskDef
{
  public static final CheckNewAllergiesTaskDef INSTANCE = new CheckNewAllergiesTaskDef();
  public static final TaskVariable NEW_ALLERGIES = TaskVariable.named("newAllergiesCodes");

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "CHECK_NEW_ALLERGIES_TASK_PATIENT_ID";
  }
}
