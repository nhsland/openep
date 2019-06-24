package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Klavdij Lapajne
 */
public abstract class SupplyTaskDef extends TherapyTaskDef
{
  public static final TaskVariable TASK_CLOSED_WITH_THERAPY_ID = TaskVariable.named("closedWithTherapyId");
  public static final TaskVariable SUPPLY_TYPE = TaskVariable.named("supplyType");
  public static final TaskVariable DAYS_SUPPLY = TaskVariable.named("supplyInDays");
  public static final TaskVariable SUPPLY_REQUEST_COMMENT = TaskVariable.named("supplyRequestComment");
}
