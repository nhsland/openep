package com.marand.thinkmed.fdb.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.data.IdentityDto;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.fdb.dto.ApiError;
import com.marand.thinkmed.fdb.dto.FdbAlertRelevanceTypeEnum;
import com.marand.thinkmed.fdb.dto.FdbConceptTypeEnum;
import com.marand.thinkmed.fdb.dto.FdbConditionAlertSeverityEnum;
import com.marand.thinkmed.fdb.dto.FdbDrugSensitivityWarningDto;
import com.marand.thinkmed.fdb.dto.FdbGenderEnum;
import com.marand.thinkmed.fdb.dto.FdbInteractionsSeverityEnums;
import com.marand.thinkmed.fdb.dto.FdbDrug;
import com.marand.thinkmed.fdb.dto.FdbNameValue;
import com.marand.thinkmed.fdb.dto.FdbPatientChecksWarningDto;
import com.marand.thinkmed.fdb.dto.FdbPatientDto;
import com.marand.thinkmed.fdb.dto.FdbScreeningDto;
import com.marand.thinkmed.fdb.dto.FdbScreeningResultDto;
import com.marand.thinkmed.fdb.dto.FdbTerminologyDto;
import com.marand.thinkmed.fdb.dto.FdbTerminologyEnum;
import com.marand.thinkmed.fdb.dto.FdbTerminologyWithConceptDto;
import com.marand.thinkmed.fdb.dto.FdbWarningDto;
import com.marand.thinkmed.fdb.dto.FdbWarningDuplicateDto;
import com.marand.thinkmed.fdb.rest.FdbRestService;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.service.dto.WarningScreenMedicationDto;
import com.marand.thinkmed.medications.warnings.WarningsPlugin;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import static com.marand.thinkmed.medications.service.WarningSeverity.HIGH;
import static com.marand.thinkmed.medications.service.WarningSeverity.HIGH_OVERRIDE;
import static com.marand.thinkmed.medications.service.WarningType.UNMATCHED;

/**
 * @author Mitja Lapajne
 */
@Secured("ROLE_User")
@Component
public class FdbServiceImpl implements WarningsPlugin
{

  private FdbRestService restService;
  private MedsProperties medsProperties;

  @Autowired
  public void setRestService(final FdbRestService restService)
  {
    this.restService = restService;
  }

  @Autowired
  public void setMedsProperties(final MedsProperties medsProperties)
  {
    this.medsProperties = medsProperties;
  }

  @Override
  public void reloadCache() { }

  @Override
  public List<MedicationsWarningDto> findMedicationWarnings(
      @NonNull final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Double bsaInM2,
      @NonNull final Gender gender,
      @NonNull final List<IdNameDto> diseaseTypeValues,
      @NonNull final List<IdNameDto> allergiesExternalValues,
      @NonNull final List<WarningScreenMedicationDto> medicationSummaries,
      @NonNull final DateTime when)
  {
    final Map<String, Long> orderableMedIdToInternalIdMap = new HashMap<>();

    final FdbScreeningDto screeningDto =
        buildFdbScreeningDto(
            dateOfBirth,
            gender,
            diseaseTypeValues,
            medicationSummaries,
            allergiesExternalValues,
            when,
            orderableMedIdToInternalIdMap);

    final String json = JsonUtil.toJson(screeningDto);

    final String warningJson = restService.scanForWarnings(
        false,
        FdbConditionAlertSeverityEnum.PRECAUTION.getName(),
        FdbInteractionsSeverityEnums.LOW_RISK.getName(),
        json);

    final FdbScreeningResultDto resultDto = JsonUtil.fromJson(warningJson, FdbScreeningResultDto.class);

    final List<MedicationsWarningDto> mappedMedicationWarnings = mapMedicationsWarnings(
        medicationSummaries,
        resultDto,
        orderableMedIdToInternalIdMap);

    final List<FdbTerminologyWithConceptDto> drugs = Lists.newArrayList();
    drugs.addAll(screeningDto.getCurrentDrugs());
    drugs.addAll(screeningDto.getProspectiveDrugs());

    final MedicationsWarningDto unmatchedOrderableMedsWarning = createUnmatchedOrderableMedsWarning(drugs);

    if (unmatchedOrderableMedsWarning != null)
    {
      mappedMedicationWarnings.add(unmatchedOrderableMedsWarning);
    }

    return mappedMedicationWarnings;
  }

  MedicationsWarningDto createUnmatchedOrderableMedsWarning(final List<FdbTerminologyWithConceptDto> drugs)
  {
    final List<String> unmatchedOrderableMeds = drugs.stream()
        .filter(d -> d.getConceptType().equals(FdbConceptTypeEnum.DRUG.getName()))
        .map(FdbTerminologyDto::getName)
        .collect(Collectors.toList());
    final String names = String.join(", ", unmatchedOrderableMeds);

    if (!unmatchedOrderableMeds.isEmpty())
    {
      return new MedicationsWarningDto(
          Dictionary.getEntry("medication.warning.navigation.orderable.meds.unmatched")
              + " " + names + "!", HIGH, UNMATCHED);
    }
    return null;
  }

  List<MedicationsWarningDto> mapMedicationsWarnings(
      final List<WarningScreenMedicationDto> medicationSummaries,
      final FdbScreeningResultDto fdbResult,
      final Map<String, Long> orderableMedIdToInternalIdMap)
  {
    final Map<String, String> externalToInternalMedicationIdMap = createExternalToInternalMedicationMapping(
        medicationSummaries);

    return mapWarnings(fdbResult, externalToInternalMedicationIdMap, orderableMedIdToInternalIdMap);
  }

  List<MedicationsWarningDto> mapWarnings(
      final FdbScreeningResultDto returnedFdbWarnings,
      final Map<String, String> externalToInternalMedicationIdMap,
      final Map<String, Long> orderableMedIdToInternalIdMap)
  {
   final List<MedicationsWarningDto> warnings = new ArrayList<>();

   if (returnedFdbWarnings.getApiError() != null)
   {
     warnings.add(mapApiError(returnedFdbWarnings.getApiError()));
   }

    warnings.addAll(mapSensivitiesWarnings(
        returnedFdbWarnings.getDrugSensitivities(),
        externalToInternalMedicationIdMap,
        orderableMedIdToInternalIdMap));

    final List<FdbPatientChecksWarningDto> specificAndRelatedContraindicationPatientChecks =
        returnedFdbWarnings.getPatientChecks().stream()
            .filter(pc -> pc.getConditionAlertSeverity().getValue()
                .equals(FdbConditionAlertSeverityEnum.CONTRAINDICATION.getKey()))
            .filter(pc -> pc.getAlertRelevanceType().getValue().equals(FdbAlertRelevanceTypeEnum.SPECIFIC.getKey())
                || pc.getAlertRelevanceType().getValue().equals(FdbAlertRelevanceTypeEnum.RELATED.getKey()))
            .collect(Collectors.toList());

    warnings.addAll(mapPatientCheckWarnings(
        specificAndRelatedContraindicationPatientChecks,
        externalToInternalMedicationIdMap,
        orderableMedIdToInternalIdMap));

    final List<FdbPatientChecksWarningDto> unrelatedContraindicationPatientChecks =
        returnedFdbWarnings.getPatientChecks().stream()
            .filter(pc -> pc.getConditionAlertSeverity().getValue()
                .equals(FdbConditionAlertSeverityEnum.CONTRAINDICATION.getKey()))
            .filter(pc -> pc.getAlertRelevanceType().getValue().equals(FdbAlertRelevanceTypeEnum.UNRELATED.getKey()))
            .collect(Collectors.toList());

    warnings.addAll(mapPatientCheckWarnings(
        unrelatedContraindicationPatientChecks,
        externalToInternalMedicationIdMap,
        orderableMedIdToInternalIdMap));

    final List<FdbWarningDto> highRiskInteractions = returnedFdbWarnings.getDrugInteractions().stream()
        .filter(hr -> hr.getAlertSeverity().getValue().equals(FdbInteractionsSeverityEnums.HIGH_RISK.getKey()))
        .collect(Collectors.toList());

    warnings.addAll(mapInteractionsWarnings(
        highRiskInteractions,
        externalToInternalMedicationIdMap,
        orderableMedIdToInternalIdMap));

    warnings.addAll(mapDuplicateWarningsDto(
        returnedFdbWarnings.getDrugDoublings(),
        "Drug Doubling",
        externalToInternalMedicationIdMap,
        orderableMedIdToInternalIdMap));

    warnings.addAll(mapDuplicateWarningsDto(
        returnedFdbWarnings.getDrugEquivalences(),
        "Drug Equivalence",
        externalToInternalMedicationIdMap,
        orderableMedIdToInternalIdMap));

    warnings.addAll(mapDuplicateWarningsDto(
        returnedFdbWarnings.getDuplicateTherapies(),
        "Duplicate therapy",
    externalToInternalMedicationIdMap,
        orderableMedIdToInternalIdMap));

    final List<FdbWarningDto> significantRiskInteractions = returnedFdbWarnings.getDrugInteractions().stream()
        .filter(sr -> sr.getAlertSeverity().getValue().equals(FdbInteractionsSeverityEnums.SIGNIFICANT_RISK.getKey()))
        .collect(Collectors.toList());

    warnings.addAll(mapInteractionsWarnings(
        significantRiskInteractions,
        externalToInternalMedicationIdMap,
        orderableMedIdToInternalIdMap));

    final List<FdbPatientChecksWarningDto> precautionPatientChecks = returnedFdbWarnings.getPatientChecks().stream()
        .filter(pr -> pr.getConditionAlertSeverity().getValue().equals(FdbConditionAlertSeverityEnum.PRECAUTION.getKey()))
        .collect(Collectors.toList());

    warnings.addAll(mapPatientCheckWarnings(
        precautionPatientChecks,
        externalToInternalMedicationIdMap,
        orderableMedIdToInternalIdMap));

    final List<FdbWarningDto> moderateRiskInteractions = returnedFdbWarnings.getDrugInteractions().stream()
        .filter(m -> m.getAlertSeverity().getValue().equals(FdbInteractionsSeverityEnums.MODERATE_RISK.getKey()))
        .collect(Collectors.toList());

    warnings.addAll(mapInteractionsWarnings(
        moderateRiskInteractions,
        externalToInternalMedicationIdMap,
        orderableMedIdToInternalIdMap));

    final List<FdbWarningDto> lowRiskInteractions = returnedFdbWarnings.getDrugInteractions().stream()
        .filter(l -> l.getAlertSeverity().getValue().equals(FdbInteractionsSeverityEnums.LOW_RISK.getKey()))
        .collect(Collectors.toList());

    warnings.addAll(mapInteractionsWarnings(
        lowRiskInteractions,
        externalToInternalMedicationIdMap,
        orderableMedIdToInternalIdMap));

    return warnings;
  }

  private MedicationsWarningDto mapApiError(final ApiError apiError)
  {
    final MedicationsWarningDto medicationsWarningDto = new MedicationsWarningDto();
    medicationsWarningDto.setSeverity(WarningSeverity.HIGH);
    medicationsWarningDto.setExternalType("External service error");
    medicationsWarningDto.setDescription(apiError.getMessage());
    return medicationsWarningDto;
  }

  private List<MedicationsWarningDto> mapDuplicateWarningsDto(
      final List<FdbWarningDuplicateDto> fdbDuplicateWarnings,
      final String externalType,
      final Map<String, String> externalToInternalMedicationIdMap,
      final Map<String, Long> orderableMedIdToInternalId)
  {
    return fdbDuplicateWarnings.stream()
        .map(dw -> {
          final MedicationsWarningDto warning = new MedicationsWarningDto();
          warning.setDescription(dw.getFullAlertMessage());
          warning.setSeverity(medsProperties.isDuplicateTherapyWarningOverrideRequired()
                                            ? HIGH_OVERRIDE
                                            : WarningSeverity.HIGH);
          warning.setExternalType(externalType);
          warning.setMedications(mapMedications(
              Lists.newArrayList(dw.getPrimaryDrug(), dw.getSecondaryDrug()),
              externalToInternalMedicationIdMap,
              orderableMedIdToInternalId));
          warning.setType(WarningType.DUPLICATE);
          return warning;
        })
        .collect(Collectors.toList());
  }

  private List<MedicationsWarningDto> mapInteractionsWarnings(
      final List<FdbWarningDto> fdbInteractionsWarnings,
      final Map<String, String> externalToInternalMedicationIdMap,
      final Map<String, Long> orderableMedIdToInternalId)
  {
    return fdbInteractionsWarnings.stream()
        .map(iw ->
             {
               final MedicationsWarningDto warning = new MedicationsWarningDto();
               warning.setDescription(iw.getFullAlertMessage());
               warning.setSeverity(mapInteractionsSeverity(iw.getAlertSeverity()));
               warning.setExternalSeverity(mapFdbInteractionsSeverityToExternalSeverity(iw.getAlertSeverity().getName()));
               warning.setExternalType("Drug-Drug Interaction");
               warning.setMedications(mapMedications(
                   Lists.newArrayList(iw.getPrimaryDrug(), iw.getSecondaryDrug()),
                   externalToInternalMedicationIdMap,
                   orderableMedIdToInternalId));
               warning.setType(WarningType.INTERACTION);
               return warning;
             })
        .collect(Collectors.toList());
  }

  List<MedicationsWarningDto> mapPatientCheckWarnings(
      final List<FdbPatientChecksWarningDto> fdbPatientCheckWarnings,
      final Map<String, String> externalToInternalMedicationIdMap,
      final Map<String, Long> orderableMedIdToInternalIdMap)
  {
    return fdbPatientCheckWarnings.stream()
        .sorted(Comparator.comparing(p -> p.getAlertRelevanceType().getValue()))
        .map(pw -> {
          final MedicationsWarningDto warning = new MedicationsWarningDto();
          warning.setDescription(pw.getFullAlertMessage());
          warning.setSeverity(mapPatientCheckSeverity(pw));
          warning.setExternalSeverity(pw.getConditionAlertSeverity().getName());
          warning.setExternalType("Patient Checking");
          warning.setMedications(mapMedications(
              Lists.newArrayList(pw.getDrug()),
              externalToInternalMedicationIdMap,
              orderableMedIdToInternalIdMap));
          warning.setType(WarningType.PATIENT_CHECK);
          return warning;
        })
        .collect(Collectors.toList());
  }

  private List<MedicationsWarningDto> mapSensivitiesWarnings(
      final List<FdbDrugSensitivityWarningDto> fdbSensivitiesWarnings,
      final Map<String, String> externalToInternalMedicationIdMap,
      final Map<String, Long> orderableMedIdToInternalId)
  {
    return fdbSensivitiesWarnings.stream()
        .map(sw -> {
          final MedicationsWarningDto warning = new MedicationsWarningDto();
          warning.setDescription(sw.getFullAlertMessage());
          warning.setSeverity(WarningSeverity.HIGH_OVERRIDE);
          warning.setExternalType("Sensitivity Checking");
          warning.setMedications(mapMedications(
              Lists.newArrayList(sw.getDrug()),
              externalToInternalMedicationIdMap,
              orderableMedIdToInternalId));
          warning.setType(WarningType.ALLERGY);
          return warning;
        })
        .collect(Collectors.toList());
  }

  private String mapFdbInteractionsSeverityToExternalSeverity(final String fdbInteractionsSeverity)
  {
    if (FdbInteractionsSeverityEnums.LOW_RISK.getName().equals(fdbInteractionsSeverity))
    {
      return "Low Risk";
    }
    if (FdbInteractionsSeverityEnums.MODERATE_RISK.getName().equals(fdbInteractionsSeverity))
    {
      return "Moderate Risk";
    }
    if (FdbInteractionsSeverityEnums.SIGNIFICANT_RISK.getName().equals(fdbInteractionsSeverity))
    {
      return "Significant Risk";
    }
    if (FdbInteractionsSeverityEnums.HIGH_RISK.getName().equals(fdbInteractionsSeverity))
    {
      return "High Risk";
    }

    throw new IllegalArgumentException("FDB Severity " + fdbInteractionsSeverity + " not supported.");
  }

  List<NamedExternalDto> mapMedications(
      final List<FdbDrug> fdbWarningsMedications,
      final Map<String, String> externalToInternalMedicationIdMap,
      final Map<String, Long> orderableMedIdToInternalIdMap)
  {
    return fdbWarningsMedications.stream()
        .map(
            wm -> {
              final String fdbReturnedId = String.valueOf(wm.getId());
              if (wm.getConcept().getName().equals(FdbConceptTypeEnum.ORDERABLEMED.getName()))
              {
                Long id = orderableMedIdToInternalIdMap.get(String.valueOf(fdbReturnedId));
                return new NamedExternalDto(String.valueOf(id), wm.getUserSpecifiedName());
              }

              Long id = Long.valueOf(externalToInternalMedicationIdMap.get(String.valueOf(fdbReturnedId)));
              return new NamedExternalDto(String.valueOf(id), wm.getUserSpecifiedName());
            }
        )
        .collect(Collectors.toList());
  }

  Map<String, String> createExternalToInternalMedicationMapping(final Collection<WarningScreenMedicationDto> medications)
  {
    final Map<String, String> result = new HashMap<>();
    for (final WarningScreenMedicationDto externalMedication : medications)
    {
      result.put(externalMedication.getExternalId(), String.valueOf(externalMedication.getId()));
    }
    return result;
  }

  private WarningSeverity mapPatientCheckSeverity(final FdbPatientChecksWarningDto fdbWarning)
  {
    final Long relevance = fdbWarning.getAlertRelevanceType().getValue();
    final boolean triggeredByPatientsDisease = relevance.equals(FdbAlertRelevanceTypeEnum.SPECIFIC.getKey()) ||
        relevance.equals(FdbAlertRelevanceTypeEnum.RELATED.getKey());

    final boolean contraindication = fdbWarning.getConditionAlertSeverity().getValue()
        .equals(FdbConditionAlertSeverityEnum.CONTRAINDICATION.getKey());

    if (contraindication && triggeredByPatientsDisease)
    {
      return WarningSeverity.HIGH_OVERRIDE;
    }
    return WarningSeverity.OTHER;
  }

  FdbScreeningDto buildFdbScreeningDto(
      final DateTime dateOfBirth,
      final Gender gender,
      final List<IdNameDto> diseaseTypeCodes,
      final List<WarningScreenMedicationDto> medicationSummaries,
      final List<IdNameDto> allergiesExternalValues,
      final DateTime when,
      final Map<String, Long> orderableMedIdToInternalIdMap)
  {
    final FdbScreeningDto screeningDto = new FdbScreeningDto();

    addFdbModules(screeningDto);

    final FdbPatientDto patientInformation = new FdbPatientDto();
    final long ageInDays = (long)Days.daysBetween(dateOfBirth.toLocalDate(), when.toLocalDate()).getDays();

    patientInformation.setAge(ageInDays);
    patientInformation.setGender(extractGender(gender));
    patientInformation.setConditionListComplete(false);

    final Map<FdbTerminologyWithConceptDto, Long> prospectiveDrugsToInternalIdMap = medicationSummaries.stream()
        .filter(WarningScreenMedicationDto::isProspective)
        .collect(Collectors.toMap(this::mapDrug, IdentityDto::getId, (p1, p2) -> p1));

    final Map<FdbTerminologyWithConceptDto, Long> currentDrugsToInternalIdMap = medicationSummaries.stream()
        .filter(m -> !m.isProspective())
        .collect(Collectors.toMap(this::mapDrug, IdentityDto::getId, (p1, p2) -> p1));

    final Map<FdbTerminologyWithConceptDto, Long> drugsToInternalIdMap = new HashMap<>();
    drugsToInternalIdMap.putAll(prospectiveDrugsToInternalIdMap);
    drugsToInternalIdMap.putAll(currentDrugsToInternalIdMap);

    drugsToInternalIdMap.forEach((key, value) -> {
      if (key.getConceptType().equals(FdbConceptTypeEnum.ORDERABLEMED.getName()))
      {
        orderableMedIdToInternalIdMap.put(key.getId(), value);
      }
    });

    screeningDto.setPatientInformation(patientInformation);
    screeningDto.setCurrentDrugs(new ArrayList<>(currentDrugsToInternalIdMap.keySet()));
    screeningDto.setProspectiveDrugs(new ArrayList<>(prospectiveDrugsToInternalIdMap.keySet()));
    screeningDto.setAllergens(mapAllergies(allergiesExternalValues));
    screeningDto.setConditions(mapConditions(diseaseTypeCodes));

    return screeningDto;
  }

  private void addFdbModules(final FdbScreeningDto screeningDto)
  {
    screeningDto.getScreeningModules().add(FdbScreeningModulesEnum.PATIENT_CHECK.getKey());
    screeningDto.getScreeningModules().add(FdbScreeningModulesEnum.DRUG_INTERACTIONS.getKey());
    screeningDto.getScreeningModules().add(FdbScreeningModulesEnum.DRUG_SENSIVITIES.getKey());
    //screeningDto.getScreeningModules().add(FdbScreeningModulesEnum.DRUG_DOUBLINGS.getKey());
    //screeningDto.getScreeningModules().add(FdbScreeningModulesEnum.DRUG_EQUIVALENCE.getKey());
    //screeningDto.getScreeningModules().add(FdbScreeningModulesEnum.DUPLICATE_THERAPY.getKey());
    screeningDto.getScreeningModules().add(FdbScreeningModulesEnum.PHARMACOLOGICAL_EQUIVALENCE.getKey());
  }

  private Long extractGender(final Gender gender)
  {
    if (gender.getIsoCode() == Gender.MALE.getIsoCode())
    {
     return FdbGenderEnum.MALE.getKey();
    }
    if (gender.getIsoCode() == Gender.FEMALE.getIsoCode())
    {
      return FdbGenderEnum.FEMALE.getKey();
    }
    if (gender.getIsoCode() == Gender.INDEFINABLE.getIsoCode() || gender.getIsoCode() == Gender.NOT_KNOWN.getIsoCode())
    {
      return FdbGenderEnum.UNKNOWN.getKey();
    }

    return null;
  }

  private List<FdbTerminologyDto> mapConditions(final List<IdNameDto> diseaseTypeCodes)
  {
    return diseaseTypeCodes.stream()
        .map(c -> new FdbTerminologyDto(
            c.getId(),
            c.getName(),
            FdbTerminologyEnum.SNOMED.getName()
        ))
        .collect(Collectors.toList());
  }

  private List<FdbTerminologyWithConceptDto> mapAllergies(final List<IdNameDto> allergiesExternalValues)
  {
    return allergiesExternalValues.stream()
        .map(a -> new FdbTerminologyWithConceptDto(
            a.getId(),
            a.getName(),
            FdbTerminologyEnum.SNOMED.getName(),
            FdbConceptTypeEnum.DRUG.getName()
        ))
        .collect(Collectors.toList());
  }

  private Map<Long, FdbTerminologyWithConceptDto> mapDrugs(final List<WarningScreenMedicationDto> medicationSummaries)
  {
    Map<Long, FdbTerminologyWithConceptDto> map = new HashMap<>();
    medicationSummaries.stream()
        .forEach(m -> map.put(m.getId(), mapDrug(m)));
    return map;
  }

  FdbTerminologyWithConceptDto mapDrug(final WarningScreenMedicationDto warningScreenMedicationDto)
  {
    if (warningScreenMedicationDto.isProduct())
    {
      return createFdbDrug(
          warningScreenMedicationDto.getExternalId(),
          warningScreenMedicationDto.getName(),
          FdbConceptTypeEnum.PRODUCT);
    }

    final FdbTerminologyWithConceptDto fdbDrug = getOrderableMedication(warningScreenMedicationDto);

    return fdbDrug != null ? fdbDrug : createFdbDrug(
        warningScreenMedicationDto.getExternalId(),
        warningScreenMedicationDto.getName(),
        FdbConceptTypeEnum.DRUG);
  }

  private FdbTerminologyWithConceptDto getOrderableMedication(final WarningScreenMedicationDto warningScreenMedicationDto)
  {
    if (warningScreenMedicationDto.getExternalId() != null
        && warningScreenMedicationDto.getRouteExternalId() != null)
    {
      final String responseJson = restService.getOrderableMedicine(
          warningScreenMedicationDto.getName(),
          warningScreenMedicationDto.getExternalId(),
          warningScreenMedicationDto.getRouteExternalId());

      final JsonObject jsonObject = JsonUtil.fromJson(responseJson, JsonObject.class);

      if (jsonObject.getAsJsonObject("OrderableMed") != null)
      {
        if (jsonObject.getAsJsonObject("OrderableMed").get("SingleId") != null)
        {
          final String orderableMedicationId = jsonObject.getAsJsonObject("OrderableMed").get("SingleId").getAsString();

          return createFdbDrug(
              orderableMedicationId,
              warningScreenMedicationDto.getName(),
              FdbConceptTypeEnum.ORDERABLEMED);
        }
      }
    }

    return null;
  }

  private FdbTerminologyWithConceptDto createFdbDrug(
      final String id,
      final String name,
      final FdbConceptTypeEnum fdbConceptTypeEnum)
  {
    return new FdbTerminologyWithConceptDto(
        id,
        name,
        FdbTerminologyEnum.SNOMED.getName(),
        fdbConceptTypeEnum.getName());
  }

  private WarningSeverity mapInteractionsSeverity(final FdbNameValue fdbSeverity)
  {
    if (FdbInteractionsSeverityEnums.LOW_RISK.getKey().equals(fdbSeverity.getValue()) ||
        FdbInteractionsSeverityEnums.MODERATE_RISK.getKey().equals(fdbSeverity.getValue()) ||
        FdbInteractionsSeverityEnums.SIGNIFICANT_RISK.getKey().equals(fdbSeverity.getValue()))
    {
      return WarningSeverity.OTHER;
    }
    if (FdbInteractionsSeverityEnums.HIGH_RISK.getKey().equals(fdbSeverity.getValue()))
    {
      return WarningSeverity.HIGH;
    }
    throw new IllegalArgumentException("FDB Severity " + fdbSeverity.getName() + " not supported.");
  }

  @Override
  public boolean requiresDiseaseCodesTranslation()
  {
    return true;
  }

  @Override
  public String getExternalSystemName()
  {
    return "SNOMED";
  }
}
