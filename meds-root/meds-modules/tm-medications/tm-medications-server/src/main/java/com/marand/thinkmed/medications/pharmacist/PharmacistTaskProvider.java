package com.marand.thinkmed.medications.pharmacist;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe.PerfusionSyringePreparationDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.SupplyDataForPharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringePatientTasksDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringeTaskSimpleDto;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import com.marand.thinkmed.process.dto.TaskDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Klavdij Lapajne
 */
public interface PharmacistTaskProvider
{
  List<TaskDto> findPharmacistReminderAndSupplyTasks(String patientId, Interval searchInterval);

  Opt<String> findPharmacistReviewTaskId(@NonNull String patientId);

  List<String> findTaskIds(
      final Interval searchInterval,
      final String assignee,
      final Set<String> patientIdsSet,
      final Set<TaskTypeEnum> taskTypes);

  List<PatientTaskDto> findPharmacistTasks(
      Interval searchInterval,
      @NonNull Collection<String> patientIds,
      Set<TaskTypeEnum> taskTypes);

  List<MedicationSupplyTaskDto> findSupplyTasks(
      @Nullable Interval searchInterval,
      @NonNull Collection<String> patientIds,
      Set<TaskTypeEnum> taskTypes,
      boolean closedTasksOnly,
      boolean includeUnverifiedDispenseTasks,
      DateTime when,
      Locale locale);

  List<MedicationSupplyTaskSimpleDto> findSupplySimpleTasksForTherapy(
      @Nullable final Interval searchInterval,
      final Set<String> patientIdsSet,
      final Set<TaskTypeEnum> taskTypes,
      final String originalTherapyId);

  List<TaskDto> findNurseSupplyTasksForTherapy(String patientId, String originalTherapyId);

  MedicationSupplyTaskSimpleDto getSupplySimpleTask(
      String taskId,
      DateTime when,
      Locale locale);

  SupplyDataForPharmacistReviewDto getSupplyDataForPharmacistReview(
      @NonNull String patientId,
      @NonNull String therapyCompositionUid);

  MedicationSupplyTaskSimpleDto getSupplySimpleTask(String taskId);

  List<PerfusionSyringePatientTasksDto> findPerfusionSyringeTasks(
      @NonNull Collection<String> patientIds,
      Interval searchInterval,
      @NonNull Set<TaskTypeEnum> taskTypes,
      boolean closedTasksOnly,
      @NonNull DateTime when,
      @NonNull Locale locale);

  Map<String, PerfusionSyringePreparationDto> getOriginalTherapyIdAndPerfusionSyringePreparationDtoMap(
      String patientId,
      boolean isUrgent,
      Set<String> originalTherapyIds,
      DateTime when,
      Locale locale);

  Map<String, String> getOriginalTherapyIdAndPerfusionSyringeTaskIdMap(String patientId, boolean isUrgent);

  PerfusionSyringeTaskSimpleDto getPerfusionSyringeTaskSimpleDto(String taskId, Locale locale);

  boolean therapyHasTasksClosedInInterval(
      @NonNull String patientId,
      @NonNull String originalTherapyId,
      @NonNull Set<TaskTypeEnum> taskTypeEnum,
      @NonNull Interval interval);

  DateTime getLastEditTimestampForPharmacistReview(String patientId);

  List<TaskDto> getTherapyTasks(TaskTypeEnum taskTypeEnum, String patientId, String originalTherapyId);
}
