package com.marand.thinkmed.medications.batch;

import java.util.List;

import lombok.NonNull;

import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface TherapyBatchActionHandler
{
  void abortAllTherapies(@NonNull String patientId, @NonNull DateTime when, String stopReason);

  void suspendAllTherapies(@NonNull String patientId, @NonNull DateTime when, String suspendReason);

  void suspendAllTherapiesOnTemporaryLeave(@NonNull String patientId, @NonNull DateTime when);

  List<String> reissueAllTherapiesOnReturnFromTemporaryLeave(@NonNull String patientId, @NonNull DateTime when);
}
