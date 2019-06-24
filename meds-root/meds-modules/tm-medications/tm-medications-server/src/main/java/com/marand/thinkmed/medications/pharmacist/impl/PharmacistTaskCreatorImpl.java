package com.marand.thinkmed.medications.pharmacist.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.marand.maf.core.Pair;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.PrescriptionChangeTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskCreator;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.medications.task.PharmacistReminderTaskDef;
import com.marand.thinkmed.medications.task.PharmacistReviewTaskDef;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.service.ProcessService;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Klavdij Lapajne
 */
@Component
public class PharmacistTaskCreatorImpl implements PharmacistTaskCreator
{
  private ProcessService processService;

  @Autowired
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Override
  public void createPharmacistReminderTask(
      final String patientId,
      final String compositionUid,
      final DateTime reminderDate,
      final String reminderNote,
      final Locale locale)
  {
    final NewTaskRequestDto taskRequest = createPharmacistReminderTaskRequest(
        patientId,
        compositionUid,
        reminderDate,
        reminderNote,
        locale);

    processService.createTasks(taskRequest);
  }

  @Override
  public void createPharmacistReviewTask(
      final @NonNull String patientId,
      final DateTime dueTime,
      final String lastEditorName,
      final DateTime lastEditTime,
      final @NonNull PrescriptionChangeTypeEnum changeType,
      final @NonNull PharmacistReviewTaskStatusEnum status)
  {
    final NewTaskRequestDto taskRequest = createPharmacistReviewTaskRequest(
        patientId,
        dueTime,
        lastEditorName,
        lastEditTime,
        changeType,
        status);

    processService.createTasks(taskRequest);
  }

  @Override
  public void updatePharmacistReviewTask(
      final @NonNull String taskId,
      final DateTime dueTime,
      final String lastEditorName,
      final DateTime lastEditTime)
  {
    final Map<String, Object> variables = new HashMap<>();
    variables.put(PharmacistReviewTaskDef.LAST_EDITOR_NAME.getName(), lastEditorName);
    variables.put(PharmacistReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS.getName(), lastEditTime.getMillis());
    processService.setDueDate(taskId, dueTime);
    processService.setVariables(taskId, variables);
  }

  private NewTaskRequestDto createPharmacistReviewTaskRequest(
      final String patientId,
      final DateTime dueTime,
      final String lastEditorName,
      final DateTime lastEditTime,
      final PrescriptionChangeTypeEnum changeType,
      final PharmacistReviewTaskStatusEnum status)
  {
    return new NewTaskRequestDto(
        PharmacistReviewTaskDef.INSTANCE,
        PharmacistReviewTaskDef.INSTANCE.buildKey(String.valueOf(patientId)),
        "Pharmacist review",
        "Pharmacist review",
        TherapyAssigneeEnum.PHARMACIST.name(),
        dueTime,
        null,
        Pair.of(MedsTaskDef.PATIENT_ID, patientId),
        Pair.of(PharmacistReviewTaskDef.LAST_EDITOR_NAME, lastEditorName),
        Pair.of(PharmacistReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS, lastEditTime.getMillis()),
        Pair.of(PharmacistReviewTaskDef.CHANGE_TYPE, changeType.name()),
        Pair.of(PharmacistReviewTaskDef.STATUS, status.name()));
  }

  private NewTaskRequestDto createPharmacistReminderTaskRequest(
      final String patientId,
      final String pharmacistReviewCompositionUid,
      final DateTime reminderDate,
      final String reminderNote,
      final Locale locale)
  {
    return new NewTaskRequestDto(
        PharmacistReminderTaskDef.INSTANCE,
        PharmacistReminderTaskDef.INSTANCE.buildKey(String.valueOf(patientId)),
        "Pharmacist reminder " + reminderDate.toString(DateTimeFormatters.shortDate(locale)),
        "Pharmacist reminder " + reminderDate.toString(DateTimeFormatters.shortDate(locale)),
        TherapyAssigneeEnum.PHARMACIST.name(),
        reminderDate,
        null,
        Pair.of(MedsTaskDef.PATIENT_ID, patientId),
        Pair.of(PharmacistReminderTaskDef.PHARMACIST_REVIEW_ID, pharmacistReviewCompositionUid),
        Pair.of(PharmacistReminderTaskDef.COMMENT, reminderNote));
  }
}
