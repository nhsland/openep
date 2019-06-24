package com.marand.thinkmed.medications.infusion.impl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.marand.ispek.bpm.TaskCompletedType;
import com.marand.thinkmed.medications.dto.administration.InfusionBagTaskDto;
import com.marand.thinkmed.medications.infusion.InfusionBagTaskHandler;
import com.marand.thinkmed.medications.infusion.InfusionBagTaskProvider;
import com.marand.thinkmed.medications.task.InfusionBagChangeTaskDef;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */
@Component
public class InfusionBagTaskProviderImpl implements InfusionBagTaskProvider
{
  private ProcessService processService;
  private InfusionBagTaskHandler infusionBagTaskHandler;

  @Autowired
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Autowired
  public void setInfusionBagTaskHandler(final InfusionBagTaskHandler infusionBagTaskHandler)
  {
    this.infusionBagTaskHandler = infusionBagTaskHandler;
  }

  @Override
  public List<InfusionBagTaskDto> findInfusionBagTasks(
      final @NonNull String patientId,
      final @NonNull List<String> therapyIds,
      final Interval searchInterval)
  {
    final List<TaskDto> tasks =
        findTasks(
            Collections.singletonList(InfusionBagChangeTaskDef.INSTANCE.buildKey(patientId)),
            therapyIds,
            false,
            searchInterval != null ? searchInterval.getStart() : null,
            searchInterval != null ? searchInterval.getEnd() : null);

    return tasks
        .stream()
        .map(infusionBagTaskHandler::convertTaskToInfusionBagTask)
        .collect(Collectors.toList());
  }

  @Override
  public List<TaskDto> findTasks(
      final @NonNull List<String> taskKeys,
      final @NonNull List<String> therapyIds,
      final boolean historic,
      final DateTime taskDueAfter,
      final DateTime taskDueBefore)
  {
    final List<TaskDto> allTasks = processService.findTasks(
        null,
        null,
        null,
        historic,
        taskDueAfter,
        taskDueBefore,
        taskKeys,
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    final Predicate<TaskDto> containedInTherapies = task -> {
      final String therapyId = (String)task.getVariables().get(InfusionBagChangeTaskDef.THERAPY_ID.getName());
      return therapyIds.contains(therapyId);
    };

    return allTasks
        .stream()
        .filter(containedInTherapies)
        .filter(task -> task.getCompletedType() != TaskCompletedType.DELETED)
        .collect(Collectors.toList());
  }
}
