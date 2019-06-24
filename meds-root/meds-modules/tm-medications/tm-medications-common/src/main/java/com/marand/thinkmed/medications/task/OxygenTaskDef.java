package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Nejc Korasa
 */
public class OxygenTaskDef extends AdministrationTaskDef
{
  public static final TaskVariable OXYGEN_ADMINISTRATION = TaskVariable.named("oxygenAdministration");
  public static final TaskVariable STARTING_DEVICE_ROUTE = TaskVariable.named("startingDeviceRoute");
  public static final TaskVariable STARTING_DEVICE_TYPE = TaskVariable.named("startingDeviceType");
}
