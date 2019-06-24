package com.marand.thinkmed.medications.task;

/**
 * @author Klavdij Lapajne
 */
public class PerfusionSyringeCompletePreparationTaskDef extends PerfusionSyringeTaskDef
{
  public static final PerfusionSyringeCompletePreparationTaskDef INSTANCE = new PerfusionSyringeCompletePreparationTaskDef();

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "PERFUSION_SYRINGE_COMPLETE_TASK_PATIENT_ID";
  }
}
