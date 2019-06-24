package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.Collection;
import java.util.List;

import com.marand.thinkmed.medications.connector.data.object.EncounterDto;
import lombok.NonNull;

/**
 * @author Mitja Lapajne
 */
public interface EncounterProvider
{
  EncounterDto getPatientLatestEncounter(@NonNull String patientId);

  EncounterDto getEncounter(@NonNull String patientId, @NonNull String encounterId);

  List<EncounterDto> getPatientsActiveEncounters(@NonNull Collection<String> patientsIds);
}
