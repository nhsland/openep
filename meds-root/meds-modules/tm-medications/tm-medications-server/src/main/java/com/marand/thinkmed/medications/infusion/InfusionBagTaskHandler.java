package com.marand.thinkmed.medications.infusion;

import java.util.List;
import lombok.NonNull;

import com.marand.thinkmed.medications.dto.administration.InfusionBagTaskDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface InfusionBagTaskHandler
{
  InfusionBagTaskDto convertTaskToInfusionBagTask(@NonNull TaskDto task);

  void deleteInfusionBagTasks(
      @NonNull String patientId,
      @NonNull List<String> therapyIds,
      String comment);

  void createInfusionBagTask(
      @NonNull String patientId,
      @NonNull String therapyId,
      @NonNull DateTime plannedInfusionBagChange);
}
