package com.marand.thinkmed.medications.infusion;

import java.util.List;
import lombok.NonNull;

import com.marand.thinkmed.medications.dto.administration.InfusionBagTaskDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface InfusionBagTaskProvider
{
  List<InfusionBagTaskDto> findInfusionBagTasks(
      @NonNull String patientId,
      @NonNull List<String> therapyIds,
      Interval searchInterval);

  List<TaskDto> findTasks(
      @NonNull List<String> taskKeys,
      @NonNull List<String> therapyIds,
      boolean historic,
      DateTime taskDueAfter,
      DateTime taskDueBefore);
}
