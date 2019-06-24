package com.marand.meds.rest.meds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.marand.auditing.auditing.Auditable;
import com.marand.auditing.auditing.AuditableType;
import com.marand.maf.core.JsonUtil;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.MedsJsonDeserializer;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsActionDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.rule.parameters.RuleParameters;
import com.marand.thinkmed.medications.rule.result.RuleResult;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.marand.meds.config.GsonDeserializators.INTERVAL_DESERIALIZER;
import static com.marand.meds.config.GsonDeserializators.RULE_PARAMETERS_DESERIALIZER;

/**
 * @author Boris Marn.
 */
@RestController
@RequestMapping("/medications")
public class WarningsController
{
  private final MedicationsService medicationsService;

  @Autowired
  public WarningsController(final MedicationsService medicationsService)
  {
    this.medicationsService = medicationsService;
  }

  @PostMapping(value = "findCurrentTherapiesWarnings")
  public List<MedicationsWarningDto> findCurrentTherapiesWarnings(
      @RequestParam("patientId") final String patientId,
      @RequestParam("dateOfBirth") final String dateOfBirth,
      @RequestParam(value = "patientWeightInKg", required = false) final Double patientWeightInKg,
      @RequestParam("patientAllergies") final String patientAllergies,
      @RequestParam("patientDiseases") final String patientDiseases,
      @RequestParam(value = "bsaInM2", required = false) final Double bsaInM2,
      @RequestParam("gender") final String gender,
      @RequestParam("language") final String language)
  {
    final IdNameDto[] patientAllergiesArray = JsonUtil.fromJson(patientAllergies, IdNameDto[].class);
    final IdNameDto[] patientDiseasesArray = JsonUtil.fromJson(patientDiseases, IdNameDto[].class);
    final List<IdNameDto> patientAllergiesList = Arrays.asList(patientAllergiesArray);
    final List<IdNameDto> patientDiseasesList = Arrays.asList(patientDiseasesArray);

    return medicationsService.findCurrentTherapiesWarnings(
        patientId,
        JsonUtil.fromJson(dateOfBirth, DateTime.class),
        patientWeightInKg != null && patientWeightInKg > 0.0 ? patientWeightInKg : null,
        bsaInM2 != null && bsaInM2 > 0.0 ? bsaInM2 : null,
        JsonUtil.fromJson(gender, Gender.class),
        patientDiseasesList,
        patientAllergiesList,
        new Locale(language)
    );
  }

  @PostMapping(value = "findMedicationWarnings")
  public List<MedicationsWarningDto> findMedicationWarnings(
      @RequestParam("patientId") final String patientId,
      @RequestParam("dateOfBirth") final String dateOfBirth,
      @RequestParam(value = "patientWeightInKg", required = false) final Double patientWeightInKg,
      @RequestParam("patientAllergies") final String patientAllergies,
      @RequestParam("patientDiseases") final String patientDiseases,
      @RequestParam(value = "bsaInM2", required = false) final Double bsaInM2,
      @RequestParam("gender") final String gender,
      @RequestParam("therapies") final String therapiesJson,
      @RequestParam(value = "includeActiveTherapies", required = false) final Boolean includeActiveTherapies,
      @RequestParam("language") final String language)
  {
    final List<TherapyDto> therapies = Arrays.asList(JsonUtil.fromJson(
        therapiesJson,
        TherapyDto[].class,
        MedsJsonDeserializer.INSTANCE.getTypeAdapters()));

    final IdNameDto[] patientAllergiesArray = JsonUtil.fromJson(patientAllergies, IdNameDto[].class);
    final IdNameDto[] patientDiseasesArray = JsonUtil.fromJson(patientDiseases, IdNameDto[].class);

    final List<IdNameDto> patientAllergiesList = Arrays.asList(patientAllergiesArray);
    final List<IdNameDto> patientDiseasesList = Arrays.asList(patientDiseasesArray);

    return medicationsService.findMedicationWarnings(
        patientId,
        JsonUtil.fromJson(dateOfBirth, DateTime.class),
        patientWeightInKg != null && patientWeightInKg > 0.0 ? patientWeightInKg : null,
        bsaInM2 != null && bsaInM2 > 0.0 ? bsaInM2 : null,
        JsonUtil.fromJson(gender, Gender.class),
        patientDiseasesList,
        patientAllergiesList,
        therapies,
        includeActiveTherapies == null ? false : includeActiveTherapies,
        new Locale(language));
  }

  @GetMapping(value = "getAdditionalWarnings", produces = MediaType.APPLICATION_JSON_VALUE)
  public AdditionalWarningsDto getAdditionalWarnings(
      @RequestParam("patientId") final String patientId,
      @RequestParam("additionalWarningsTypes") final String additionalWarningsTypes,
      @RequestParam("patientData") final String patientData,
      @RequestParam("language") final String language)
  {
    final PatientDataForMedicationsDto patientDataForMedications = JsonUtil.fromJson(
        patientData,
        PatientDataForMedicationsDto.class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));

    final List<AdditionalWarningsType> warningsTypes = Arrays.asList(JsonUtil.fromJson(
        additionalWarningsTypes,
        AdditionalWarningsType[].class));

    return
        medicationsService.getAdditionalWarnings(
            patientId,
            warningsTypes,
            patientDataForMedications,
            new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "handleAdditionalWarningsAction", produces = MediaType.TEXT_PLAIN_VALUE)
  public void handleAdditionalWarningsAction(@RequestParam("additionalWarningsActionDto") final String additionalWarningsActionDto)
  {
    medicationsService.handleAdditionalWarningsAction(JsonUtil.fromJson(
        additionalWarningsActionDto,
        AdditionalWarningsActionDto.class,
        MedsJsonDeserializer.INSTANCE.getTypeAdapters()));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "applyMedicationRule", produces = MediaType.APPLICATION_JSON_VALUE)
  public RuleResult applyMedicationRule(
      @RequestParam("ruleParameters") final String ruleParametersJson,
      @RequestParam("language") final String language)
  {
    final List<JsonUtil.TypeAdapterPair> adapters = new ArrayList<>();
    adapters.add(RULE_PARAMETERS_DESERIALIZER);
    adapters.addAll(MedsJsonDeserializer.INSTANCE.getTypeAdapters());
    adapters.add(INTERVAL_DESERIALIZER);

    final RuleParameters ruleParameters = JsonUtil.fromJson(ruleParametersJson, RuleParameters.class, adapters);

    final Locale locale = new Locale(language);
    return medicationsService.applyMedicationRule(ruleParameters, locale);
  }
}
