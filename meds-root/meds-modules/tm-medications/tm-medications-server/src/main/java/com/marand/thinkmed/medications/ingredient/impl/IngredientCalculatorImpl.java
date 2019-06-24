package com.marand.thinkmed.medications.ingredient.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.dose.DoseUtils;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.PrescribingDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import com.marand.thinkmed.medications.ingredient.IngredientCalculator;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medications.units.converter.UnitsConverter;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Minutes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.marand.thinkmed.medications.dto.unit.KnownUnitType.ML;
import static com.marand.thinkmed.medications.dto.unit.UnitGroupEnum.LIQUID_UNIT;
import static java.util.stream.Collectors.toList;

/**
 * @author Nejc Korasa
 */

@Component
public class IngredientCalculatorImpl implements IngredientCalculator
{
  private final UnitsConverter unitsConverter;
  private final DoseUtils doseUtils;

  @Autowired
  public IngredientCalculatorImpl(
      final UnitsConverter unitsConverter,
      final DoseUtils doseUtils)
  {
    this.unitsConverter = unitsConverter;
    this.doseUtils = doseUtils;
  }

  @Override
  public double calculateIngredientQuantityInTherapies(
      final @NonNull List<TherapyDto> therapies,
      final @NonNull Map<Long, MedicationDataDto> medicationDataDtoMap,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final @NonNull KnownUnitType knownUnit)
  {
    Preconditions.checkArgument(
        ingredientId != null || ingredientRuleEnum != null,
        "ingredientId or ingredientRuleEnum must be defined");

    double quantitySum = 0.0;
    for (final TherapyDto therapy : therapies)
    {
      final MedicationOrderFormType medicationOrderFormType = therapy.getMedicationOrderFormType();

      Double quantity = null;
      if (MedicationOrderFormType.SIMPLE_ORDERS.contains(medicationOrderFormType))
      {
        final Long mainMedicationId = therapy.getMainMedicationId();
        if (medicationContainedInMap(medicationDataDtoMap, mainMedicationId))
        {
          quantity = calculateIngredientQuantityForSimpleTherapy(
              therapy,
              medicationDataDtoMap.get(mainMedicationId),
              ingredientId,
              ingredientRuleEnum,
              knownUnit);
        }
      }
      else if (medicationOrderFormType == MedicationOrderFormType.COMPLEX)
      {
        quantity = calculateIngredientQuantityForComplexTherapy(
            therapy,
            medicationDataDtoMap,
            ingredientId,
            ingredientRuleEnum,
            knownUnit);
      }
      else if (medicationOrderFormType != MedicationOrderFormType.OXYGEN)
      {
        throw new UnsupportedOperationException("this method only support SIMPLE or COMPLEX orders");
      }

      if (quantity != null)
      {
        quantitySum += quantity;
      }
    }

    return quantitySum;
  }

  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  private boolean medicationContainedInMap(final Map<Long, MedicationDataDto> medicationDataDtoMap, final Long medicationId)
  {
    return medicationId != null && medicationDataDtoMap.get(medicationId) != null;
  }

  private Double calculateIngredientQuantityForSimpleTherapy(
      final TherapyDto therapyDto,
      final MedicationDataDto medicationDataDto,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final KnownUnitType knownUnit)
  {
    final SimpleTherapyDto simpleTherapyDto = (SimpleTherapyDto)therapyDto;

    final Double quantity;
    final boolean variable = simpleTherapyDto instanceof VariableSimpleTherapyDto;
    final Integer timesPerDay;

    if (variable)
    {
      quantity = getSimpleVariablePerDay(((VariableSimpleTherapyDto)simpleTherapyDto).getTimedDoseElements());
      timesPerDay = 1;
    }
    else
    {
      final SimpleDoseElementDto doseElement = ((ConstantSimpleTherapyDto)simpleTherapyDto).getDoseElement();

      if (doseElement == null)
      {
        return null;
      }

      quantity = doseElement.getQuantity() != null ? doseElement.getQuantity() : getMaxDoseRangeQuantity(doseElement);

      final Integer calculatedTimesPerDay = getTimesPerDay(
          simpleTherapyDto.getDosingFrequency(),
          therapyDto.getMaxDailyFrequency());

      if (calculatedTimesPerDay == null)
      {
        return null;
      }
      else
      {
        timesPerDay = calculatedTimesPerDay;
      }
    }

    if (quantity == null)
    {
      return null;
    }

    if (medicationDataDto.getAdministrationUnit() != null)
    {
      return calculateIngredientQuantityForAdministrationUnit(
          medicationDataDto,
          ingredientId,
          ingredientRuleEnum,
          knownUnit,
          timesPerDay * quantity);
    }

    final PrescribingDoseDto prescribingDose = medicationDataDto.getPrescribingDose();
    if (prescribingDose != null)
    {
      final double ingredientPercentage = getIngredientPercentage(
          medicationDataDto,
          ingredientId,
          ingredientRuleEnum,
          knownUnit);

      return unitsConverter.convert(
          timesPerDay * quantity * ingredientPercentage,
          prescribingDose.getNumeratorUnit(),
          knownUnit);
    }
    return 0.0;
  }

  private List<MedicationIngredientDto> getSearchIngredients(
      final MedicationDataDto medicationDataDto,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum)
  {
    return medicationDataDto.getMedicationIngredients()
            .stream()
            .filter(i -> isSearchIngredient(ingredientId, ingredientRuleEnum, i))
            .collect(toList());
  }

  private Double getMaxDoseRangeQuantity(final SimpleDoseElementDto doseElement)
  {
    return Opt.resolve(() -> doseElement.getDoseRange().getMaxNumerator()).orElse(null);
  }

  private Double calculateIngredientQuantityForComplexTherapy(
      final TherapyDto therapyDto,
      final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final KnownUnitType knownUnit)
  {
    final ComplexTherapyDto complexTherapyDto = (ComplexTherapyDto)therapyDto;

    final boolean continuousInfusion = complexTherapyDto.isContinuousInfusion();

    //noinspection IfMayBeConditional
    if (continuousInfusion)
    {
      return calculateIngredientQuantityForContinuousInfusion(
          complexTherapyDto,
          medicationDataDtoMap,
          ingredientId,
          ingredientRuleEnum,
          knownUnit);
    }
    else
    {
      return calculateIngredientQuantityForNormalInfusion(
          complexTherapyDto,
          medicationDataDtoMap,
          ingredientId,
          ingredientRuleEnum,
          knownUnit);
    }
  }

  private Double calculateIngredientQuantityForNormalInfusion(
      final ComplexTherapyDto complexTherapyDto,
      final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final KnownUnitType knownUnit)
  {
    final double ingredientQuantity = getIngredientQuantityOfInfusionIngredients(
        medicationDataDtoMap,
        complexTherapyDto.getIngredientsList(),
        ingredientId,
        ingredientRuleEnum,
        knownUnit);

    final Integer timesPerDay = getTimesPerDay(
        complexTherapyDto.getDosingFrequency(),
        complexTherapyDto.getMaxDailyFrequency());

    return timesPerDay != null ? ingredientQuantity * timesPerDay : null;
  }

  private Double calculateIngredientQuantityForContinuousInfusion(
      final ComplexTherapyDto complexTherapyDto,
      final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final KnownUnitType knownUnit)
  {
    final List<InfusionIngredientDto> infusionIngredientDtoList = complexTherapyDto.getIngredientsList();

    // in ml per hour
    final Double rate = getContinuousInfusionRate(complexTherapyDto);

    if (rate != null)
    {
      //noinspection IfMayBeConditional
      if (infusionIngredientDtoList.size() == 1)
      {
        return calculateIngredientQuantityForContinuousInfusionWithOneIngredient(
            medicationDataDtoMap,
            infusionIngredientDtoList,
            rate,
            ingredientId,
            ingredientRuleEnum,
            knownUnit);
      }
      else
      {
        return calculateIngredientQuantityForContinuousInfusionWithMultipleIngredients(
            complexTherapyDto,
            medicationDataDtoMap,
            infusionIngredientDtoList,
            rate,
            ingredientId,
            ingredientRuleEnum,
            knownUnit);
      }
    }
    else
    {
      return null;
    }
  }

  private Double calculateIngredientQuantityForContinuousInfusionWithOneIngredient(
      final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final List<InfusionIngredientDto> infusionIngredientDtoList,
      final Double rate,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final KnownUnitType knownUnit)
  {
    final Long medicationId = infusionIngredientDtoList.get(0).getMedication().getId();
    if (medicationContainedInMap(medicationDataDtoMap, medicationId))
    {
      final MedicationDataDto medicationDataDto = medicationDataDtoMap.get(medicationId);

      final Double ingredientQuantityPerMl = getIngredientQuantityInOneMl(
          medicationDataDto,
          ingredientId,
          ingredientRuleEnum,
          knownUnit);

      return ingredientQuantityPerMl * rate * 24;
    }

    return null;
  }

  private Double calculateIngredientQuantityForContinuousInfusionWithMultipleIngredients(
      final ComplexTherapyDto complexTherapyDto,
      final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final List<InfusionIngredientDto> infusionIngredientDtoList,
      final Double rate,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final KnownUnitType knownUnit)
  {
    if (complexTherapyDto.getVolumeSum() != null)
    {
      final double ingredientQuantity = getIngredientQuantityOfInfusionIngredients(
          medicationDataDtoMap,
          infusionIngredientDtoList,
          ingredientId,
          ingredientRuleEnum,
          knownUnit);

      if (unitsConverter.isConvertible(complexTherapyDto.getVolumeSumUnit(), ML))
      {
        final Double volumeSumInMl = unitsConverter.convert(
            complexTherapyDto.getVolumeSum(),
            complexTherapyDto.getVolumeSumUnit(),
            ML);

        return ingredientQuantity / volumeSumInMl * rate * 24;
      }
    }

    return null;
  }

  @Override
  public double calculateIngredientQuantityInAdministrations(
      final TherapyDoseDto currentAdministrationTherapyDoseDto,
      final TherapyDto currentTherapyDto,
      final @NonNull Multimap<String, AdministrationDto> administrationDtoMap,
      final @NonNull Map<String, TherapyDto> therapyDtoMap,
      final @NonNull Map<Long, MedicationDataDto> medicationDataMap,
      final @NonNull Interval searchInterval,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule,
      final @NonNull KnownUnitType knownUnit)
  {
    Preconditions.checkArgument(
        ingredientId != null || ingredientRule != null,
        "ingredientId or ingredientRuleEnum must be defined");

    double quantitySumInUnit = 0.0;

    final Multimap<String, AdministrationDto> sortedAdministrationsMultimap = sortAdministrationsByTime(administrationDtoMap);
    for (final String therapyId : sortedAdministrationsMultimap.keySet())
    {
      final Collection<AdministrationDto> administrations = administrationDtoMap.get(therapyId);
      for (final AdministrationDto administration : administrations)
      {
        final TherapyDto therapyDto = therapyDtoMap.get(therapyId);
        final AdministrationTypeEnum administrationType = administration.getAdministrationType();

        final boolean correctType = administrationType == AdministrationTypeEnum.START
            || administrationType == AdministrationTypeEnum.ADJUST_INFUSION
            || administrationType == AdministrationTypeEnum.BOLUS;

        final boolean correctResult = AdministrationResultEnum.ADMINISTERED.contains(administration.getAdministrationResult());

        if (correctType && correctResult)
        {
          final double quantity = calculateIngredientQuantityForAdministration(
              sortedAdministrationsMultimap,
              medicationDataMap,
              searchInterval,
              therapyId,
              ingredientId,
              ingredientRule,
              administration,
              therapyDto,
              knownUnit);

          quantitySumInUnit += quantity;
        }
      }
    }

    if (currentTherapyDto != null && currentAdministrationTherapyDoseDto != null)
    {
      quantitySumInUnit += getIngredientQuantityOfCurrentAdministration(
          currentAdministrationTherapyDoseDto,
          currentTherapyDto,
          medicationDataMap,
          ingredientId,
          ingredientRule,
          knownUnit);
    }

    return quantitySumInUnit;
  }

  private double calculateIngredientQuantityForAdministration(
      final @NonNull Multimap<String, AdministrationDto> administrationDtoMap,
      final @NonNull Map<Long, MedicationDataDto> medicationDataMap,
      final @NonNull Interval searchInterval,
      final String therapyId,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule,
      final AdministrationDto administration,
      final TherapyDto therapyDto,
      final KnownUnitType knownUnit)
  {
    final TherapyDoseDto administeredDose = getAdministeredDose(administration);
    final TherapyDoseTypeEnum therapyDoseTypeEnum = administeredDose.getTherapyDoseTypeEnum();
    final boolean isInSearchInterval = searchInterval.contains(administration.getAdministrationTime())
        || administration.getAdministrationTime().equals(searchInterval.getEnd());

    if (therapyDoseTypeEnum == TherapyDoseTypeEnum.QUANTITY && isInSearchInterval)
    {
      final Long mainMedicationId = therapyDto.getMainMedicationId();
      if (medicationContainedInMap(medicationDataMap, mainMedicationId))
      {
        final MedicationDataDto medicationDataDto = medicationDataMap.get(mainMedicationId);
        return calculateIngredientQuantityForQuantityDoseType(
            administeredDose,
            medicationDataDto,
            ingredientId,
            ingredientRule,
            knownUnit);
      }
    }
    else if (TherapyDoseTypeEnum.WITH_RATE.contains(therapyDoseTypeEnum))
    {
      return calculateIngredientQuantityForRateAdministration(
          administrationDtoMap,
          medicationDataMap,
          searchInterval,
          therapyId,
          administration,
          (ComplexTherapyDto)therapyDto,
          administeredDose,
          ingredientId,
          ingredientRule,
          knownUnit);
    }
    else if (therapyDoseTypeEnum == TherapyDoseTypeEnum.VOLUME_SUM && isInSearchInterval)
    {
      return calculateIngredientQuantityForVolumeSumDoseType(
          medicationDataMap,
          (ComplexTherapyDto)therapyDto,
          administeredDose,
          ingredientId,
          ingredientRule,
          knownUnit);
    }
    return 0.0;
  }

  private TherapyDoseDto getAdministeredDose(final AdministrationDto administration)
  {
    final AdministrationTypeEnum administrationType = administration.getAdministrationType();

    return administrationType == AdministrationTypeEnum.START ?
           ((StartAdministrationDto)administration).getAdministeredDose() :
           ((AdjustInfusionAdministrationDto)administration).getAdministeredDose();
  }

  private double getIngredientQuantityOfCurrentAdministration(
      final TherapyDoseDto currentAdministrationTherapyDoseDto,
      final TherapyDto currentTherapyDto,
      final Map<Long, MedicationDataDto> medicationDataMap,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule,
      final KnownUnitType knownUnit)
  {
    if (currentAdministrationTherapyDoseDto.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.QUANTITY)
    {
      final Long medicationId = currentTherapyDto.getMainMedicationId();
      if (medicationContainedInMap(medicationDataMap, medicationId))
      {
        return calculateIngredientQuantityForQuantityDoseType(
            currentAdministrationTherapyDoseDto,
            medicationDataMap.get(medicationId),
            ingredientId,
            ingredientRule,
            knownUnit);
      }
    }
    else if (currentAdministrationTherapyDoseDto.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.VOLUME_SUM)
    {
      return calculateIngredientQuantityForVolumeSumDoseType(
          medicationDataMap,
          (ComplexTherapyDto)currentTherapyDto,
          currentAdministrationTherapyDoseDto,
          ingredientId,
          ingredientRule,
          knownUnit);
    }

    return 0.0;
  }

  private double calculateIngredientQuantityForQuantityDoseType(
      final TherapyDoseDto administeredDose,
      final MedicationDataDto medicationDataDto,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule,
      final KnownUnitType knownUnit)
  {
    final Double administeredQuantity = administeredDose.getNumerator();
    final String administeredUnit = administeredDose.getNumeratorUnit();

    if (administeredQuantity == null || administeredUnit == null)
    {
      return 0.0;
    }

    if (medicationDataDto.getAdministrationUnit() != null)
    {
      return calculateIngredientQuantityForAdministrationUnit(medicationDataDto, ingredientId, ingredientRule, knownUnit, administeredQuantity);
    }

    if (unitsConverter.isConvertible(administeredUnit, knownUnit))
    {
      final double ingredientPercentage = getIngredientPercentage(medicationDataDto, ingredientId, ingredientRule, knownUnit);
      final double convertedToUnit = unitsConverter.convert(administeredQuantity, administeredUnit, knownUnit);
      return ingredientPercentage * convertedToUnit;
    }

    return 0.0;
  }

  private double calculateIngredientQuantityForAdministrationUnit(
      final MedicationDataDto medicationDataDto,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule,
      final KnownUnitType knownUnit,
      final double quantity)
  {
    final List<MedicationIngredientDto> searchIngredients = getSearchIngredients(
        medicationDataDto,
        ingredientId,
        ingredientRule);

    final PrescribingDoseDto searchIngredientsDose = doseUtils.buildDoseFromIngredients(searchIngredients);
    if (searchIngredientsDose != null)
    {
      return unitsConverter.convert(
          quantity * medicationDataDto.getAdministrationUnitFactor() * searchIngredientsDose.getNumerator(),
          searchIngredientsDose.getNumeratorUnit(),
          knownUnit);
    }
    return 0.0;
  }

  private Multimap<String, AdministrationDto> sortAdministrationsByTime(final Multimap<String, AdministrationDto> administrationDtoMap)
  {
    final Multimap<String, AdministrationDto> sorted = ArrayListMultimap.create();
    administrationDtoMap.keySet().forEach(
        t ->
            sorted.putAll(t, administrationDtoMap.get(t)
                .stream()
                .sorted(Comparator.comparing(AdministrationDto::getAdministrationTime))
                .collect(toList())));

    return sorted;
  }

  private double calculateIngredientQuantityForRateAdministration(
      final Multimap<String, AdministrationDto> administrationDtoMap,
      final Map<Long, MedicationDataDto> medicationDataMap,
      final Interval searchInterval,
      final String therapyId,
      final AdministrationDto administration,
      final ComplexTherapyDto therapyDto,
      final TherapyDoseDto administeredDose,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule,
      final KnownUnitType knownUnit)
  {
    final Double quantity;
    final Double rate = administeredDose.getNumerator();

    final int minutesDuration = calculateRateDurationInterval(
        administration,
        searchInterval,
        therapyId,
        administrationDtoMap);

    final List<InfusionIngredientDto> infusionIngredientDtoList = therapyDto.getIngredientsList();

    if (infusionIngredientDtoList.size() == 1)
    {
      final Long ingredientMedicationId = infusionIngredientDtoList.get(0).getMedication().getId();
      if (medicationContainedInMap(medicationDataMap, ingredientMedicationId))
      {
        final double ingredientQuantityInMlPerHour = getIngredientQuantityInOneMl(
            medicationDataMap.get(ingredientMedicationId),
            ingredientId,
            ingredientRule,
            knownUnit);

        quantity = ingredientQuantityInMlPerHour * rate * minutesDuration / 60;
      }
      else
      {
        quantity = 0.0;
      }
    }
    else
    {
      final double ingredientQuantity = getIngredientQuantityOfInfusionIngredients(
          medicationDataMap,
          infusionIngredientDtoList,
          ingredientId,
          ingredientRule,
          knownUnit);

      if (unitsConverter.isConvertible(therapyDto.getVolumeSumUnit(), ML))
      {
        final Double volumeSumInMl = unitsConverter.convert(
            therapyDto.getVolumeSum(),
            therapyDto.getVolumeSumUnit(),
            ML);

        quantity = ingredientQuantity / volumeSumInMl * rate * minutesDuration / 60;
      }
      else
      {
        quantity = 0.0;
      }
    }

    return quantity;
  }

  private int calculateRateDurationInterval(
      final AdministrationDto administrationDto,
      final Interval searchInterval,
      final String therapyId,
      final Multimap<String, AdministrationDto> administrationDtoMap)
  {
    if (administrationDto.getAdministrationTime().isAfter(searchInterval.getEnd()))
    {
      return 0;
    }

    final List<AdministrationDto> laterAdministrationsForTherapy = getLaterAdministrationsForTherapy(
        therapyId,
        administrationDto,
        administrationDtoMap);

    if (laterAdministrationsForTherapy.isEmpty())
    {
      final Interval administrationDurationInterval =
          new Interval(administrationDto.getAdministrationTime(), searchInterval.getEnd());

      final Interval overlapInterval = searchInterval.overlap(administrationDurationInterval);
      return Minutes.minutesIn(overlapInterval).getMinutes();
    }
    else
    {
      DateTime intervalEnd = searchInterval.getEnd();

      for (final AdministrationDto laterAdministrationDto : laterAdministrationsForTherapy)
      {
        if (laterAdministrationDto.getAdministrationType() == AdministrationTypeEnum.STOP
            || laterAdministrationDto.getAdministrationType() == AdministrationTypeEnum.ADJUST_INFUSION)
        {
          final DateTime laterAdministrationTime = laterAdministrationDto.getAdministrationTime();
          if (laterAdministrationTime != null && laterAdministrationTime.isBefore(intervalEnd))
          {
            intervalEnd = laterAdministrationTime;
          }
        }
      }

      final Interval administrationDurationInterval = new Interval(administrationDto.getAdministrationTime(), intervalEnd);
      final Interval overlapInterval = searchInterval.overlap(administrationDurationInterval);
      return Minutes.minutesIn(overlapInterval).getMinutes();
    }
  }

  private List<AdministrationDto> getLaterAdministrationsForTherapy(
      final String therapyId,
      final AdministrationDto searchAdministrationDto,
      final Multimap<String, AdministrationDto> administrationDtoMap)
  {
    return administrationDtoMap.get(therapyId)
        .stream()
        .filter(administration -> administration.getTherapyId().equals(searchAdministrationDto.getTherapyId())
            && !administration.getAdministrationId().equals(searchAdministrationDto.getAdministrationId())
            && administration.getAdministrationTime().isAfter(searchAdministrationDto.getAdministrationTime()))
        .collect(toList());
  }

  private double calculateIngredientQuantityForVolumeSumDoseType(
      final Map<Long, MedicationDataDto> medicationDataMap,
      final ComplexTherapyDto therapyDto,
      final TherapyDoseDto administeredDose,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRule,
      final KnownUnitType knownUnit)
  {
    final List<InfusionIngredientDto> infusionIngredientDtoList = therapyDto.getIngredientsList();

    final double ingredientQuantity = getIngredientQuantityOfInfusionIngredients(
        medicationDataMap,
        infusionIngredientDtoList,
        ingredientId,
        ingredientRule,
        knownUnit);

    if (unitsConverter.isGroupType(therapyDto.getVolumeSumUnit(), LIQUID_UNIT)
        && unitsConverter.isGroupType(administeredDose.getNumeratorUnit(), LIQUID_UNIT))
    {
      final double therapyVolumeSum = unitsConverter.convert(
          therapyDto.getVolumeSum(),
          therapyDto.getVolumeSumUnit(),
          ML);

      final double administrationVolumeSum = unitsConverter.convert(
          administeredDose.getNumerator(),
          administeredDose.getNumeratorUnit(),
          ML);

      return administrationVolumeSum / therapyVolumeSum * ingredientQuantity;
    }

    return 0.0;
  }

  private double getIngredientQuantityOfInfusionIngredients(
      final Map<Long, MedicationDataDto> medicationDataDtoMap,
      final List<InfusionIngredientDto> infusionIngredientDtoList,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final KnownUnitType knownUnit)
  {
    double ingredientQuantitySum = 0.0;
    for (final InfusionIngredientDto infusionIngredientDto : infusionIngredientDtoList)
    {
      final Long medicationId = infusionIngredientDto.getMedication().getId();
      if (medicationContainedInMap(medicationDataDtoMap, medicationId))
      {
        final MedicationDataDto medicationDataDto = medicationDataDtoMap.get(medicationId);
        if (medicationDataDto != null)
        {
          final double ingredientQuantity = getIngredientQuantityForInfusionIngredient(
              medicationDataDto,
              infusionIngredientDto,
              ingredientId,
              ingredientRuleEnum,
              knownUnit);

          ingredientQuantitySum += ingredientQuantity;
        }
      }
    }

    return ingredientQuantitySum;
  }

  private double getIngredientQuantityForInfusionIngredient(
      final MedicationDataDto medicationDataDto,
      final InfusionIngredientDto infusionIngredientDto,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final KnownUnitType knownUnit)
  {
    final Double quantity = infusionIngredientDto.getQuantity();
    if (quantity != null)
    {
      if (medicationDataDto.getAdministrationUnit() != null)
      {
        return calculateIngredientQuantityForAdministrationUnit(medicationDataDto, ingredientId, ingredientRuleEnum, knownUnit, quantity);
      }

      if (unitsConverter.isConvertible(infusionIngredientDto.getQuantityUnit(), knownUnit))
      {
        final double ingredientPercentage = getIngredientPercentage(medicationDataDto, ingredientId, ingredientRuleEnum, knownUnit);
        final double quantityInUnit = unitsConverter.convert(quantity, infusionIngredientDto.getQuantityUnit(), knownUnit);
        return ingredientPercentage * quantityInUnit;
      }
    }

    return 0.0;
  }

  private double getIngredientPercentage(
      final MedicationDataDto medicationDataDto,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final KnownUnitType knownUnit)
  {
    if (medicationDataDto.getMedicationIngredients().size() == 1)
    {
      return isSearchIngredient(ingredientId, ingredientRuleEnum, medicationDataDto.getMedicationIngredients().get(0))
             ? 1.0
             : 0.0;
    }

    double ingredientQuantitySum = 0.0;

    final PrescribingDoseDto prescribingDose = medicationDataDto.getPrescribingDose();
    if (prescribingDose != null && unitsConverter.isConvertible(prescribingDose.getNumeratorUnit(), knownUnit))
    {
      final double quantitySum = unitsConverter.convert(prescribingDose.getNumerator(), prescribingDose.getNumeratorUnit(), knownUnit);

      for (final MedicationIngredientDto medicationIngredientDto : medicationDataDto.getMedicationIngredients())
      {
        if (isSearchIngredient(ingredientId, ingredientRuleEnum, medicationIngredientDto)
            && unitsConverter.isConvertible(medicationIngredientDto.getStrengthNumeratorUnit(), knownUnit))
        {
          final double ingredientQuantity = unitsConverter.convert(
              medicationIngredientDto.getStrengthNumerator(),
              medicationIngredientDto.getStrengthNumeratorUnit(),
              knownUnit);

          ingredientQuantitySum += ingredientQuantity;
        }
      }

      return ingredientQuantitySum / quantitySum;
    }

    return ingredientQuantitySum;
  }

  private boolean isSearchIngredient(
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final MedicationIngredientDto medicationIngredientDto)
  {
    final boolean ruleEnumMatches = ingredientRuleEnum == null
        || medicationIngredientDto.getIngredientRule() == ingredientRuleEnum;

    final boolean ingredientIdMatches = ingredientId == null
        || medicationIngredientDto.getIngredientId() == ingredientId;

    return ruleEnumMatches && ingredientIdMatches;
  }

  private double getIngredientQuantityInOneMl(
      final MedicationDataDto medicationDataDto,
      final Long ingredientId,
      final MedicationRuleEnum ingredientRuleEnum,
      final KnownUnitType knownUnit)
  {
    double ingredientQuantity = 0.0;
    for (final MedicationIngredientDto medicationIngredientDto : medicationDataDto.getMedicationIngredients())
    {
      if (isSearchIngredient(ingredientId, ingredientRuleEnum, medicationIngredientDto)
          && unitsConverter.isConvertible(medicationIngredientDto.getStrengthNumeratorUnit(), knownUnit)
          && medicationIngredientDto.getStrengthDenominator() != null)
      {
        final double ingredientInUnit = unitsConverter.convert(
            medicationIngredientDto.getStrengthNumerator(),
            medicationIngredientDto.getStrengthNumeratorUnit(),
            knownUnit);

        if (unitsConverter.isGroupType(medicationIngredientDto.getStrengthDenominatorUnit(), LIQUID_UNIT))
        {
          final double ingredientMl = unitsConverter.convert(
              medicationIngredientDto.getStrengthDenominator(),
              medicationIngredientDto.getStrengthDenominatorUnit(),
              ML);

          ingredientQuantity += ingredientInUnit / ingredientMl;
        }
      }
    }

    return ingredientQuantity;
  }

  private double getInfusionRateForVariableComplexTherapy(final VariableComplexTherapyDto complexTherapyDto)
  {
    final List<TimedComplexDoseElementDto> timedDoseElements = complexTherapyDto.getTimedDoseElements();

    double durationInMinutes = 0.0;
    double mlQuantitySum = 0.0;
    for (int i = 0; i < timedDoseElements.size(); i++)
    {
      final boolean isLastElement = i == timedDoseElements.size() - 1;
      final TimedComplexDoseElementDto doseElementDto = timedDoseElements.get(i);

      final double currentDuration;
      if (isLastElement)
      {
        currentDuration = 1440 - durationInMinutes; // 1440 = 60 * 24
      }
      else
      {
        currentDuration = doseElementDto.getDoseElement().getDuration();
        durationInMinutes += currentDuration;
      }

      final double currentQuantity = doseElementDto.getDoseElement().getRate() * currentDuration / 60;
      mlQuantitySum += currentQuantity;
    }

    return mlQuantitySum;
  }

  private Double getSimpleVariablePerDay(final List<TimedSimpleDoseElementDto> timedDoseElements)
  {
    if (timedDoseElements.isEmpty())
    {
      return null;
    }

    return timedDoseElements.stream()
        .filter(t -> t.getDoseElement().getQuantity() != null)
        .mapToDouble(t -> t.getDoseElement().getQuantity())
        .sum();
  }

  private Integer getTimesPerDay(final DosingFrequencyDto dosingFrequency, final Integer maxDailyFrequency)
  {
    if (maxDailyFrequency != null)
    {
      return maxDailyFrequency;
    }
    if (dosingFrequency == null)
    {
      return null;
    }
    if (dosingFrequency.getType() == DosingFrequencyTypeEnum.DAILY_COUNT)
    {
      return dosingFrequency.getValue().intValue();
    }
    else if (dosingFrequency.getType() == DosingFrequencyTypeEnum.BETWEEN_DOSES)
    {
      return (int)(24 / dosingFrequency.getValue());
    }
    else
    {
      return 1;
    }
  }

  private Double getContinuousInfusionRate(final ComplexTherapyDto complexTherapyDto)
  {
    if (complexTherapyDto instanceof VariableComplexTherapyDto)
    {
      return getInfusionRateForVariableComplexTherapy((VariableComplexTherapyDto)complexTherapyDto);
    }
    else
    {
      final ComplexDoseElementDto doseElement = ((ConstantComplexTherapyDto)complexTherapyDto).getDoseElement();
      return doseElement != null ? doseElement.getRate() : null;
    }
  }
}

