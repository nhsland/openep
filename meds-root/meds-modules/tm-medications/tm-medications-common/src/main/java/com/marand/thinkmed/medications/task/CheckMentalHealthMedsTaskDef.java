package com.marand.thinkmed.medications.task;

/**
 * @author Nejc Korasa
 */
public class CheckMentalHealthMedsTaskDef extends MedsTaskDef
{
  public static final CheckMentalHealthMedsTaskDef INSTANCE = new CheckMentalHealthMedsTaskDef();

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "CHECK_MENTAL_HEALTH_MEDS_TASK_PATIENT_ID";
  }
}
