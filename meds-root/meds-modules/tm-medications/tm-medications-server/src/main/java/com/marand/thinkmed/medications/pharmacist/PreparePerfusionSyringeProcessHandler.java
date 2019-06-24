package com.marand.thinkmed.medications.pharmacist;

import lombok.NonNull;

import org.joda.time.DateTime;

/**
 * @author Klavdij Lapajne
 */
public interface PreparePerfusionSyringeProcessHandler
{
  void handlePreparationRequest(
      @NonNull String patientId,
      @NonNull String therapyCompositionUid,
      @NonNull String ehrOrderName,
      int numberOfSyringes,
      boolean urgent,
      @NonNull DateTime dueTime,
      @NonNull String userName,
      boolean printSystemLabel);

  void handleOrderCancellationMessage(
      @NonNull String patientId,
      @NonNull String therapyCompositionUid,
      @NonNull String ehrOrderName
  );

  void handleTherapyCancellationMessage(
      @NonNull String patientId,
      @NonNull String therapyCompositionUid,
      @NonNull String ehrOrderName);

  void handleTherapyCancellationMessage(@NonNull String patientId, @NonNull String originalTherapyId);

  void handleMedicationAdministrationMessage(
      @NonNull String patientId,
      @NonNull String therapyCompositionUid,
      @NonNull String ehrOrderName);
}