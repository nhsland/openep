package com.marand.thinkmed.medications.task;

import com.marand.thinkmed.process.definition.TaskVariable;

/**
 * @author Nejc Korasa
 */
public class InfusionBagChangeTaskDef extends MedsTaskDef
{
  public static final InfusionBagChangeTaskDef INSTANCE = new InfusionBagChangeTaskDef();
  public static final TaskVariable ADMINISTRATION_TYPE = TaskVariable.named("administrationType");
  public static final TaskVariable THERAPY_ADMINISTRATION_ID = TaskVariable.named("therapyAdministrationId");
  public static final TaskVariable THERAPY_ID = TaskVariable.named("therapyId");
  public static final TaskVariable INFUSION_BAG_QUANTITY = TaskVariable.named("infusionBagQuantity");
  public static final TaskVariable INFUSION_BAG_UNIT = TaskVariable.named("infusionBagUnit");

  @Override
  public MedsTaskDef getInstance()
  {
    return INSTANCE;
  }

  @Override
  public String getKeyPrefix()
  {
    return "INFUSION_BAG_PATIENT_ID";
  }
}
