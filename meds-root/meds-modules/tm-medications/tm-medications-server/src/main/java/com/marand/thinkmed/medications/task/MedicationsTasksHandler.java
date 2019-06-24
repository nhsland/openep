package com.marand.thinkmed.medications.task;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.NonNull;

import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface MedicationsTasksHandler
{
  void deleteTherapyTasksOfType(String patientId, Set<TaskTypeEnum> taskTypes, String userId, String originalTherapyId);

  void associateTaskWithAdministration(String taskId, String administrationCompositionUid);

  void rescheduleTherapyDoctorReviewTask(@NonNull String taskId, @NonNull DateTime newTime, String comment);

  void rescheduleIvToOralTask(@NonNull String taskId, @NonNull DateTime newTime);

  void rescheduleAdministrationTask(@NonNull String taskId, @NonNull DateTime newTime);

  void setAdministrationTitratedDose(
      @NonNull String patientId,
      @NonNull String latestTherapyId,
      @NonNull String taskId,
      @NonNull TherapyDoseDto therapyDose,
      String doctorsComment,
      @NonNull DateTime plannedAdministrationTime,
      DateTime until);

  void deleteTask(@NonNull String taskId, String comment);

  void deleteAdministrationTasks(
      @NonNull String patientId,
      @NonNull String therapyId,
      String groupUUId,
      List<AdministrationTypeEnum> types);

  void createCheckNewAllergiesTask(
      @NonNull String patientId,
      @NonNull Collection<IdNameDto> allergies,
      @NonNull DateTime when);

  void createCheckMentalHealthMedsTask(@NonNull String patientId, @NonNull DateTime when);

  String undoCompleteTask(String taskId);

  void setAdministrationDoctorsComment(@NonNull String taskId, String doctorsComment);
}
