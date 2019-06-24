package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Klavdij Lapajne
 */
public class DispenseMedicationTaskDef extends SupplyTaskDef
{
  public static final DispenseMedicationTaskDef INSTANCE = new DispenseMedicationTaskDef();
  public static final TaskVariable REQUESTER_ROLE = TaskVariable.named("requesterRole");
  public static final TaskVariable REQUEST_STATUS = TaskVariable.named("supplyRequestStatus");
  public static final TaskVariable LAST_PRINTED_TIMESTAMP = TaskVariable.named("lastPrintedTimestamp");

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "DISPENSE_MEDICATION_TASK_PATIENT_ID";
  }
}
