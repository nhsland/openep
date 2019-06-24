package com.marand.thinkmed.medications.warnings;

import java.util.List;

import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.service.dto.WarningScreenMedicationDto;
import lombok.NonNull;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
public interface WarningsPlugin
{
  void reloadCache();

  List<MedicationsWarningDto> findMedicationWarnings(
      @NonNull DateTime dateOfBirth,
      Double patientWeightInKg,
      Double bsaInM2,
      @NonNull Gender gender,
      @NonNull List<IdNameDto> diseaseTypeValues,
      @NonNull List<IdNameDto> allergiesExternalValues,
      @NonNull List<WarningScreenMedicationDto> medicationSummaries,
      @NonNull DateTime when);

  boolean requiresDiseaseCodesTranslation();

  String getExternalSystemName();
}
