package com.marand.thinkmed.patient.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.marand.thinkmed.api.PatientImageUtils;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.witnessing.WitnessingHandler;
import com.marand.thinkmed.patient.PatientDataProvider;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */
@Component
public class PatientDataProviderImpl implements PatientDataProvider
{
  private MedicationsConnector medicationsConnector;
  private WitnessingHandler witnessingHandler;

  @Autowired
  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Autowired
  public void setWitnessingHandler(final WitnessingHandler witnessingHandler)
  {
    this.witnessingHandler = witnessingHandler;
  }

  @Override
  public PatientDataForMedicationsDto getPatientData(
      final @NonNull String patientId,
      final String centralCaseId,
      final @NonNull DateTime when)
  {
    final PatientDataForMedicationsDto patientData = medicationsConnector.getPatientData(patientId, centralCaseId, when);
    patientData.setWitnessingRequired(witnessingHandler.isPatientWitnessingRequired(patientData));
    return patientData;
  }

  @Override
  public Map<String, PatientDisplayWithLocationDto> getPatientDisplayWithLocationMap(
      final @NonNull Collection<String> patientIds)
  {
    if (patientIds.isEmpty())
    {
      return Collections.emptyMap();
    }

    final List<PatientDisplayWithLocationDto> patientDisplaysWithLocation = medicationsConnector.getPatientDisplaysWithLocation(
        null,
        patientIds);

    return patientDisplaysWithLocation
        .stream()
        .filter(p -> p.getPatientDisplayDto() != null)
        .peek(p -> p.getPatientDisplayDto().setPatientImagePath(getPatientImagePath(p)))
        .collect(Collectors.toMap(p -> p.getPatientDisplayDto().getId(), p -> p));
  }

  @NotNull
  private String getPatientImagePath(@NotNull final PatientDisplayWithLocationDto patient)
  {
    final Gender gender = patient.getPatientDisplayDto().getGender();
    if (gender == Gender.MALE || gender == Gender.FEMALE)
    {
      return PatientImageUtils.getPatientImagePath(gender, patient.getPatientDisplayDto().getBirthDate());
    }
    return "/images/icons/patient_anonymous_48.png";
  }

  @Override
  public Set<String> getPatientIds(final @NonNull Collection<String> careProviderIds)
  {
    final List<PatientDisplayWithLocationDto> patientDisplaysWithLocation = medicationsConnector.getPatientDisplaysWithLocation(
        careProviderIds,
        null);

    return patientDisplaysWithLocation.stream()
        .map(p -> p.getPatientDisplayDto().getId())
        .collect(Collectors.toSet());
  }
}
