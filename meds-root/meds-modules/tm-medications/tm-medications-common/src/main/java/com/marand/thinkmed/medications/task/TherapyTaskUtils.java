package com.marand.thinkmed.medications.task;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.marand.maf.core.formatter.NumberFormatters;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import lombok.NonNull;

/**
 * @author Mitja Lapajne
 */
public class TherapyTaskUtils
{
  private TherapyTaskUtils()
  {
  }

  public static List<String> getPatientIdKeysForTaskTypes(
      final @NonNull Collection<String> patientIdsSet,
      final @NonNull Set<TaskTypeEnum> taskTypesSet)
  {
    final List<String> patientIdKeys = new ArrayList<>();
    for (final String patientId : patientIdsSet)
    {
      for (final TaskTypeEnum taskType : taskTypesSet)
      {
        patientIdKeys.add(taskType.getTaskDef().buildKey(patientId));
      }
    }
    return patientIdKeys;
  }

  public static TherapyDoseDto buildTherapyDoseDtoFromTask(final @NonNull TaskDto task)
  {
    final String doseType = (String)task.getVariables().get(AdministrationTaskDef.DOSE_TYPE.getName());

    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();
    therapyDoseDto.setTherapyDoseTypeEnum(doseType != null ? TherapyDoseTypeEnum.valueOf(doseType) : null);
    therapyDoseDto.setDenominator((Double)task.getVariables().get(AdministrationTaskDef.DOSE_DENOMINATOR.getName()));
    therapyDoseDto.setDenominatorUnit((String)task.getVariables().get(AdministrationTaskDef.DOSE_DENOMINATOR_UNIT.getName()));
    therapyDoseDto.setNumerator((Double)task.getVariables().get(AdministrationTaskDef.DOSE_NUMERATOR.getName()));
    therapyDoseDto.setNumeratorUnit((String)task.getVariables().get(AdministrationTaskDef.DOSE_NUMERATOR_UNIT.getName()));

    return therapyDoseDto.getNumerator() == null ? null : therapyDoseDto;
  }

  public static String buildPlannedDoseDisplay(final TaskDto task)
  {
    final Double doseNumerator = (Double)task.getVariables().get(AdministrationTaskDef.DOSE_NUMERATOR.getName());
    final String doseNumeratorUnit = (String)task.getVariables().get(AdministrationTaskDef.DOSE_NUMERATOR_UNIT.getName());
    if (doseNumerator != null)
    {
      final StringBuilder plannedDoseDisplay = new StringBuilder();
      final String numeratorDisplay;
      try
      {
        numeratorDisplay = NumberFormatters.doubleFormatter2().valueToString(doseNumerator);
      }
      catch (final ParseException e)
      {
        throw new IllegalArgumentException(e);
      }

      plannedDoseDisplay.append(numeratorDisplay).append(" ").append(doseNumeratorUnit);
      return plannedDoseDisplay.toString();
    }
    return null;
  }
}
