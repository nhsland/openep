package com.marand.thinkmed.medications.allergies;

import java.util.Collection;
import java.util.List;
import lombok.NonNull;

import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface AllergiesHandler
{
  void handleNewAllergies(@NonNull String patientId, @NonNull Collection<IdNameDto> newAllergies, @NonNull DateTime when);

  void handleNewAllergies(@NonNull String ehrId, String oldCompositionUId, String newCompositionUId);

  List<MedicationsWarningDto> getAllergyWarnings(@NonNull String patientId, @NonNull Collection<IdNameDto> allergies, @NonNull DateTime when);
}
