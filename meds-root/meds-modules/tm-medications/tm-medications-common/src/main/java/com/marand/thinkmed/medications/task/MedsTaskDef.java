package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.process.definition.TaskDef;
import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Klavdij Lapajne
 */
public abstract class MedsTaskDef extends TaskDef
{
  public static final String GROUP_NAME = "Medications";
  public static final TaskVariable PATIENT_ID = TaskVariable.named("patientId");
  public static final TaskVariable PERFORMER = TaskVariable.named("performer");

  @Override
  public final String getGroupName()
  {
    return GROUP_NAME;
  }

  public abstract String getKeyPrefix();

  public abstract MedsTaskDef getInstance();

  public String buildKey(final String value)
  {
    if (value == null)
    {
      return null;
    }
    return getKeyPrefix() + "_" + value;
  }

  public TaskTypeEnum getTaskTypeEnum()
  {
    return TaskTypeEnum.valueOf(getInstance());
  }

  @Override
  public String getTaskExecutionId()
  {
    return getTaskTypeEnum().getName();
  }
}
