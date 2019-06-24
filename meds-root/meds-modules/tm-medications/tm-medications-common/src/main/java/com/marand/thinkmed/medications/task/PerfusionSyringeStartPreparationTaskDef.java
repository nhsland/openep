package com.marand.thinkmed.medications.task;

/**
 * @author Klavdij Lapajne
 */
public class PerfusionSyringeStartPreparationTaskDef extends PerfusionSyringeTaskDef
{
  public static final PerfusionSyringeStartPreparationTaskDef INSTANCE = new PerfusionSyringeStartPreparationTaskDef();

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "PERFUSION_SYRINGE_START_TASK_PATIENT_ID";
  }
}
