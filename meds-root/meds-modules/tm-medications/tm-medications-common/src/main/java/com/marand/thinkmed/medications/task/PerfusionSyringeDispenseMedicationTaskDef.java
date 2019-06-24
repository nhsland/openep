package com.marand.thinkmed.medications.task;

/**
 * @author Klavdij Lapajne
 */
public class PerfusionSyringeDispenseMedicationTaskDef extends PerfusionSyringeTaskDef
{
  public static final PerfusionSyringeDispenseMedicationTaskDef INSTANCE = new PerfusionSyringeDispenseMedicationTaskDef();

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "PERFUSION_SYRINGE_DISPENSE_TASK_PATIENT_ID";
  }
}
