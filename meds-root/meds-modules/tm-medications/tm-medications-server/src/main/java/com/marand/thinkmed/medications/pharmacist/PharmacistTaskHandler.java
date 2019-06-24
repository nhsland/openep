package com.marand.thinkmed.medications.pharmacist;

import java.util.List;
import lombok.NonNull;

import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.PrescriptionChangeTypeEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import org.joda.time.DateTime;

/**
 * @author Klavdij Lapajne
 */
public interface PharmacistTaskHandler
{
  void deleteSupplyTask(final List<String> taskIds, final String userId);

  void dismissSupplyTask(List<String> taskIds, String userId);

  void deleteNurseSupplyTask(String patientId, String taskId, String userId);

  void confirmSupplyReminderTask(
      String taskId,
      String therapyIdAtConfirmation,
      MedicationSupplyTypeEnum supplyTypeEnum,
      Integer supplyInDays,
      String userId,
      String comment);

  void editSupplyReminderTask(
      String taskId,
      MedicationSupplyTypeEnum supplyTypeEnum,
      Integer supplyInDays,
      String comment,
      DateTime when);

  void confirmSupplyReviewTask(
      String patientId,
      String taskId,
      String therapyIdAtConfirmation,
      boolean alreadyDispensed,
      boolean createSupplyReminder,
      MedicationSupplyTypeEnum supplyTypeEnum,
      Integer supplyInDays,
      String comment,
      String userId,
      DateTime when);

  void handleReviewTaskOnTherapiesChange(
      @NonNull String patientId,
      DateTime hospitalizationStart,
      @NonNull DateTime when,
      String lastEditorName,
      DateTime lastEditTime,
      PrescriptionChangeTypeEnum changeType,
      @NonNull PharmacistReviewTaskStatusEnum status);

  void confirmPharmacistDispenseTask(
      String patientId,
      String taskId,
      String therapyIdAtConfirmation,
      TherapyAssigneeEnum requesterRole,
      SupplyRequestStatus supplyRequestStatus,
      String userId);

  void deletePerfusionSyringeTask(String taskId, String userId);

  void undoPerfusionSyringeTask(String taskId, String userId);

  void confirmPerfusionSyringeTasks(List<String> taskIds, String userId);

  void setDispenseTaskPrintedTimestamp(String taskId, DateTime requestTimestamp);

  void editPerfusionSyringeTask(
      @NonNull String taskId,
      @NonNull Integer numberOfSyringes,
      boolean isUrgent,
      @NonNull DateTime dueDate,
      boolean printSystemLabel);

  void confirmPerfusionSyringeTasksForTherapy(String patientId, String userId, TaskTypeEnum taskTypeEnum, String originalTherapyId);
}
