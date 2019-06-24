package com.marand.thinkmed.medications.task;

import lombok.NonNull;

import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */

public interface TasksRescheduler
{
  void rescheduleAdministrationTask(@NonNull String patientId, @NonNull String taskId, @NonNull DateTime newTime);

  void rescheduleTherapyDoctorReviewTask(
      final @NonNull String taskId,
      final @NonNull DateTime newTime,
      final String comment);

  void rescheduleIvToOralTask(
      final @NonNull String taskId,
      final @NonNull DateTime newTime);

  void rescheduleAdministrationTasks(@NonNull String patientId, @NonNull String taskId, @NonNull DateTime newTime);

  void rescheduleAdministrationGroup(
      @NonNull String patientId,
      @NonNull String therapyId,
      @NonNull String groupUUId,
      String excludeTaskId,
      long timeDiff);
}
