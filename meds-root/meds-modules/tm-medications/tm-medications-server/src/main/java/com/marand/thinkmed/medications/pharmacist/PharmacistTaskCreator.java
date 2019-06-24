package com.marand.thinkmed.medications.pharmacist;

import java.util.Locale;
import lombok.NonNull;

import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.PrescriptionChangeTypeEnum;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface PharmacistTaskCreator
{
  void createPharmacistReminderTask(
      String patientId,
      String compositionUid,
      DateTime reminderDate,
      String reminderNote,
      Locale locale);

  void createPharmacistReviewTask(
      @NonNull String patientId,
      DateTime dueTime,
      String lastEditorName,
      DateTime lastEditTime,
      @NonNull PrescriptionChangeTypeEnum changeType,
      @NonNull PharmacistReviewTaskStatusEnum status);

  void updatePharmacistReviewTask(
      @NonNull String taskId,
      DateTime dueTime,
      String lastEditorName,
      DateTime lastEditTime);
}
