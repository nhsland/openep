package com.marand.thinkmed.medications.task;

/**
 * @author Mitja Lapajne
 */
public class SwitchToOralTaskDef extends TherapyTaskDef
{
  public static final SwitchToOralTaskDef INSTANCE = new SwitchToOralTaskDef();

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "SWITCH_TO_ORAL_TASK_PATIENT_ID";
  }
}
