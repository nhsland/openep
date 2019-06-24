package com.marand.thinkmed.medications.task;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.allergies.CheckNewAllergiesTaskDto;
import com.marand.thinkmed.medications.dto.mentalHealth.CheckMentalHealthMedsTaskDto;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import com.marand.thinkmed.process.dto.TaskDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Mitja Lapajne
 */
public interface MedicationsTasksProvider
{
  TaskDto loadTask(@NonNull String taskId);

  AdministrationTaskDto loadAdministrationTask(@NonNull String taskId);

  TaskDto loadNextAdministrationTask(String patientId, DateTime fromWhen);

  List<AdministrationTaskDto> findAdministrationTasks(
      String patientId,
      Collection<String> therapyIds,
      Interval searchInterval,
      boolean findHistoric);

  List<TaskDto> findAdministrationTasks(
      String patientId,
      Collection<String> therapyIds,
      DateTime taskDueAfter,
      DateTime taskDueBefore,
      String groupUUId,
      boolean findHistoric);

  List<TaskDto> findAdministrationTasks(Collection<String> patientIds, DateTime taskDueAfter, DateTime taskDueBefore);

  Opt<AdministrationTaskDto> findLastAdministrationTaskForTherapy(
      @NonNull String patientId,
      @NonNull String therapyId,
      Interval searchInterval,
      boolean findHistoric);

  Opt<DateTime> findLastAdministrationTaskTimeForTherapy(
      @NonNull String patientId,
      @NonNull String therapyId,
      Interval searchInterval,
      boolean findHistoric);

  Map<String, DateTime> findLastAdministrationTaskTimesForTherapies(
      Collection<String> patientIds,
      DateTime fromTime,
      boolean findHistoric);

  Map<String, List<TherapyTaskSimpleDto>> findSimpleTasksForTherapies(
      String patientId,
      Collection<String> therapyIds,
      DateTime when);

  List<AdministrationPatientTaskDto> findAdministrationTasks(
      @NonNull Collection<String> patientIds,
      @NonNull Interval searchInterval,
      int maxNumberOfTasks,
      Set<AdministrationTypeEnum> types,
      @NonNull Locale locale,
      @NonNull DateTime when);

  List<CheckNewAllergiesTaskDto> findNewAllergiesTasks(final @NonNull String patientId);

  List<CheckMentalHealthMedsTaskDto> findNewCheckMentalHealthMedsTasks(final @NonNull String patientId);
}
