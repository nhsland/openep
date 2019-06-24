package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Klavdij Lapajne
 */
public class SupplyReviewTaskDef extends SupplyTaskDef
{
  public static final SupplyReviewTaskDef INSTANCE = new SupplyReviewTaskDef();
  public static final TaskVariable ALREADY_DISPENSED = TaskVariable.named("alreadyDispensed");
  public static final TaskVariable REQUESTER_ROLE = TaskVariable.named("requesterRole");

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "SUPPLY_REVIEW_TASK_PATIENT_ID";
  }
}
