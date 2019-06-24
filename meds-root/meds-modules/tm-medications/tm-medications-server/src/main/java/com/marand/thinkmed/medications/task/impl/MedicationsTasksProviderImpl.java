package com.marand.thinkmed.medications.task.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.maf.core.PartialList;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.allergies.CheckNewAllergiesTaskDto;
import com.marand.thinkmed.medications.dto.mentalHealth.CheckMentalHealthMedsTaskDto;
import com.marand.thinkmed.medications.dto.task.TherapyDoctorReviewTaskDto;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.CheckMentalHealthMedsTaskDef;
import com.marand.thinkmed.medications.task.CheckNewAllergiesTaskDef;
import com.marand.thinkmed.medications.task.DoctorReviewTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.PerfusionSyringeCompletePreparationTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeDispenseMedicationTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeStartPreparationTaskDef;
import com.marand.thinkmed.medications.task.SupplyReminderTaskDef;
import com.marand.thinkmed.medications.task.SupplyReviewTaskDef;
import com.marand.thinkmed.medications.task.SwitchToOralTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskDef;
import com.marand.thinkmed.patient.PatientDataProvider;
import com.marand.thinkmed.process.dto.AbstractTaskDto;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class MedicationsTasksProviderImpl implements MedicationsTasksProvider
{
  private ProcessService processService;
  private AdministrationTaskConverter administrationTaskConverter;
  private PatientDataProvider patientDataProvider;

  @Autowired
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Autowired
  public void setAdministrationTaskConverter(final AdministrationTaskConverter administrationTaskConverter)
  {
    this.administrationTaskConverter = administrationTaskConverter;
  }

  @Autowired
  public void setPatientDataProvider(final PatientDataProvider patientDataProvider)
  {
    this.patientDataProvider = patientDataProvider;
  }

  @Override
  public List<AdministrationTaskDto> findAdministrationTasks(
      final String patientId,
      final Collection<String> therapyIds,
      final Interval searchInterval,
      final boolean findHistoric)
  {
    if (therapyIds.isEmpty())
    {
      return new ArrayList<>();
    }
    final List<TaskDto> tasks =
        findAdministrationTasks(
            Collections.singletonList(AdministrationTaskDef.INSTANCE.buildKey(patientId)),
            therapyIds,
            searchInterval != null ? searchInterval.getStart() : null,
            searchInterval != null ? searchInterval.getEnd() : null,
            null,
            findHistoric);

    return tasks.stream()
        .map(task -> administrationTaskConverter.convertTaskToAdministrationTask(task))
        .collect(Collectors.toList());
  }

  @Override
  public List<TaskDto> findAdministrationTasks(
      final String patientId,
      final Collection<String> therapyIds,
      final DateTime taskDueAfter,
      final DateTime taskDueBefore,
      final String groupUUId,
      final boolean findHistoric)
  {
    final List<String> taskKeys = Collections.singletonList(AdministrationTaskDef.INSTANCE.buildKey(patientId));
    return findAdministrationTasks(taskKeys, therapyIds, taskDueAfter, taskDueBefore, groupUUId, findHistoric);
  }

  @Override
  public AdministrationTaskDto loadAdministrationTask(final @NonNull String taskId)
  {
    return administrationTaskConverter.convertTaskToAdministrationTask(loadTask(taskId));
  }

  @Override
  public TaskDto loadTask(final @NonNull String taskId)
  {
    return Opt
        .of(processService.loadTask(taskId, false))
        .orElseThrow(() -> new IllegalStateException("Task " + taskId + " not found!"));
  }

  @Override
  public List<TaskDto> findAdministrationTasks(
      final Collection<String> patientIds,
      final DateTime taskDueAfter,
      final DateTime taskDueBefore)
  {
    final List<String> taskKeys = patientIds
        .stream()
        .map(AdministrationTaskDef.INSTANCE::buildKey)
        .collect(Collectors.toList());

    return findAdministrationTasks(taskKeys, null, taskDueAfter, taskDueBefore, null, false);
  }

  private List<TaskDto> findAdministrationTasks(
      final List<String> taskKeys,
      final Collection<String> therapyIds,
      final DateTime taskDueAfter,
      final DateTime taskDueBefore,
      final String groupUUId,
      final boolean findHistorical)
  {
    if (taskKeys.isEmpty())
    {
      return Collections.emptyList();
    }

    final List<TaskDto> allTasks = processService.findTasks(
        null,
        null,
        null,
        findHistorical,
        taskDueAfter,
        taskDueBefore,
        taskKeys,
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    return allTasks.stream()
        .filter(task -> !findHistorical || !task.isDeleted())
        .filter(task -> groupUUId == null || groupUUId.equals(task.getVariables().get(AdministrationTaskDef.GROUP_UUID.getName())))
        .filter(task -> therapyIds == null
            || therapyIds.contains(task.getVariables().get(AdministrationTaskDef.THERAPY_ID.getName())))
        .collect(Collectors.toList());
  }

  @Override
  public TaskDto loadNextAdministrationTask(final String patientId, final DateTime fromWhen)
  {
    final List<TaskDto> tasks = findAdministrationTasks(patientId, null, fromWhen, null, null, false);

    return tasks.stream()
        .filter(t -> t.getDueTime().isAfter(fromWhen))
        .min(Comparator.comparing(TaskDto::getDueTime))
        .orElse(null);
  }

  @Override
  public Opt<AdministrationTaskDto> findLastAdministrationTaskForTherapy(
      final @NonNull String patientId,
      final @NonNull String therapyId,
      final Interval searchInterval,
      final boolean findHistoric)
  {
    return findLastTaskForTherapy(patientId, therapyId, searchInterval, findHistoric)
        .map(task -> administrationTaskConverter.convertTaskToAdministrationTask(task));
  }

  private Opt<TaskDto> findLastTaskForTherapy(
      final String patientId,
      final String therapyId,
      final Interval searchInterval,
      final boolean findHistoric)
  {
    final List<TaskDto> tasks = findAdministrationTasks(
        Collections.singletonList(AdministrationTaskDef.INSTANCE.buildKey(patientId)),
        Collections.singletonList(therapyId),
        searchInterval != null ? searchInterval.getStart() : null,
        searchInterval != null ? searchInterval.getEnd() : null,
        null,
        findHistoric);

    return Opt.from(
        tasks.stream()
            .max(Comparator.comparing(TaskDto::getDueTime)));
  }

  @Override
  public Opt<DateTime> findLastAdministrationTaskTimeForTherapy(
      final @NonNull String patientId,
      final @NonNull String therapyId,
      final Interval searchInterval,
      final boolean findHistoric)
  {
    return findLastTaskForTherapy(patientId, therapyId, searchInterval, findHistoric).map(AbstractTaskDto::getDueTime);
  }

  @Override
  public Map<String, DateTime> findLastAdministrationTaskTimesForTherapies(
      final Collection<String> patientIds,
      final DateTime fromTime,
      final boolean findHistoric)
  {
    final List<String> taskKeys = patientIds
        .stream()
        .map(AdministrationTaskDef.INSTANCE::buildKey)
        .collect(Collectors.toList());

    final List<TaskDto> administrationTasks = findAdministrationTasks(taskKeys, null, fromTime, null, null, findHistoric);

    final Map<String, DateTime> therapyLastAdministrationTaskTimestampMap = new HashMap<>();
    //noinspection Convert2streamapi
    for (final TaskDto taskDto : administrationTasks)
    {
      final String taskTherapyId = (String)taskDto.getVariables().get(AdministrationTaskDef.THERAPY_ID.getName());
      final DateTime taskDueTime = taskDto.getDueTime();
      if (therapyLastAdministrationTaskTimestampMap.containsKey(taskTherapyId))
      {
        if (taskDueTime.isAfter(therapyLastAdministrationTaskTimestampMap.get(taskTherapyId)))
        {
          therapyLastAdministrationTaskTimestampMap.put(taskTherapyId, taskDueTime);
        }
      }
      else
      {
        therapyLastAdministrationTaskTimestampMap.put(taskTherapyId, taskDueTime);
      }
    }

    return therapyLastAdministrationTaskTimestampMap;
  }

  @Override
  public Map<String, List<TherapyTaskSimpleDto>> findSimpleTasksForTherapies(
      final String patientId,
      final Collection<String> therapyIds,
      final DateTime when)
  {
    final List<String> taskKeys = new ArrayList<>();
    taskKeys.add(DoctorReviewTaskDef.INSTANCE.buildKey(String.valueOf(patientId)));
    taskKeys.add(SwitchToOralTaskDef.INSTANCE.buildKey(String.valueOf(patientId)));
    taskKeys.add(SupplyReviewTaskDef.INSTANCE.buildKey(String.valueOf(patientId)));
    taskKeys.add(SupplyReminderTaskDef.INSTANCE.buildKey(String.valueOf(patientId)));
    taskKeys.add(PerfusionSyringeStartPreparationTaskDef.INSTANCE.buildKey(String.valueOf(patientId)));
    taskKeys.add(PerfusionSyringeCompletePreparationTaskDef.INSTANCE.buildKey(String.valueOf(patientId)));
    taskKeys.add(PerfusionSyringeDispenseMedicationTaskDef.INSTANCE.buildKey(String.valueOf(patientId)));

    final Map<String, TaskDto> doctorReviewTasks = new HashMap<>();
    final Map<String, TaskDto> switchToOralTasks = new HashMap<>();
    final Map<String, TaskDto> supplyReviewTasks = new HashMap<>();
    final Map<String, TaskDto> supplyReminderTasks = new HashMap<>();
    final Map<String, TaskDto> perfusionSyringeTasks = new HashMap<>();

    final PartialList<TaskDto> tasks = processService.findTasks(
        null,
        null,
        null,
        false,
        null,
        null,
        taskKeys,
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    if (tasks != null)
    {
      for (final TaskDto task : tasks)
      {
        final String therapyId = (String)task.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
        if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.DOCTOR_REVIEW.getName()))
        {
          //don't show future tasks
          if (!task.getDueTime().withTimeAtStartOfDay().isAfter(when.withTimeAtStartOfDay()))
          {
            doctorReviewTasks.put(therapyId, task);
          }
        }
        else if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.SWITCH_TO_ORAL.getName()))
        {
          //don't show future tasks
          if (!task.getDueTime().withTimeAtStartOfDay().isAfter(when.withTimeAtStartOfDay()))
          {
            switchToOralTasks.put(therapyId, task);
          }
        }
        else if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.SUPPLY_REVIEW.getName()))
        {
          supplyReviewTasks.put(therapyId, task);
        }
        else if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.SUPPLY_REMINDER.getName()))
        {
          final Boolean isDismissed = (Boolean)task.getVariables().get(SupplyReminderTaskDef.IS_DISMISSED.getName());
          if (isDismissed == null || !isDismissed)
          {
            supplyReminderTasks.put(therapyId, task);
          }
        }
        else if (
            task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PERFUSION_SYRINGE_START.getName())
                || task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE.getName())
                || task.getTaskExecutionStrategyId().equals(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE.getName()))
        {
          perfusionSyringeTasks.put(therapyId, task);
        }
      }
    }

    final Map<String, List<TherapyTaskSimpleDto>> tasksMap = new HashMap<>();
    for (final String therapyId : therapyIds)
    {
      tasksMap.put(therapyId, new ArrayList<>());

      if (doctorReviewTasks.containsKey(therapyId))
      {
        final TherapyTaskSimpleDto doctorReviewTask =
            buildTherapyTaskReminderDto(doctorReviewTasks.get(therapyId), TaskTypeEnum.DOCTOR_REVIEW);
        tasksMap.get(therapyId).add(doctorReviewTask);
      }
      if (switchToOralTasks.containsKey(therapyId))
      {
        final TherapyTaskSimpleDto switchToOralTask =
            buildTherapyTaskSimpleDto(switchToOralTasks.get(therapyId), TaskTypeEnum.SWITCH_TO_ORAL);
        tasksMap.get(therapyId).add(switchToOralTask);
      }
      if (supplyReviewTasks.containsKey(therapyId))
      {
        final TherapyTaskSimpleDto supplyReviewTask =
            buildTherapyTaskSimpleDto(supplyReviewTasks.get(therapyId), TaskTypeEnum.SUPPLY_REVIEW);
        tasksMap.get(therapyId).add(supplyReviewTask);
      }
      if (supplyReminderTasks.containsKey(therapyId))
      {
        final TherapyTaskSimpleDto supplyReminderTask =
            buildTherapyTaskSimpleDto(supplyReminderTasks.get(therapyId), TaskTypeEnum.SUPPLY_REMINDER);
        tasksMap.get(therapyId).add(supplyReminderTask);
      }
      if (perfusionSyringeTasks.containsKey(therapyId))
      {
        final TaskDto perfusionSyringeTaskDto = perfusionSyringeTasks.get(therapyId);
        final TherapyTaskSimpleDto perfusionSyringeTaskSimple = buildTherapyTaskSimpleDto(
            perfusionSyringeTasks.get(therapyId),
            TaskTypeEnum.getByName(perfusionSyringeTaskDto.getTaskExecutionStrategyId()));
        tasksMap.get(therapyId).add(perfusionSyringeTaskSimple);
      }
    }
    return tasksMap;
  }

  private TherapyDoctorReviewTaskDto buildTherapyTaskReminderDto(
      final TaskDto taskDto,
      final TaskTypeEnum taskType)
  {
    final TherapyDoctorReviewTaskDto reminderTask = new TherapyDoctorReviewTaskDto();
    reminderTask.setId(taskDto.getId());
    reminderTask.setDueTime(taskDto.getDueTime());
    reminderTask.setTaskType(taskType);
    reminderTask.setComment((String)taskDto.getVariables().get(DoctorReviewTaskDef.COMMENT.getName()));
    return reminderTask;
  }

  private TherapyTaskSimpleDto buildTherapyTaskSimpleDto(final TaskDto taskDto, final TaskTypeEnum taskType)
  {
    final TherapyTaskSimpleDto simpleTaskDto = new TherapyTaskSimpleDto();
    simpleTaskDto.setId(taskDto.getId());
    simpleTaskDto.setDueTime(taskDto.getDueTime());
    simpleTaskDto.setTaskType(taskType);
    return simpleTaskDto;
  }

  @Override
  public List<AdministrationPatientTaskDto> findAdministrationTasks(
      final @NonNull Collection<String> patientIds,
      final @NonNull Interval searchInterval,
      final int maxNumberOfTasks,
      final Set<AdministrationTypeEnum> types,
      final @NonNull Locale locale,
      final @NonNull DateTime when)
  {
    if (patientIds.isEmpty())
    {
      return Collections.emptyList();
    }

    final List<TaskDto> tasks = findAdministrationTasks(
        patientIds,
        searchInterval.getStart(),
        searchInterval.getEnd());

    final List<TaskDto> filteredTasks = tasks.stream()
        .filter(t -> isTaskOfType(t, types))
        .sorted(Comparator.comparing(AbstractTaskDto::getDueTime))
        .limit(maxNumberOfTasks)
        .collect(Collectors.toList());

    if (filteredTasks.isEmpty())
    {
      return Collections.emptyList();
    }

    final Set<String> patientsWithTasks = filteredTasks.stream()
        .map(t -> (String)t.getVariables().get(AdministrationTaskDef.PATIENT_ID.getName()))
        .collect(Collectors.toSet());

    final Map<String, PatientDisplayWithLocationDto> patientWithLocationMap = patientDataProvider.getPatientDisplayWithLocationMap(
        patientsWithTasks);

    final List<AdministrationPatientTaskDto> list = administrationTaskConverter.convertTasksToAdministrationPatientTasks(
        filteredTasks,
        patientWithLocationMap,
        locale,
        when);

    list.sort(Comparator.comparing(AdministrationPatientTaskDto::getPlannedTime));
    return list;
  }

  private boolean isTaskOfType(final TaskDto task, final Set<AdministrationTypeEnum> types)
  {
    if (types == null || types.isEmpty())
    {
      return true;
    }
    final String typeVar = (String)task.getVariables().get(AdministrationTaskDef.ADMINISTRATION_TYPE.getName());
    return types.contains(AdministrationTypeEnum.valueOf(typeVar));
  }

  @Override
  public List<CheckNewAllergiesTaskDto> findNewAllergiesTasks(final @NonNull String patientId)
  {
    final PartialList<TaskDto> tasks = processService.findTasks(
        null,
        null,
        null,
        false,
        null,
        null,
        Collections.singletonList(CheckNewAllergiesTaskDef.INSTANCE.buildKey(patientId)),
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    return tasks
        .stream()
        .map(t -> new CheckNewAllergiesTaskDto(t.getId(), getAllergiesFromTask(t)))
        .collect(Collectors.toList());
  }

  @Override
  public List<CheckMentalHealthMedsTaskDto> findNewCheckMentalHealthMedsTasks(final @NonNull String patientId)
  {
    final PartialList<TaskDto> tasks = processService.findTasks(
        null,
        null,
        null,
        false,
        null,
        null,
        Collections.singletonList(CheckMentalHealthMedsTaskDef.INSTANCE.buildKey(patientId)),
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    return tasks
        .stream()
        .map(t -> new CheckMentalHealthMedsTaskDto(t.getId()))
        .collect(Collectors.toList());
  }

  private Set<IdNameDto> getAllergiesFromTask(final TaskDto task)
  {
    return Sets.newHashSet(JsonUtil.fromJson(
        (String)task.getVariables().get(CheckNewAllergiesTaskDef.NEW_ALLERGIES.getName()),
        IdNameDto[].class));
  }
}