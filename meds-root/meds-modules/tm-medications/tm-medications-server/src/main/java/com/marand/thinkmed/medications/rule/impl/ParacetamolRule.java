package com.marand.thinkmed.medications.rule.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.administration.converter.AdministrationFromEhrConverter;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationAdministration;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.PrescriptionsEhrUtils;
import com.marand.thinkmed.medications.ingredient.IngredientCalculator;
import com.marand.thinkmed.medications.rule.MedicationParacetamolRuleType;
import com.marand.thinkmed.medications.rule.MedicationRule;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForAdministrationParameters;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForTherapiesParameters;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForTherapyParameters;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleParameters;
import com.marand.thinkmed.medications.rule.result.ParacetamolRuleResult;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.medications.units.converter.UnitsConverter;
import com.marand.thinkmed.medications.units.provider.UnitsProvider;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolder;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.marand.thinkmed.medications.dto.unit.KnownUnitType.G;
import static com.marand.thinkmed.medications.dto.unit.KnownUnitType.MG;

/**
 * @author Nejc Korasa
 */
@Component(value = "PARACETAMOL_MAX_DAILY_DOSE")
public class ParacetamolRule implements MedicationRule<ParacetamolRuleParameters, ParacetamolRuleResult>
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsValueHolderProvider medicationsValueHolderProvider;
  private MedicationsValueHolder medicationsValueHolder;
  private IngredientCalculator ingredientCalculator;
  private AdministrationFromEhrConverter administrationFromEhrConverter;
  private MedicationsTasksProvider medicationsTasksProvider;
  private TherapyConverter therapyConverter;

  private UnitsConverter unitsConverter;
  private UnitsProvider unitsProvider;

  private static final double UNDERAGE_MAX_MG_PER_KG_PER_DAY = 60;  // 60mg/kg/day
  private static final double MAX_MG_PER_DAY_KG_LEVEL_1 = 2000;  // 2g per day
  private static final double MAX_MG_PER_DAY_KG_LEVEL_2 = 4000;  // 4g per day
  private static final double KG_LEVEL_LIMIT = 50;  // 50kg

  private static final double UNDERAGE_LIMIT = 16;
  private static final int BETWEEN_DOSES_LIMIT = 4; // in hours

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setMedicationsValueHolder(final MedicationsValueHolder  medicationsValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
  }

  @Autowired
  public void setIngredientCalculator(final IngredientCalculator ingredientCalculator)
  {
    this.ingredientCalculator = ingredientCalculator;
  }

  @Autowired
  public void setAdministrationFromEhrConverter(final AdministrationFromEhrConverter administrationFromEhrConverter)
  {
    this.administrationFromEhrConverter = administrationFromEhrConverter;
  }

  @Autowired
  public void setTherapyConverter(final TherapyConverter therapyConverter)
  {
    this.therapyConverter = therapyConverter;
  }

  @Autowired
  public void setUnitsConverter(final UnitsConverter unitsConverter)
  {
    this.unitsConverter = unitsConverter;
  }

  @Autowired
  public void setUnitsProvider(final UnitsProvider unitsProvider)
  {
    this.unitsProvider = unitsProvider;
  }

  @Override
  public ParacetamolRuleResult applyRule(
      final @NonNull ParacetamolRuleParameters parameters,
      final @NonNull DateTime actionTimestamp,
      final @NonNull Locale locale)
  {
    final MedicationParacetamolRuleType medicationParacetamolRuleType = parameters.getMedicationParacetamolRuleType();

    if (medicationParacetamolRuleType == MedicationParacetamolRuleType.FOR_THERAPY)
    {
      final TherapyDto therapyDto = ((ParacetamolRuleForTherapyParameters)parameters).getTherapyDto();
      final List<MedicationDataDto> medicationDataDtoList = ((ParacetamolRuleForTherapyParameters)parameters).getMedicationDataDtoList();

      Preconditions.checkNotNull(therapyDto, "therapyDto must not be null");
      Preconditions.checkNotNull(medicationDataDtoList, "medicationDataDtoList must not be null");
      Preconditions.checkArgument(!medicationDataDtoList.isEmpty(), "medicationDataDtoList must not be empty");

      return applyRuleForTherapy(
          therapyDto,
          medicationDataDtoList,
          parameters.getPatientWeight(),
          parameters.getPatientAgeInYears(),
          locale);
    }
    else if (medicationParacetamolRuleType == MedicationParacetamolRuleType.FOR_THERAPIES)
    {
      final List<TherapyDto> therapies = ((ParacetamolRuleForTherapiesParameters)parameters).getTherapies();
      final String patientId = ((ParacetamolRuleForTherapiesParameters)parameters).getPatientId();

      Preconditions.checkNotNull(therapies, "therapies must not be null");
      Preconditions.checkNotNull(patientId, "patientId must not be null");

      return applyRuleForTherapies(
          therapies,
          parameters.getPatientWeight(),
          parameters.getPatientAgeInYears(),
          locale);
    }
    else if (medicationParacetamolRuleType == MedicationParacetamolRuleType.FOR_ADMINISTRATION)
    {
      final TherapyDoseDto therapyDoseDto = ((ParacetamolRuleForAdministrationParameters)parameters).getTherapyDoseDto();
      final String administrationId = ((ParacetamolRuleForAdministrationParameters)parameters).getAdministrationId();
      final String taskId = ((ParacetamolRuleForAdministrationParameters)parameters).getTaskId();
      final TherapyDto therapyDto = ((ParacetamolRuleForAdministrationParameters)parameters).getTherapyDto();
      final Interval searchInterval = ((ParacetamolRuleForAdministrationParameters)parameters).getSearchInterval();
      final String patientId = ((ParacetamolRuleForAdministrationParameters)parameters).getPatientId();

      Preconditions.checkNotNull(searchInterval, "searchInterval must not be null");
      Preconditions.checkNotNull(patientId, "patientId must not be null");

      return applyRuleForAdministration(
          therapyDoseDto,
          administrationId,
          taskId,
          therapyDto,
          parameters.getPatientWeight(),
          parameters.getPatientAgeInYears(),
          searchInterval,
          patientId,
          locale);
    }
    else
    {
      throw new IllegalArgumentException("Not supported medication paracetamol rule type");
    }
  }

  private ParacetamolRuleResult applyRuleForTherapies(
      final List<TherapyDto> basketTherapies,
      final Double patientWeight,
      final Long patientAgeInYears,
      final Locale locale)
  {
    if (patientWeight == null)
    {
      return createResultWithErrorMessage(Dictionary.getEntry("paracetamol.patient.weight.missing", locale));
    }

    final List<TherapyDto> basketTherapiesWithParacetamol = basketTherapies
        .stream()
        .filter(this::isTherapyWithParacetamol)
        .collect(Collectors.toList());

    if (basketTherapiesWithParacetamol.isEmpty())
    {
      final ParacetamolRuleResult medicationIngredientRuleDto = new ParacetamolRuleResult();
      medicationIngredientRuleDto.setQuantityOk(true);
      return medicationIngredientRuleDto;
    }

    final Set<Long> medicationIdsWithParacetamolIngredient =
        medicationsValueHolderProvider.getMedicationIdsWithIngredientRule(
            MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final Map<Long, MedicationDataDto> medicationDataDtoWithParacetamolMap =
        medicationsValueHolderProvider.getAllMedicationDataMap(medicationIdsWithParacetamolIngredient);

    final List<TherapyDto> therapies = new ArrayList<>(basketTherapiesWithParacetamol);

    final double ingredientQuantityInTherapies = ingredientCalculator.calculateIngredientQuantityInTherapies(
        therapies,
        medicationDataDtoWithParacetamolMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        MG);

    final List<NamedExternalDto> basketParacetamolMedications = basketTherapiesWithParacetamol
        .stream()
        .flatMap(t -> t.getMedications().stream())
        .map(m -> new NamedExternalDto(String.valueOf(m.getId()), m.getName()))
        .collect(Collectors.toList());

    return calculateRuleResult(
        ingredientQuantityInTherapies,
        patientWeight,
        patientAgeInYears,
        basketParacetamolMedications,
        locale);
  }

  private boolean isTherapyWithParacetamol(final TherapyDto therapyDto)
  {
    return therapyDto.getMedications()
        .stream()
        .map(m -> medicationsValueHolder.getMedications().get(m.getId()))
        .anyMatch(m -> m != null && m.getMedicationIngredients()
            .stream()
            .anyMatch(i -> i.getIngredientRule() == MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE));
  }

  private ParacetamolRuleResult applyRuleForAdministration(
      final TherapyDoseDto currentAdministrationTherapyDoseDto,
      final String administrationId,
      final String taskId,
      final TherapyDto currentTherapyDto,
      final Double patientWeight,
      final Long patientAgeInYears,
      final Interval searchInterval,
      final String patientId,
      final Locale locale)
  {
    if (patientWeight == null)
    {
      return createResultWithErrorMessage(Dictionary.getEntry("paracetamol.patient.weight.missing", locale));
    }

    final List<InpatientPrescription> prescriptions = medicationsOpenEhrDao.findInpatientPrescriptions(
        patientId,
        Intervals.infiniteFrom(searchInterval.getStart()));

    final Set<String> therapyCompositionUids = prescriptions
        .stream()
        .map(EhrComposition::getUid)
        .map(TherapyIdUtils::getCompositionUidWithoutVersion)
        .collect(Collectors.toSet());

    final List<MedicationAdministration> administrations = medicationsOpenEhrDao.getMedicationAdministrations(
        patientId,
        therapyCompositionUids,
        null,
        true);

    final Set<Long> medicationIdsWithParacetamol = medicationsValueHolderProvider.getMedicationIdsWithIngredientRule(
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final Multimap<String, AdministrationDto> paracetamolAdministrationDtoMap = extractGivenAdministrationsWithMedications(
        prescriptions,
        administrations,
        administrationId,
        medicationIdsWithParacetamol);

    final Map<Long, MedicationDataDto> paracetamolMedicationDataMap = medicationsValueHolderProvider.getAllMedicationDataMap(
        new HashSet<>(medicationIdsWithParacetamol));

    final Map<String, TherapyDto> therapyDtoMap = buildTherapyDtosForTherapyIds(
        prescriptions,
        paracetamolAdministrationDtoMap.keySet());

    final double ingredientQuantity = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        currentAdministrationTherapyDoseDto,
        currentTherapyDto,
        paracetamolAdministrationDtoMap,
        therapyDtoMap,
        paracetamolMedicationDataMap,
        searchInterval,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        MG);

    final Collection<String> therapyIdsWithParacetamol = getTherapyIdsWithMedicationIds(
        prescriptions,
        medicationIdsWithParacetamol);

    final Opt<DateTime> lastAdministrationTime = getMostRecentAdministrationTime(
        searchInterval,
        paracetamolAdministrationDtoMap.values(),
        EnumSet.of(AdministrationTypeEnum.START, AdministrationTypeEnum.ADJUST_INFUSION));

    final Opt<DateTime> mostRecentPlannedTaskTimeAfterLastAdministration = getMostRecentPlannedTaskTime(
        patientId,
        taskId,
        therapyIdsWithParacetamol,
        searchInterval,
        lastAdministrationTime,
        EnumSet.of(AdministrationTypeEnum.START, AdministrationTypeEnum.ADJUST_INFUSION));

    final Opt<DateTime> lastParacetamolActionTime = mostRecentPlannedTaskTimeAfterLastAdministration.or(() -> lastAdministrationTime);
    final boolean lastParacetamolActionAdministered = !mostRecentPlannedTaskTimeAfterLastAdministration.isPresent();

    return calculateAdministrationRuleResult(
        ingredientQuantity,
        searchInterval,
        patientWeight,
        patientAgeInYears,
        lastParacetamolActionTime,
        lastParacetamolActionAdministered,
        locale);
  }

  private Opt<DateTime> getMostRecentAdministrationTime(
      final Interval searchInterval,
      final Collection<AdministrationDto> administrations,
      final EnumSet<AdministrationTypeEnum> administrationTypes)
  {
    return Opt.from(
        administrations
            .stream()
            .filter(t -> administrationTypes.contains(t.getAdministrationType()))
            .map(AdministrationDto::getAdministrationTime)
            .filter(t -> searchInterval.contains(t) || searchInterval.getEnd().equals(t))
            .max(Comparator.naturalOrder()));
  }

  private Opt<DateTime> getMostRecentPlannedTaskTime(
      final String patientId,
      final String excludeTaskId,
      final Collection<String> therapyIds,
      final Interval searchInterval,
      final Opt<DateTime> after,
      final EnumSet<AdministrationTypeEnum> administrationTypes)
  {
    return Opt.from(
        medicationsTasksProvider.findAdministrationTasks(patientId, therapyIds, searchInterval, false)
            .stream()
            .filter(t -> administrationTypes.contains(t.getAdministrationTypeEnum()))
            .filter(t -> excludeTaskId == null || !excludeTaskId.equals(t.getTaskId()))
            .map(AdministrationTaskDto::getPlannedAdministrationTime)
            .filter(time -> after.isAbsent() || time.isAfter(after.get()))
            .max(Comparator.naturalOrder()));
  }

  private Collection<String> getTherapyIdsWithMedicationIds(
      final Collection<InpatientPrescription> prescriptions,
      final Collection<Long> medicationIdsWithParacetamol)
  {
    return prescriptions
        .stream()
        .filter(p -> MedicationsEhrUtils.getMedicationIds(p.getMedicationOrder())
            .stream()
            .anyMatch(medicationIdsWithParacetamol::contains))
        .map(TherapyIdUtils::createTherapyId)
        .collect(Collectors.toList());
  }

  // returns map of therapyId and administrationDto List
  private Multimap<String, AdministrationDto> extractGivenAdministrationsWithMedications(
      final List<InpatientPrescription> prescriptions,
      final List<MedicationAdministration> administrations,
      final String excludeAdministrationId,
      final Set<Long> medicationIdsWithParacetamol)
  {
    final Multimap<String, AdministrationDto> administrationDtoMultimap = ArrayListMultimap.create();

    administrations
        .stream()
        .filter(a -> a.getMedicationManagement() != null)
        .forEach(administrationComp -> {

          final MedicationManagement action = administrationComp.getMedicationManagement();
          final String compositionUid = Opt
              .resolve(() -> administrationComp
                  .getInstructionDetails()
                  .getInstructionId()
                  .getId()
                  .getValue())
              .orElseThrow(() -> new IllegalStateException("Could not retrieve instruction composition id from composition!"));

          final String therapyId = TherapyIdUtils.createTherapyId(compositionUid);

          final AdministrationDto administration = administrationFromEhrConverter.convertToAdministrationDto(
              administrationComp,
              PrescriptionsEhrUtils.extractPrescriptionByTherapyId(therapyId, prescriptions));

          final boolean includeAdministration = excludeAdministrationId == null
              || !excludeAdministrationId.equals(administration.getAdministrationId());

          final boolean given = AdministrationResultEnum.ADMINISTERED.contains(administration.getAdministrationResult());

          final boolean containsParacetamol = MedicationsEhrUtils.getMedicationIds(action)
              .stream()
              .anyMatch(medicationIdsWithParacetamol::contains);

          if (given && containsParacetamol && includeAdministration)
          {
            administrationDtoMultimap.put(therapyId, administration);
          }
        });

    return administrationDtoMultimap;
  }

  final ParacetamolRuleResult calculateAdministrationRuleResult(
      final double dailyDosage,
      final Interval searchInterval,
      final double patientWeight,
      final Long patientAge,
      final Opt<DateTime> lastParacetamolActionTime,
      final boolean lastParacetamolActionAdministered,
      final Locale locale)
  {
    final ParacetamolRuleResult result = calculateRuleResult(
        dailyDosage,
        patientWeight,
        patientAge,
        Collections.emptyList(),
        locale);

    setBetweenDosesLimitRule(searchInterval, lastParacetamolActionTime, lastParacetamolActionAdministered, result);
    return result;
  }

  private void setBetweenDosesLimitRule(
      final Interval searchInterval,
      final Opt<DateTime> lastParacetamolActionTime,
      final boolean lastParacetamolActionAdministered,
      final ParacetamolRuleResult paracetamolRuleResult)
  {
    final DateTime lastAllowedAdministrationTime = searchInterval.getEnd().minusHours(BETWEEN_DOSES_LIMIT);

    if (lastParacetamolActionTime.isPresent()
        && (lastParacetamolActionTime.get().isAfter(lastAllowedAdministrationTime)
        || lastParacetamolActionTime.get().isEqual(lastAllowedAdministrationTime)))
    {
      paracetamolRuleResult.setLastTaskTimestamp(lastParacetamolActionTime.get());
      paracetamolRuleResult.setBetweenDosesTimeOk(false);
      paracetamolRuleResult.setLastTaskAdministered(lastParacetamolActionAdministered);
    }
  }

  private Map<String, TherapyDto> buildTherapyDtosForTherapyIds(
      final List<InpatientPrescription> prescriptions,
      final Set<String> therapyIds)
  {
    final Map<String, TherapyDto> therapyDtoMap = new HashMap<>();
    for (final String therapyId : therapyIds)
    {
      final InpatientPrescription prescription = PrescriptionsEhrUtils.extractPrescriptionByTherapyId(therapyId, prescriptions);
      final MedicationOrder order = prescription.getMedicationOrder();

      final TherapyDto therapyDto = therapyConverter.convertToTherapyDto(
          order,
          prescription.getUid(),
          DataValueUtils.getDateTime(prescription.getContext().getStartTime()));

      therapyDtoMap.put(therapyId, therapyDto);
    }
    return therapyDtoMap;
  }

  private ParacetamolRuleResult applyRuleForTherapy(
      final TherapyDto therapyDto,
      final List<MedicationDataDto> medicationDataDtoList,
      final Double patientWeight,
      final Long patientAgeInYears,
      final Locale locale)
  {
    if (patientWeight == null)
    {
      return createResultWithErrorMessage(Dictionary.getEntry("paracetamol.patient.weight.missing", locale));
    }

    final Map<Long, MedicationDataDto> medicationDataDtoMap = medicationDataDtoList
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(
            medicationDataDto -> medicationDataDto.getMedication().getId(),
            medicationDataDto -> medicationDataDto,
            (m1, m2) -> m1));

    final double ingredientQuantityInTherapies = ingredientCalculator.calculateIngredientQuantityInTherapies(
        Collections.singletonList(therapyDto),
        medicationDataDtoMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        MG);

    final List<NamedExternalDto> medications = therapyDto.getMedications()
        .stream()
        .map(m -> new NamedExternalDto(String.valueOf(m.getId()), m.getName()))
        .collect(Collectors.toList());

    return calculateRuleResult(
        ingredientQuantityInTherapies,
        patientWeight,
        patientAgeInYears,
        medications,
        locale);
  }

  ParacetamolRuleResult calculateRuleResult(
      final double dose,
      final double patientWeight,
      final Long patientAge,
      final List<NamedExternalDto> medications,
      final Locale locale)
  {
    final ParacetamolRuleResult result = new ParacetamolRuleResult();
    result.setMedications(medications);

    final boolean patientUnderaged = isPatientUnderage(patientAge);
    final double underageDoseLimit = getUnderageDoseLimit(patientWeight);
    final boolean underageOverdose = patientUnderaged && dose > underageDoseLimit;

    final double adultDoseLimit = getAdultDoseLimit(patientWeight);
    final boolean adultOverdose = dose > adultDoseLimit;

    result.setQuantityOk(!underageOverdose && !adultOverdose);
    result.setAdultRulePercentage(Math.ceil(dose / adultDoseLimit * 100));
    result.setUnderageRulePercentage(patientUnderaged ? Math.ceil(dose / underageDoseLimit * 100) : null);

    if (patientUnderaged && result.getUnderageRulePercentage() > result.getAdultRulePercentage())
    {
      result.setRule(getUnderageRuleDescription(locale));
    }
    if (adultOverdose)
    {
      result.setRule(getAdultRuleDescription(locale, adultDoseLimit));
    }

    return result;
  }

  private String getAdultRuleDescription(final Locale locale, final Double maxDailyLimit)
  {
    return unitsConverter.convert(maxDailyLimit, MG, G) + unitsProvider.getDisplayName(G) + " " + Dictionary.getEntry("per.day", locale);
  }

  private String getUnderageRuleDescription(final Locale locale)
  {
    return (int)UNDERAGE_MAX_MG_PER_KG_PER_DAY + "mg/kg/" + Dictionary.getEntry("day", locale);
  }

  private ParacetamolRuleResult createResultWithErrorMessage(final String errorMessage)
  {
    final ParacetamolRuleResult paracetamolRuleResult = new ParacetamolRuleResult();
    paracetamolRuleResult.setQuantityOk(false);
    paracetamolRuleResult.setErrorMessage(errorMessage);
    return paracetamolRuleResult;
  }

  private double getAdultDoseLimit(final double referenceWeight)
  {
    return referenceWeight > KG_LEVEL_LIMIT ? MAX_MG_PER_DAY_KG_LEVEL_2 : MAX_MG_PER_DAY_KG_LEVEL_1;
  }

  private double getUnderageDoseLimit(final double patientWeight)
  {
    return patientWeight * UNDERAGE_MAX_MG_PER_KG_PER_DAY;
  }

  private boolean isPatientUnderage(final Long patientAgeInYears)
  {
    return patientAgeInYears != null && patientAgeInYears < UNDERAGE_LIMIT;
  }
}
