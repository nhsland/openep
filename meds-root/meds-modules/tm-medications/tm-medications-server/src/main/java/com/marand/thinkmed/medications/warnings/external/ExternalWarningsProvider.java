package com.marand.thinkmed.medications.warnings.external;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.maf.core.StackTraceUtils;
import com.marand.maf.core.data.object.NamedIdDto;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.service.dto.WarningScreenMedicationDto;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import com.marand.thinkmed.medications.warnings.TherapyWarningsUtils;
import com.marand.thinkmed.medications.warnings.WarningsPlugin;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.marand.thinkmed.medications.service.WarningSeverity.HIGH;
import static com.marand.thinkmed.medications.service.WarningType.DUPLICATE;
import static com.marand.thinkmed.medications.service.WarningType.FAILED;
import static com.marand.thinkmed.medications.service.WarningType.UNMATCHED;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Nejc Korasa
 */

@Component
public class ExternalWarningsProvider
{
  private static final Logger LOG = LoggerFactory.getLogger(ExternalWarningsProvider.class);

  private MedicationsDao medicationsDao;
  private MedsProperties medsProperties;
  private WarningsPlugin warningsPlugin;
  private TherapyWarningsUtils therapyWarningsUtils;
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  @Autowired
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Autowired(required = false)
  public void setWarningsPlugin(final WarningsPlugin warningsPlugin)
  {
    this.warningsPlugin = warningsPlugin;
  }

  @Autowired
  public void setMedsProperties(final MedsProperties medsProperties)
  {
    this.medsProperties = medsProperties;
  }

  @Autowired
  public void setTherapyWarningsUtils(final TherapyWarningsUtils therapyWarningsUtils)
  {
    this.therapyWarningsUtils = therapyWarningsUtils;
  }

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  public List<MedicationsWarningDto> getExternalWarnings(
      final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Double bsaInM2,
      final @NonNull Gender gender,
      final @NonNull List<IdNameDto> diseases,
      final @NonNull List<IdNameDto> allergies,
      final @NonNull List<TherapyDto> prospectiveTherapies,
      final @NonNull List<TherapyDto> activeTherapies,
      final @NonNull DateTime when)
  {
    final List<MedicationsWarningDto> externalWarnings = new ArrayList<>();

    if (warningsPlugin != null)
    {
      final List<WarningScreenMedicationDto> medicationsForWarnings = buildWarningScreenMedications(
          prospectiveTherapies,
          activeTherapies);

      final ExternalWarningsProvider.MappedAndUnmatchedConditionLists allergiesConditionLists = translateConditionIds(
          medsProperties.getAllergyCodeTranslationRequired(),
          MedicationsExternalValueType.ALLERGY,
          warningsPlugin.getExternalSystemName(),
          allergies);

      final ExternalWarningsProvider.MappedAndUnmatchedConditionLists diseasesConditionLists = translateConditionIds(
          medsProperties.getDiseaseCodeTranslationRequired(),
          MedicationsExternalValueType.DISEASE,
          warningsPlugin.getExternalSystemName(),
          diseases);

      // load external warnings
      final List<MedicationsWarningDto> warningsFromExternalSystem =
          getWarningsFromExternalSystem(
              allergiesConditionLists.getMappedConditions(),
              diseasesConditionLists.getMappedConditions(),
              medicationsForWarnings,
              dateOfBirth,
              patientWeightInKg,
              bsaInM2,
              gender,
              when);

      externalWarnings.addAll(warningsFromExternalSystem);
      externalWarnings.addAll(getUnmatchedMedicationsWarnings(medicationsForWarnings));
      externalWarnings.addAll(getUnmatchedConditionsWarnings(
          allergiesConditionLists.getUnmatchedConditions(),
          diseasesConditionLists.getUnmatchedConditions()));
    }

    return externalWarnings;
  }

  List<MedicationsWarningDto> getUnmatchedMedicationsWarnings(final List<WarningScreenMedicationDto> warningScreenMedications)
  {
    final String medicationsWithNoExternalIdNames = warningScreenMedications.stream()
        .filter(m -> m.getExternalId() == null)
        .filter(WarningScreenMedicationDto::isProspective)
        .map(NamedIdDto::getName)
        .collect(Collectors.joining(", "));

    final MedicationsWarningDto warning = buildUnmatchedWarning(medicationsWithNoExternalIdNames, "medications.unmatched");
    return warning == null ? Collections.emptyList() : Collections.singletonList(warning);
  }

  private List<MedicationsWarningDto> getUnmatchedConditionsWarnings(
      final List<IdNameDto> allergiesUnmatchedConditions,
      final List<IdNameDto> diseasesUnmatchedConditions)
  {
    final List<MedicationsWarningDto> warnings = new ArrayList<>();

    createWarningForUnmatchedConditions(
        medsProperties.getAllergyCodeTranslationRequired(),
        allergiesUnmatchedConditions,
        "allergies.unmatched")
        .ifPresent(warnings::add);

    createWarningForUnmatchedConditions(
        medsProperties.getDiseaseCodeTranslationRequired(),
        diseasesUnmatchedConditions,
        "diseases.unmatched")
        .ifPresent(warnings::add);

    return warnings;
  }

  private List<WarningScreenMedicationDto> buildWarningScreenMedications(
      final List<TherapyDto> prospectiveTherapies,
      final List<TherapyDto> activeTherapies)
  {
    final List<WarningScreenMedicationDto> warningScreenMedications = new ArrayList<>();

    warningScreenMedications.addAll(therapyWarningsUtils.extractWarningScreenMedicationDtos(activeTherapies));
    warningScreenMedications.addAll(
        therapyWarningsUtils.extractWarningScreenMedicationDtos(prospectiveTherapies)
            .stream()
            .peek(m -> m.setProspective(true))
            .collect(toList()));

    final Map<Long, String> externalIdMap = buildMedicationExternalIdMap(prospectiveTherapies, activeTherapies);
    warningScreenMedications.forEach(m -> m.setExternalId(externalIdMap.getOrDefault(m.getId(), null)));
    return warningScreenMedications;
  }

  private List<MedicationsWarningDto> getWarningsFromExternalSystem(
      final List<IdNameDto> allergiesExternalIds,
      final List<IdNameDto> diseasesExternalIds,
      final List<WarningScreenMedicationDto> medicationForWarnings,
      final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Double bsaInM2,
      final Gender gender,
      final DateTime when)
  {
    final List<MedicationsWarningDto> warnings = new ArrayList<>();
    try
    {
      final List<WarningScreenMedicationDto> screenableMedications = medicationForWarnings
          .stream()
          .filter(m -> m.getExternalId() != null)
          .collect(toList());

      warnings.addAll(warningsPlugin.findMedicationWarnings(
          dateOfBirth,
          patientWeightInKg,
          bsaInM2,
          gender,
          diseasesExternalIds,
          allergiesExternalIds,
          screenableMedications,
          when));
    }
    catch (final RuntimeException ex)
    {
      LOG.error(String.format(
          "Failed finding warnings from %s\n%s",
          Opt.resolve(() -> warningsPlugin.getExternalSystemName()).orElse(""),
          StackTraceUtils.getStackTraceString(ex)));

      warnings.add( new MedicationsWarningDto(Dictionary.getEntry("medication.warning.screening.failed"), HIGH, FAILED));
    }

    return warnings.stream()
        .filter(w -> !ignoreWarning(w))
        .distinct()
        .collect(toList());
  }

  boolean ignoreWarning(final MedicationsWarningDto warning)
  {
    if (warning.getType() == DUPLICATE && warning.getMedications() != null)
    {
      return warning.getMedications().stream()
          .filter(m -> m.getId() != null)
          .map(m -> medicationsValueHolderProvider.getMedicationData(Long.valueOf(m.getId())))
          .anyMatch(MedicationDataDto::ignoreDuplicationWarnings);
    }
    return false;
  }

  private ExternalWarningsProvider.MappedAndUnmatchedConditionLists translateConditionIds(
      final boolean translationRequired,
      final MedicationsExternalValueType medicalConditionType,
      final String externalSystemName,
      final List<IdNameDto> medicalConditionsList)
  {
    return translationRequired
           ? getMappedAndUnmatchedConditions(externalSystemName, medicalConditionType, medicalConditionsList)
           : new ExternalWarningsProvider.MappedAndUnmatchedConditionLists(medicalConditionsList, null);
  }

  private Map<Long, String> buildMedicationExternalIdMap(
      final List<TherapyDto> prospectiveTherapies,
      final List<TherapyDto> activeTherapies)
  {
    final Set<Long> allMedicationIds = Stream.of(prospectiveTherapies, activeTherapies)
        .flatMap(List::stream)
        .flatMap(t -> t.getMedicationIds().stream())
        .collect(toSet());

    return medicationsDao.getMedicationsExternalIds(warningsPlugin.getExternalSystemName(), allMedicationIds);
  }

  Opt<MedicationsWarningDto> createWarningForUnmatchedConditions(
      final boolean translationRequired,
      final List<IdNameDto> unmatchedConditions,
      final String messageKey)
  {
    if (translationRequired)
    {
      final String conditionsWithNoExternalIdNames = unmatchedConditions.stream()
          .map(IdNameDto::getName)
          .collect(Collectors.joining(", "));

      return Opt.of(buildUnmatchedWarning(conditionsWithNoExternalIdNames, messageKey));
    }
    return Opt.none();
  }

  private MedicationsWarningDto buildUnmatchedWarning(final String unpairedItems, final String messageKey)
  {
    // TODO should we add medication ids to warning?
    return unpairedItems.isEmpty()
           ? null
           : new MedicationsWarningDto(Dictionary.getEntry(messageKey) + ": " + unpairedItems + '!', HIGH, UNMATCHED);
  }

  ExternalWarningsProvider.MappedAndUnmatchedConditionLists getMappedAndUnmatchedConditions(
      final String externalSystem,
      final MedicationsExternalValueType valueType,
      final List<IdNameDto> conditionsList)
  {
    final Map<String, String> externalValues = medicationsDao.getMedicationExternalValues(
        externalSystem,
        valueType,
        conditionsList.stream().map(IdNameDto::getId).collect(toSet()));

    final List<IdNameDto> mappedConditions = new ArrayList<>();
    final List<IdNameDto> unmatchedConditions = new ArrayList<>();

    for (final IdNameDto condition : conditionsList)
    {
      if (externalValues.containsKey(condition.getId()))
      {
        mappedConditions.add(new IdNameDto(externalValues.get(condition.getId()), condition.getName()));
      }
      else
      {
        unmatchedConditions.add(condition);
      }
    }

    return new ExternalWarningsProvider.MappedAndUnmatchedConditionLists(mappedConditions, unmatchedConditions);
  }

  @SuppressWarnings({"PackageVisibleInnerClass", "AssignmentOrReturnOfFieldWithMutableType"})
  static class MappedAndUnmatchedConditionLists
  {
    private final List<IdNameDto> mappedConditions;
    private final List<IdNameDto> unmatchedConditions;

    MappedAndUnmatchedConditionLists(final List<IdNameDto> mappedConditions, final List<IdNameDto> unmatchedConditions)
    {
      this.mappedConditions = mappedConditions;
      this.unmatchedConditions = unmatchedConditions;
    }

    public List<IdNameDto> getMappedConditions()
    {
      return mappedConditions;
    }

    public List<IdNameDto> getUnmatchedConditions()
    {
      return unmatchedConditions;
    }
  }
}
