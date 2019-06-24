package com.marand.thinkmed.medications.connector.impl.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.AllergiesStatus;
import com.marand.thinkmed.medications.connector.data.object.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Mitja Lapajne
 */
public class DemoMedicationsConnector implements MedicationsConnector
{
  @Override
  public PatientDataForMedicationsDto getPatientData(
      final @NonNull String patientId,
      final String centralCaseId,
      final @NonNull DateTime when)
  {
    final List<IdNameDto> allergies = new ArrayList<>();
    allergies.add(new IdNameDto("91936005", "Penicillin"));

    final MedicationsCentralCaseDto centralCaseDto = new MedicationsCentralCaseDto();
    centralCaseDto.setOutpatient(false);
    centralCaseDto.setCentralCaseId("1");
    centralCaseDto.setEpisodeId("1");
    final NamedExternalDto careProvider = new NamedExternalDto("1", "KOOKIT");
    centralCaseDto.setCareProvider(careProvider);
    centralCaseDto.setCentralCaseEffective(Intervals.infiniteFrom(new DateTime(2014, 11, 20, 12, 0)));

    return new PatientDataForMedicationsDto(
        new DateTime(1984, 5, 3, 0, 0),
        "John Smith",
        52.0,
        165.0,
        Gender.FEMALE,
        Collections.emptyList(),
        AllergiesStatus.PRESENT,
        allergies,
        centralCaseDto);
  }

  @Override
  public PatientDataForTherapyReportDto getPatientDataForTherapyReport(
      final String patientId,
      final boolean mainDiseaseTypeOnly,
      final DateTime when,
      final Locale locale)
  {
    return new PatientDataForTherapyReportDto(
        true,
        "Jane Smith",
        DateTimeFormatters.shortDateTime(locale).print(new DateTime(1984, 5, 3, 0, 0)),
        Gender.FEMALE,
        "BIS",
        "765344",
        "877545346",
        "Cardiology",
        "Cardio",
        "Ivan",
        "R02/B06",
        DateTimeFormatters.shortDateTime(locale).print(when.minusDays(7)),
        7,
        Collections.emptyList(),
        null,
        Lists.newArrayList("Amoxicillin", "Penicillin", "Eggs"),
        AllergiesStatus.PRESENT,
        "1111",
        "Ljubljana 20"
    );
  }

  @Override
  public Interval getLastDischargedCentralCaseEffectiveInterval(final String patientId)
  {
    return new Interval(new DateTime(2014, 10, 10, 12, 0), new DateTime(2014, 11, 15, 12, 0));
  }

  @Override
  public List<NamedExternalDto> getCurrentUserCareProviders()
  {
    return Collections.emptyList();
  }

  @Override
  public List<PatientDisplayWithLocationDto> getPatientDisplaysWithLocation(
      final Collection<String> careProviderIds,
      final Collection<String> patientIds)
  {
    return patientIds
        .stream()
        .map(patientId -> new PatientDisplayWithLocationDto(
            new PatientDisplayDto(
                patientId,
                "Jane Smith",
                new DateTime(1984, 2, 5, 0, 0),
                Gender.FEMALE,
                null),
            "Cardio",
            "R04/B06"))
        .collect(Collectors.toList());
  }

  @Override
  public List<QuantityWithTimeDto> getBloodSugarObservations(
      final @NonNull String patientId, final @NonNull Interval interval)
  {
    return Collections.emptyList();
  }

  @Override
  public List<QuantityWithTimeDto> getLabResults(
      final @NonNull String patientId,
      final @NonNull String resultCode,
      final @NonNull Interval interval)
  {
    return Collections.emptyList();
  }

  @Override
  public List<QuantityWithTimeDto> findMeanArterialPressureMeasurements(
      final @NonNull String patientId,
      final @NonNull Interval interval)
  {
    return Collections.emptyList();
  }
}
