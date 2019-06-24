package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Klavdij Lapajne
 */
public class SupplyReminderTaskDef extends SupplyTaskDef
{
  public static final SupplyReminderTaskDef INSTANCE = new SupplyReminderTaskDef();
  public static final TaskVariable IS_DISMISSED = TaskVariable.named("isDismissed");

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "SUPPLY_REMINDER_TASK_PATIENT_ID";
  }
}
