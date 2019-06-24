package com.marand.thinkmed.patient;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lombok.NonNull;

import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface PatientDataProvider
{
  PatientDataForMedicationsDto getPatientData(@NonNull String patientId, String centralCaseId, @NonNull DateTime when);

  Map<String, PatientDisplayWithLocationDto> getPatientDisplayWithLocationMap(@NonNull Collection<String> careProviderIds);

  Set<String> getPatientIds(@NonNull Collection<String> careProviderIds);
}
