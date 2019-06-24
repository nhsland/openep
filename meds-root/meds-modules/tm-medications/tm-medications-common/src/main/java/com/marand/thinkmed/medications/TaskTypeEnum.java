package com.marand.thinkmed.medications;

import java.util.Arrays;
import java.util.EnumSet;

import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.AdmissionReviewTaskDef;
import com.marand.thinkmed.medications.task.CheckMentalHealthMedsTaskDef;
import com.marand.thinkmed.medications.task.CheckNewAllergiesTaskDef;
import com.marand.thinkmed.medications.task.DischargeReviewTaskDef;
import com.marand.thinkmed.medications.task.DispenseMedicationTaskDef;
import com.marand.thinkmed.medications.task.DoctorReviewTaskDef;
import com.marand.thinkmed.medications.task.InfusionBagChangeTaskDef;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeCompletePreparationTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeDispenseMedicationTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeStartPreparationTaskDef;
import com.marand.thinkmed.medications.task.PharmacistReminderTaskDef;
import com.marand.thinkmed.medications.task.PharmacistReviewTaskDef;
import com.marand.thinkmed.medications.task.SupplyReminderTaskDef;
import com.marand.thinkmed.medications.task.SupplyReviewTaskDef;
import com.marand.thinkmed.medications.task.SwitchToOralTaskDef;

/**
 * User: Klavdij Lapajne
 */

public enum TaskTypeEnum
{
  ADMINISTRATION_TASK("AdministrationTask", AdministrationTaskDef.INSTANCE),
  ADMISSION_REVIEW_TASK("AdmissionReviewTask", AdmissionReviewTaskDef.INSTANCE),
  DISCHARGE_REVIEW_TASK("DischargeReviewTask", DischargeReviewTaskDef.INSTANCE),
  INFUSION_BAG_CHANGE_TASK("InfusionBagChangeTask", InfusionBagChangeTaskDef.INSTANCE),
  PHARMACIST_REVIEW("PharmacistReviewTask", PharmacistReviewTaskDef.INSTANCE),
  PHARMACIST_REMINDER("PharmacistReminderTask", PharmacistReminderTaskDef.INSTANCE),
  SUPPLY_REMINDER("SupplyReminderTask", SupplyReminderTaskDef.INSTANCE),
  SUPPLY_REVIEW("SupplyReviewTask", SupplyReviewTaskDef.INSTANCE),
  DISPENSE_MEDICATION("DispenseMedicationTask", DispenseMedicationTaskDef.INSTANCE),
  DOCTOR_REVIEW("DoctorReview", DoctorReviewTaskDef.INSTANCE),
  SWITCH_TO_ORAL("SwitchToOral", SwitchToOralTaskDef.INSTANCE),
  CHECK_NEW_ALLERGIES("CheckNewAllergies", CheckNewAllergiesTaskDef.INSTANCE),
  CHECK_MENTAL_HEALTH_MEDS("CheckMentalHealthMeds", CheckMentalHealthMedsTaskDef.INSTANCE),
  PERFUSION_SYRINGE_START("PerfusionSyringeStartPreparationTask", PerfusionSyringeStartPreparationTaskDef.INSTANCE),
  PERFUSION_SYRINGE_COMPLETE("PerfusionSyringeCompletePreparationTask", PerfusionSyringeCompletePreparationTaskDef.INSTANCE),
  PERFUSION_SYRINGE_DISPENSE("PerfusionSyringeDispenseMedicationTask", PerfusionSyringeDispenseMedicationTaskDef.INSTANCE);

  private final String name;
  private final MedsTaskDef taskDef;

  TaskTypeEnum(final String name, final MedsTaskDef taskDef)
  {
    this.name = name;
    this.taskDef = taskDef;
  }

  public String getName()
  {
    return name;
  }

  public MedsTaskDef getTaskDef()
  {
    return taskDef;
  }

  public static TaskTypeEnum valueOf(final MedsTaskDef medsTaskDef)
  {
    return Arrays.stream(values())
        .filter(v -> v.getTaskDef().equals(medsTaskDef))
        .findAny()
        .orElse(null);
  }

  public static TaskTypeEnum getByName(final String name)
  {
    for (final TaskTypeEnum taskTypeEnum : values())
    {
      if (name.equals(taskTypeEnum.name))
      {
        return taskTypeEnum;
      }
    }
    return null;
  }

  public static final EnumSet<TaskTypeEnum> SUPPLY_TASKS_SET = EnumSet.of(
      SUPPLY_REMINDER,
      DISPENSE_MEDICATION,
      SUPPLY_REVIEW);
  public static final EnumSet<TaskTypeEnum> PERFUSION_SYRINGE_TASKS_SET = EnumSet.of(
      PERFUSION_SYRINGE_START,
      PERFUSION_SYRINGE_COMPLETE,
      PERFUSION_SYRINGE_DISPENSE);
}
