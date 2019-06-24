package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.marand.maf.core.CollectionUtils;
import com.marand.maf.core.Opt;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.api.externals.data.object.ExternalIdentityDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.AllergiesDto;
import com.marand.thinkmed.medications.connector.data.object.EncounterDto;
import com.marand.thinkmed.medications.connector.data.object.EncounterType;
import com.marand.thinkmed.medications.connector.data.object.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.connector.data.object.ObservationDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDemographicsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDemographicsEncounterDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Mitja Lapajne
 */
public class ProviderBasedMedicationsConnector implements MedicationsConnector
{
  private PatientDemographicsProvider patientDemographicsProvider;
  private EncounterProvider encounterProvider;
  private AllergiesProvider allergiesProvider;
  private DiseasesProvider diseasesProvider;
  private WeightProvider weightProvider;
  private HeightProvider heightProvider;
  private BloodGlucoseProvider bloodGlucoseProvider;
  private LabResultProvider labResultProvider;

  @Autowired
  public void setPatientDemographicsProvider(final PatientDemographicsProvider patientDemographicsProvider)
  {
    this.patientDemographicsProvider = patientDemographicsProvider;
  }

  @Autowired
  public void setEncounterProvider(final EncounterProvider encounterProvider)
  {
    this.encounterProvider = encounterProvider;
  }

  @Autowired
  public void setAllergiesProvider(final AllergiesProvider allergiesProvider)
  {
    this.allergiesProvider = allergiesProvider;
  }

  @Autowired
  public void setDiseasesProvider(final DiseasesProvider diseasesProvider)
  {
    this.diseasesProvider = diseasesProvider;
  }

  @Autowired
  public void setWeightProvider(final WeightProvider weightProvider)
  {
    this.weightProvider = weightProvider;
  }

  @Autowired
  public void setHeightProvider(final HeightProvider heightProvider)
  {
    this.heightProvider = heightProvider;
  }

  @Autowired
  public void setBloodGlucoseProvider(final BloodGlucoseProvider bloodGlucoseProvider)
  {
    this.bloodGlucoseProvider = bloodGlucoseProvider;
  }

  @Autowired
  public void setLabResultProvider(final LabResultProvider labResultProvider)
  {
    this.labResultProvider = labResultProvider;
  }

  @Override
  public PatientDataForMedicationsDto getPatientData(
      final @NonNull String patientId,
      final String centralCaseId,
      final @NonNull DateTime when)
  {
    final PatientDto patientData = getPatientData(patientId, centralCaseId);

    final List<IdNameDto> diseases = patientData.getDiseases().stream()
        .map(d -> new IdNameDto(d.getId(), d.getName()))
        .collect(Collectors.toList());

    final List<IdNameDto> allergies = patientData.getAllergies().getAllergens().stream()
        .map(a -> new IdNameDto(a.getId(), a.getName()))
        .collect(Collectors.toList());

    MedicationsCentralCaseDto centralCaseDto = null;
    final EncounterDto encounter = patientData.getEncounter();
    if (encounter != null)
    {
      centralCaseDto = new MedicationsCentralCaseDto();
      centralCaseDto.setCentralCaseId(encounter.getId());
      centralCaseDto.setCareProvider(encounter.getWard());
      centralCaseDto.setCentralCaseEffective(
          encounter.getEnd() != null ?
          new Interval(encounter.getStart(), encounter.getEnd()) :
          Intervals.infiniteFrom(encounter.getStart())
      );
      centralCaseDto.setCentralCaseId(encounter.getId());
      centralCaseDto.setOutpatient(encounter.getType() == EncounterType.OUTPATIENT);
    }
    return new PatientDataForMedicationsDto(
        patientData.getDemographics().getBirthDate(),
        patientData.getDemographics().getName(),
        patientData.getWeight(),
        patientData.getHeight(),
        patientData.getDemographics().getGender(),
        diseases,
        patientData.getAllergies().getAllergiesStatus(),
        allergies,
        centralCaseDto);
  }

  @Override
  public PatientDataForTherapyReportDto getPatientDataForTherapyReport(
      final String patientId, final boolean mainDiseaseTypeOnly, final DateTime when, final Locale locale)
  {
    final PatientDemographicsDto patientDemographics =
        patientDemographicsProvider.getPatientsDemographics(Collections.singleton(patientId)).get(0);

    final EncounterDto encounter = encounterProvider.getPatientLatestEncounter(patientId);

    final AllergiesDto patientAllergiesWithStatus = allergiesProvider.getAllergies(patientId);

    final List<String> allergiesPrintout = patientAllergiesWithStatus.getAllergens().stream()
        .map(IdNameDto::getName)
        .collect(Collectors.toList());

    return new PatientDataForTherapyReportDto(
        true,
        patientDemographics.getName(),
        DateTimeFormatters.shortDateTime(locale).print(patientDemographics.getBirthDate()),
        patientDemographics.getDisplayGender(),
        patientDemographics.getPatientIdentificatorType(),
        patientDemographics.getId(),
        null,
        encounter != null && encounter.getWard() != null ? encounter.getWard().getName() : null,
        encounter != null && encounter.getWard() != null ? encounter.getWard().getName() : null,
        null,
        encounter != null ? encounter.getLocation() : null,
        encounter != null ? DateTimeFormatters.shortDateTime(locale).print(encounter.getStart()) : "",
        null,
        Collections.emptyList(),
        null,
        allergiesPrintout,
        patientAllergiesWithStatus.getAllergiesStatus(),
        encounter != null && encounter.getWard() != null ? encounter.getWard().getId() : null,
        patientDemographics.getAddress()
    );
  }

  @Override
  public Interval getLastDischargedCentralCaseEffectiveInterval(final String patientId)
  {
    return null;
  }

  @Override
  public List<NamedExternalDto> getCurrentUserCareProviders()
  {
    return new ArrayList<>();
  }

  @Override
  public List<PatientDisplayWithLocationDto> getPatientDisplaysWithLocation(
      final Collection<String> careProviderIds,
      final Collection<String> patientIds)
  {
    return getPatientsEncounters(patientIds)
        .stream()
        .map(pe -> {
               final PatientDemographicsDto demographics = pe.getPatientDemographics();
               final PatientDisplayDto patientDisplayDto = new PatientDisplayDto(
                   demographics.getId(),
                   demographics.getName(),
                   demographics.getBirthDate(),
                   demographics.getDisplayGender(),
                   null);

               return new PatientDisplayWithLocationDto(
                   patientDisplayDto,
                   Opt.of(pe.getEncounter()).map(e -> e.getWard().getName()).orElse(null),
                   Opt.of(pe.getEncounter()).map(EncounterDto::getLocation).orElse(null));
             }
        )
        .collect(Collectors.toList());
  }

  @Override
  public List<QuantityWithTimeDto> getBloodSugarObservations(
      final @NonNull String patientId, final @NonNull Interval interval)
  {
    return getPatientBloodGlucoseMeasurements(patientId, interval).stream()
        .map(m -> new QuantityWithTimeDto(m.getTimestamp(), m.getValue(), m.getComment()))
        .collect(Collectors.toList());
  }

  @Override
  public List<QuantityWithTimeDto> getLabResults(
      final @NonNull String patientId,
      final @NonNull String resultCode,
      final @NonNull Interval interval)
  {
    final List<ObservationDto> results = labResultProvider.getLabResults(patientId, resultCode, interval);
    return results.stream()
        .map(m -> new QuantityWithTimeDto(m.getTimestamp(), m.getValue(), m.getComment()))
        .collect(Collectors.toList());
  }

  @Override
  public List<QuantityWithTimeDto> findMeanArterialPressureMeasurements(
      final @NonNull String patientId,
      final @NonNull Interval interval)
  {
    return Collections.emptyList();
  }

  //TODO expose it in MedicationsConnector and replace old methods with this one
  private List<PatientDemographicsEncounterDto> getPatientsEncounters(final @NonNull Collection<String> patientsIds)
  {
    final List<PatientDemographicsEncounterDto> demographicsEncounters = new ArrayList<>();
    if (!patientsIds.isEmpty())
    {
      final List<PatientDemographicsDto> patientsDemographics =
          patientDemographicsProvider.getPatientsDemographics(patientsIds);
      final Map<String, PatientDemographicsDto> patientsDemographicsMap = patientsDemographics.stream()
          .collect(Collectors.toMap(ExternalIdentityDto::getId, p -> p));

      List<EncounterDto> encounters;

      //TODO remove try catch when OneAdvanced fix Encounter endpoint
      try
      {
        encounters = encounterProvider.getPatientsActiveEncounters(patientsIds);
      }
      catch (final Exception e)
      {
        encounters = new ArrayList<>();
      }

      final Map<String, EncounterDto> patientsEncountersMap = encounters.stream()
          .collect(Collectors.toMap(ExternalIdentityDto::getId, p -> p));

      patientsIds.forEach(
          patientId ->
              demographicsEncounters.add(
                  new PatientDemographicsEncounterDto(
                      patientsDemographicsMap.get(patientId),
                      patientsEncountersMap.get(patientId))));
    }
    return demographicsEncounters;
  }

  //TODO expose it in MedicationsConnector and replace old methods with this one
  PatientDto getPatientData(final @NonNull String patientId, final String encounterId)
  {
    final PatientDto patient = new PatientDto();
    final List<PatientDemographicsDto> patientsDemographics =
        patientDemographicsProvider.getPatientsDemographics(Collections.singleton(patientId));
    patient.setDemographics(CollectionUtils.getFirstOrNull(patientsDemographics));

    patient.setEncounter(getEncounter(patientId, encounterId));

    patient.setAllergies(allergiesProvider.getAllergies(patientId));
    patient.setDiseases(diseasesProvider.getPatientDiseases(patientId));
    patient.setWeight(weightProvider.getPatientWeight(patientId));
    patient.setHeight(heightProvider.getPatientHeight(patientId));
    return patient;
  }

  private EncounterDto getEncounter(final @NonNull String patientId, final String encounterId)
  {
    return Opt.of(encounterId)
        .map(e -> encounterProvider.getEncounter(patientId, e))
        .orElse(encounterProvider.getPatientLatestEncounter(patientId));
  }

  //TODO expose it in MedicationsConnector and replace old methods with this one
  private List<ObservationDto> getPatientBloodGlucoseMeasurements(
      final @NonNull String patientId, final @NonNull Interval interval)
  {
    return bloodGlucoseProvider.getPatientBloodGlucoseMeasurements(patientId, interval);
  }
}
