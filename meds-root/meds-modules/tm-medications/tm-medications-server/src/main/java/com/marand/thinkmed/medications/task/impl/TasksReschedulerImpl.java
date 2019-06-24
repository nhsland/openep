package com.marand.thinkmed.medications.task.impl;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;

import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.TasksRescheduler;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class TasksReschedulerImpl implements TasksRescheduler
{
  private MedicationsTasksHandler medicationsTasksHandler;
  private MedicationsTasksProvider medicationsTasksProvider;
  private AdministrationTaskConverter administrationTaskConverter;

  @Autowired
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setAdministrationTaskConverter(final AdministrationTaskConverter administrationTaskConverter)
  {
    this.administrationTaskConverter = administrationTaskConverter;
  }

  @Override
  public void rescheduleTherapyDoctorReviewTask(
      final @NonNull String taskId,
      final @NonNull DateTime newTime,
      final String comment)
  {
    medicationsTasksHandler.rescheduleTherapyDoctorReviewTask(taskId, newTime, comment);
  }

  @Override
  public void rescheduleIvToOralTask(
      final @NonNull String taskId,
      final @NonNull DateTime newTime)
  {
    medicationsTasksHandler.rescheduleIvToOralTask(taskId, newTime);
  }

  @Override
  public void rescheduleAdministrationTask(
      final @NonNull String patientId,
      final @NonNull String taskId,
      final @NonNull DateTime newTime)
  {
    final TaskDto task = medicationsTasksProvider.loadTask(taskId);

    rescheduleAdministrationTask(patientId, task, newTime);
  }

  private void rescheduleAdministrationTask(final @NonNull String patientId, final @NonNull TaskDto task, final @NonNull DateTime newTime)
  {
    final AdministrationTaskDto administrationTask = administrationTaskConverter.convertTaskToAdministrationTask(task);
    if (administrationTask.getGroupUUId() != null && administrationTask.getAdministrationTypeEnum() == AdministrationTypeEnum.START)
    {
      rescheduleAdministrationGroup(
          patientId,
          administrationTask.getTherapyId(),
          administrationTask.getGroupUUId(),
          null,
          newTime.getMillis() - administrationTask.getPlannedAdministrationTime().getMillis());
    }
    else
    {
      medicationsTasksHandler.rescheduleAdministrationTask(task.getId(), newTime);
    }
  }

  @Override
  public void rescheduleAdministrationTasks(final @NonNull String patientId, final @NonNull String taskId, final @NonNull DateTime newTime)
  {
    final AdministrationTaskDto task = medicationsTasksProvider.loadAdministrationTask(taskId);

    final List<TaskDto> laterTasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        Collections.singletonList(task.getTherapyId()),
        task.getPlannedAdministrationTime(),
        null,
        null,
        false);

    final long timeDiff = newTime.getMillis() - task.getPlannedAdministrationTime().getMillis();

    medicationsTasksHandler.rescheduleAdministrationTask(taskId, newTime);

    laterTasks.forEach(laterTask -> medicationsTasksHandler.rescheduleAdministrationTask(
        laterTask.getId(),
        new DateTime(laterTask.getDueTime()).plusMillis(Math.toIntExact(timeDiff))));
  }

  @Override
  public void rescheduleAdministrationGroup(
      final @NonNull String patientId,
      final @NonNull String therapyId,
      final @NonNull String groupUUId,
      final String excludeTaskId,
      final long timeDiff)
  {
    final List<TaskDto> groupTasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        Collections.singletonList(therapyId),
        null,
        null,
        groupUUId,
        false);

    //noinspection NumericCastThatLosesPrecision
    groupTasks
        .stream()
        .filter(t -> excludeTaskId == null || !excludeTaskId.equals(t.getId()))
        .forEach(task -> medicationsTasksHandler.rescheduleAdministrationTask(
            task.getId(),
            new DateTime(task.getDueTime()).plusMillis((int)timeDiff)));
  }
}
