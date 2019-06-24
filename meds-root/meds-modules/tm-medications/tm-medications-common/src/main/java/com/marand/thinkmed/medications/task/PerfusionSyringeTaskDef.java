package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Klavdij Lapajne
 */
public abstract class PerfusionSyringeTaskDef extends TherapyTaskDef
{
  public static final TaskVariable IS_URGENT = TaskVariable.named("isUrgent");
  public static final TaskVariable NUMBER_OF_SYRINGES = TaskVariable.named("numberOfSyringes");
  public static final TaskVariable PREPARATION_STARTED_TIME_MILLIS = TaskVariable.named("preparationStartedTimeMillis");

  public static final TaskVariable ORDERER = TaskVariable.named("orderer");
  public static final TaskVariable ORDERER_FULL_NAME = TaskVariable.named("ordererFullName");
  public static final TaskVariable PRINT_SYSTEM_LABEL = TaskVariable.named("printSystemLabel");
}
