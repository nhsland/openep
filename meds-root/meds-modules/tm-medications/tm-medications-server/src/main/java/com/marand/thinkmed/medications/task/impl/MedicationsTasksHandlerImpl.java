package com.marand.thinkmed.medications.task.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.marand.ispek.bpm.TaskCompletedType;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Pair;
import com.marand.maf.core.PartialList;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.administration.impl.AdministrationTaskCreator;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskHandler;
import com.marand.thinkmed.medications.pharmacist.PreparePerfusionSyringeProcessHandler;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.CheckMentalHealthMedsTaskDef;
import com.marand.thinkmed.medications.task.CheckNewAllergiesTaskDef;
import com.marand.thinkmed.medications.task.DoctorReviewTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.TherapyTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskUtils;
import com.marand.thinkmed.process.definition.TaskVariable;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nejc Korasa
 */
@Component
public class MedicationsTasksHandlerImpl implements MedicationsTasksHandler
{
  private ProcessService processService;
  private MedicationsTasksProvider medicationsTasksProvider;
  private PharmacistTaskHandler pharmacistTaskHandler;
  private PreparePerfusionSyringeProcessHandler preparePerfusionSyringeProcessHandler;
  private AdministrationTaskCreator administrationTaskCreator;
  private AdministrationTaskConverter administrationTaskConverter;

  @Autowired
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setPharmacistTaskHandler(final PharmacistTaskHandler pharmacistTaskHandler)
  {
    this.pharmacistTaskHandler = pharmacistTaskHandler;
  }

  @Autowired
  public void setPreparePerfusionSyringeProcessHandler(final PreparePerfusionSyringeProcessHandler preparePerfusionSyringeProcessHandler)
  {
    this.preparePerfusionSyringeProcessHandler = preparePerfusionSyringeProcessHandler;
  }

  @Autowired
  public void setAdministrationTaskCreator(final AdministrationTaskCreator administrationTaskCreator)
  {
    this.administrationTaskCreator = administrationTaskCreator;
  }

  @Autowired
  public void setAdministrationTaskConverter(final AdministrationTaskConverter administrationTaskConverter)
  {
    this.administrationTaskConverter = administrationTaskConverter;
  }

  @Override
  public void deleteTherapyTasksOfType(
      final String patientId,
      final Set<TaskTypeEnum> taskTypes,
      final String userId,
      final String originalTherapyId)
  {
    deleteTasksOfType(patientId, taskTypes, userId, originalTherapyId);
  }

  private void deleteTasksOfType(
      final String patientId,
      final Set<TaskTypeEnum> taskTypes,
      final String userId,
      final String originalTherapyId)
  {
    //noinspection TooBroadScope
    final EnumSet<TaskTypeEnum> pharmacistReviewProcessTaskTypes = EnumSet.of(TaskTypeEnum.PHARMACIST_REVIEW);
    final EnumSet<TaskTypeEnum> supplyTaskTypes = TaskTypeEnum.SUPPLY_TASKS_SET;
    final EnumSet<TaskTypeEnum> perfusionSyringeProcessTaskTypes = TaskTypeEnum.PERFUSION_SYRINGE_TASKS_SET;

    //noinspection TooBroadScope
    final EnumSet<TaskTypeEnum> tasksWithoutProcessTypes = EnumSet.of(
        TaskTypeEnum.PHARMACIST_REMINDER,
        TaskTypeEnum.SWITCH_TO_ORAL,
        TaskTypeEnum.DOCTOR_REVIEW,
        TaskTypeEnum.INFUSION_BAG_CHANGE_TASK);

    final List<String> patientIdKeysForTaskTypes = TherapyTaskUtils.getPatientIdKeysForTaskTypes(
        new HashSet<>(Collections.singletonList(patientId)),
        taskTypes);

    //noinspection unchecked
    final PartialList<TaskDto> taskDtoList =
        originalTherapyId != null
        ? processService.findTasks(
            null,
            null,
            null,
            false,
            null,
            null,
            patientIdKeysForTaskTypes,
            EnumSet.of(TaskDetailsEnum.VARIABLES),
            Pair.of(TherapyTaskDef.ORIGINAL_THERAPY_ID, originalTherapyId))
        : processService.findTasks(
            null,
            null,
            null,
            false,
            null,
            null,
            patientIdKeysForTaskTypes,
            EnumSet.of(TaskDetailsEnum.VARIABLES));

    if (!taskDtoList.isEmpty())
    {
      final List<String> supplyTaskIds = new ArrayList<>();
      final List<String> pharmacistReviewProcessTaskIds = new ArrayList<>();
      final List<String> perfusionSyringeProcessTaskIds = new ArrayList<>();
      final List<String> tasksWithoutProcessIds = new ArrayList<>();

      for (final TaskDto taskDto : taskDtoList)
      {
        final TaskTypeEnum taskTypeEnum = TaskTypeEnum.getByName(taskDto.getTaskExecutionStrategyId());
        final String taskId = taskDto.getId();

        if (supplyTaskTypes.contains(taskTypeEnum))
        {
          supplyTaskIds.add(taskId);
        }
        else if (pharmacistReviewProcessTaskTypes.contains(taskTypeEnum))
        {
          pharmacistReviewProcessTaskIds.add(taskId);
        }
        else if (perfusionSyringeProcessTaskTypes.contains(taskTypeEnum))
        {
          perfusionSyringeProcessTaskIds.add(taskId);
        }
        else if (tasksWithoutProcessTypes.contains(taskTypeEnum))
        {
          tasksWithoutProcessIds.add(taskId);
        }
        else
        {
          throw new UnsupportedOperationException(
              "this method does not support task of type: " + taskDto.getTaskExecutionStrategyId());
        }
      }

      deleteSupplyProcessTasks(supplyTaskIds, userId);
      deletePharmacistReviewProcessTasks(pharmacistReviewProcessTaskIds);
      deletePerfusionSyringeProcessTasks(perfusionSyringeProcessTaskIds, patientId, originalTherapyId);
      deleteTasksWithoutProcess(tasksWithoutProcessIds);
    }
  }

  private void deleteSupplyProcessTasks(final List<String> supplyTaskIds, final String userId)
  {
    pharmacistTaskHandler.deleteSupplyTask(supplyTaskIds, userId);
  }

  private void deletePharmacistReviewProcessTasks(final List<String> pharmacistReviewTaskIds)
  {
    //noinspection ToArrayCallWithZeroLengthArrayArgument
    processService.completeTasks(pharmacistReviewTaskIds.toArray(new String[pharmacistReviewTaskIds.size()]));
    for (final String taskId : pharmacistReviewTaskIds)
    {
      processService.setTaskDeleteReason(taskId, TaskCompletedType.DELETED.getBpmName());
    }
  }

  private void deletePerfusionSyringeProcessTasks(
      final List<String> perfusionSyringeProcessTaskIds,
      final String patientId,
      final String originalTherapyId)
  {
    if (!perfusionSyringeProcessTaskIds.isEmpty())
    {
      preparePerfusionSyringeProcessHandler.handleTherapyCancellationMessage(patientId, originalTherapyId);
    }
  }

  private void deleteTasksWithoutProcess(final List<String> tasksWithoutProcessIds)
  {
    processService.deleteTasks(tasksWithoutProcessIds);
  }

  @Override
  public void associateTaskWithAdministration(final String taskId, final String administrationCompositionUid)
  {
    processService.setVariable(
        taskId,
        AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName(),
        administrationCompositionUid);
  }

  @Override
  public void rescheduleTherapyDoctorReviewTask(final @NonNull String taskId, final @NonNull DateTime newTime, final String comment)
  {
    processService.setDueDate(taskId, newTime);
    processService.setVariable(
        taskId,
        DoctorReviewTaskDef.COMMENT.getName(),
        comment);
  }

  @Override
  public void rescheduleIvToOralTask(final @NonNull String taskId, final @NonNull DateTime newTime)
  {
    processService.setDueDate(taskId, newTime);
  }

  @Override
  public void rescheduleAdministrationTask(final @NonNull String taskId, final @NonNull DateTime newTime)
  {
    processService.setDueDate(taskId, newTime);
  }

  @Override
  @Transactional
  public void setAdministrationTitratedDose(
      final @NonNull String patientId,
      final @NonNull String latestTherapyId,
      final @NonNull String taskId,
      final @NonNull TherapyDoseDto therapyDose,
      final String doctorsComment,
      final @NonNull DateTime plannedAdministrationTime,
      final DateTime until)
  {
    processService.setDueDate(taskId, plannedAdministrationTime);

    final List<Pair<TaskVariable, Object>> taskVariables = administrationTaskCreator.getDoseTaskVariables(therapyDose);
    final Map<String, Object> variables = taskVariables.stream()
        .collect(Collectors.toMap(t -> t.getFirst().getName(), Pair::getSecond));

    if (doctorsComment != null)
    {
      variables.put(AdministrationTaskDef.DOCTORS_COMMENT.getName(), doctorsComment);
    }

    processService.setVariables(taskId, variables);

    if (until != null)
    {
      final List<TaskDto> administrationTasks = medicationsTasksProvider.findAdministrationTasks(
          patientId,
          Collections.singletonList(latestTherapyId),
          plannedAdministrationTime,
          until.plusMillis(1),
          null,
          false);

      administrationTasks.forEach(t -> processService.setVariables(t.getId(), variables));
    }
  }

  @Override
  public void deleteTask(final @NonNull String taskId, final String comment)
  {
    if (comment != null)
    {
      processService.setVariable(taskId, AdministrationTaskDef.DELETE_COMMENT.getName(), comment);
    }
    processService.deleteTasks(Collections.singletonList(taskId));
  }

  @Override
  public void deleteAdministrationTasks(
      final @NonNull String patientId,
      final @NonNull String therapyId,
      final String groupUUId,
      final List<AdministrationTypeEnum> types)
  {
    final List<String> therapyIds = Collections.singletonList(therapyId);
    final List<String> taskIds =
        medicationsTasksProvider.findAdministrationTasks(patientId, therapyIds, null, null, groupUUId, false)
            .stream()
            .filter(task -> task.getCompletedType() != TaskCompletedType.DELETED)
            .map(task -> administrationTaskConverter.convertTaskToAdministrationTask(task))
            .filter(task -> types.contains(task.getAdministrationTypeEnum()))
            .map(AdministrationTaskDto::getTaskId)
            .collect(Collectors.toList());

    processService.deleteTasks(taskIds);
  }

  @Override
  public void createCheckNewAllergiesTask(
      final @NonNull String patientId,
      final @NonNull Collection<IdNameDto> allergies,
      final @NonNull DateTime when)
  {
    final String description = new StringBuilder()
        .append("New allergies: ")
        .append(allergies.stream().map(IdNameDto::getName).collect(Collectors.joining(", ")))
        .toString();

    final NewTaskRequestDto taskRequest = new NewTaskRequestDto(
        CheckNewAllergiesTaskDef.INSTANCE,
        CheckNewAllergiesTaskDef.INSTANCE.buildKey(patientId),
        description,
        description,
        TherapyAssigneeEnum.DOCTOR.name(),
        when,
        null,
        Pair.of(CheckNewAllergiesTaskDef.NEW_ALLERGIES, JsonUtil.toJson(allergies)),
        Pair.of(CheckNewAllergiesTaskDef.PATIENT_ID, patientId));

    processService.createTasks(taskRequest);
  }

  @Override
  public void createCheckMentalHealthMedsTask(final @NonNull String patientId, final @NonNull DateTime when)
  {
    final String description = "New mental health document";

    final NewTaskRequestDto taskRequest = new NewTaskRequestDto(
        CheckMentalHealthMedsTaskDef.INSTANCE,
        CheckMentalHealthMedsTaskDef.INSTANCE.buildKey(patientId),
        description,
        description,
        TherapyAssigneeEnum.DOCTOR.name(),
        when,
        null,
        Pair.of(CheckMentalHealthMedsTaskDef.PATIENT_ID, patientId));

    processService.createTasks(taskRequest);
  }


  @Override
  public String undoCompleteTask(final String taskId)
  {
    //activiti does not support undo, we will delete completed task and create a copy
    final TaskDto task = processService.loadTask(taskId, true);
    final NewTaskRequestDto taskRequest = administrationTaskCreator.createTaskRequestFromTaskDto(task);
    processService.setTaskDeleteReason(taskId, TaskCompletedType.DELETED.getBpmName());

    final Optional<Pair<TaskVariable, ?>> administrationIdVariable = taskRequest.getVariables()
        .stream()
        .filter(taskVariablePair -> AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName().equals(taskVariablePair.getFirst().getName()))
        .findAny();

    administrationIdVariable.ifPresent(taskVariablePair -> taskRequest.getVariables().remove(taskVariablePair));

    return processService.createTasks(taskRequest)[0].getId();
  }

  @Override
  public void setAdministrationDoctorsComment(final @NonNull String taskId, final String doctorsComment)
  {
    processService.setVariable(taskId, AdministrationTaskDef.DOCTORS_COMMENT.getName(), doctorsComment);
  }

}
