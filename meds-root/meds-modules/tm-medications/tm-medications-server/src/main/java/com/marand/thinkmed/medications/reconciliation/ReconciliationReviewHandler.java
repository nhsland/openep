package com.marand.thinkmed.medications.reconciliation;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.medications.task.ReconciliationReviewTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskUtils;
import com.marand.thinkmed.process.dto.AbstractTaskDto;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.user.RequestUser;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReconciliationReviewHandler
{
  private ProcessService processService;
  private RequestDateTimeHolder requestDateTimeHolder;

  @Autowired
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Autowired
  public void setRequestDateTimeHolder(final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  public void updateReviewTask(final @NonNull String patientId, final @NonNull TaskTypeEnum taskType)
  {
    Preconditions.checkArgument(
        EnumSet.of(TaskTypeEnum.ADMISSION_REVIEW_TASK, TaskTypeEnum.DISCHARGE_REVIEW_TASK).contains(taskType));

    final DateTime lastEditTime = requestDateTimeHolder.getRequestTimestamp();
    final String lastEditorName = RequestUser.getFullName();

    final List<TaskDto> tasks = findReviewTasks(patientId, taskType);
    if (tasks.isEmpty())
    {
      createReviewTask(patientId, taskType);
    }
    else
    {
      final String taskId = tasks.get(0).getId();
      final Map<String, Object> variables = new HashMap<>();
      variables.put(ReconciliationReviewTaskDef.LAST_EDITOR_NAME.getName(), lastEditorName);
      variables.put(ReconciliationReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS.getName(), lastEditTime.getMillis());
      processService.setDueDate(taskId, lastEditTime);
      processService.setVariables(taskId, variables);
    }
  }

  public void completeReviewTask(final @NonNull String patientId, final @NonNull TaskTypeEnum taskType)
  {
    Preconditions.checkArgument(
        EnumSet.of(TaskTypeEnum.ADMISSION_REVIEW_TASK, TaskTypeEnum.DISCHARGE_REVIEW_TASK).contains(taskType));

    findReviewTasks(patientId, taskType)
        .forEach(t -> processService.completeTasks(t.getId()));
  }

  public void deleteReviewTask(final @NonNull String patientId, final @NonNull TaskTypeEnum taskType)
  {
    Preconditions.checkArgument(
        EnumSet.of(TaskTypeEnum.ADMISSION_REVIEW_TASK, TaskTypeEnum.DISCHARGE_REVIEW_TASK).contains(taskType));

    final List<String> taskIds = findReviewTasks(patientId, taskType).stream()
        .map(AbstractTaskDto::getId)
        .collect(Collectors.toList());
    processService.deleteTasks(taskIds);
  }

  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  public boolean reviewTaskExists(final @NonNull String patientId, final @NonNull TaskTypeEnum taskType)
  {
    return !findReviewTasks(patientId, taskType).isEmpty();
  }

  private void createReviewTask(final @NonNull String patientId, final @NonNull TaskTypeEnum taskType)
  {
    processService.createTasks(buildAdmissionReviewTaskRequest(patientId, taskType));
  }

  private List<TaskDto> findReviewTasks(final String patientId, final TaskTypeEnum taskType)
  {
    //noinspection unchecked
    return processService.findTasks(
        TherapyAssigneeEnum.PHARMACIST.name(),
        null,
        null,
        false,
        null,
        null,
        TherapyTaskUtils.getPatientIdKeysForTaskTypes(
            Collections.singleton(patientId),
            EnumSet.of(taskType)),
        EnumSet.of(TaskDetailsEnum.VARIABLES));
  }

  private NewTaskRequestDto buildAdmissionReviewTaskRequest(final String patientId, final @NonNull TaskTypeEnum taskType)
  {
    return new NewTaskRequestDto(
        taskType.getTaskDef(),
        taskType.getTaskDef().buildKey(patientId),
        taskType.getName(),
        taskType.getName(),
        TherapyAssigneeEnum.PHARMACIST.name(),
        requestDateTimeHolder.getRequestTimestamp(),
        null,
        Pair.of(MedsTaskDef.PATIENT_ID, patientId),
        Pair.of(ReconciliationReviewTaskDef.LAST_EDITOR_NAME, RequestUser.getFullName()),
        Pair.of(
            ReconciliationReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS,
            requestDateTimeHolder.getRequestTimestamp().getMillis())
    );
  }
}
