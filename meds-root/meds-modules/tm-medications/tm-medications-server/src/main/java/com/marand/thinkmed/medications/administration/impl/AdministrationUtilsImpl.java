package com.marand.thinkmed.medications.administration.impl;

import java.util.Collection;
import java.util.UUID;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.DoseAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionBagAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import com.marand.thinkmed.medications.dto.administration.PlannedDoseAdministration;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import com.marand.thinkmed.medications.units.converter.UnitsConverter;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.marand.thinkmed.medications.dto.unit.KnownUnitType.ML;
import static com.marand.thinkmed.medications.dto.unit.UnitGroupEnum.LIQUID_UNIT;

/**
 * @author Nejc Korasa
 */

@Component
public class AdministrationUtilsImpl implements AdministrationUtils
{
  private UnitsConverter unitsConverter;

@Autowired
  public void setUnitsConverter(final UnitsConverter unitsConverter)
  {
    this.unitsConverter = unitsConverter;
  }

  @Override
  public DateTime getAdministrationTime(final @NonNull AdministrationDto administrationDto)
  {
    return administrationDto.getAdministrationTime() != null
           ? administrationDto.getAdministrationTime()
           : administrationDto.getPlannedTime();
  }

  @Override
  public Double getInfusionRate(final @NonNull AdministrationDto administrationDto)
  {
    final TherapyDoseDto therapyDoseDto = getTherapyDose(administrationDto);
    return therapyDoseDto.getNumerator();
  }

  @Override
  public boolean isRateAdministration(final @NonNull AdministrationDto administrationDto)
  {
    return getTherapyDose(administrationDto).getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.RATE;
  }

  @Override
  public Double getVolumeForRateQuantityOrRateVolumeSum(
      final @NonNull AdministrationDto administrationDto,
      final @NonNull KnownUnitType knownUnit)
  {
    final TherapyDoseDto therapyDoseDto = getTherapyDose(administrationDto);

    if (therapyDoseDto.getSecondaryNumeratorUnit() != null
        && unitsConverter.isGroupType(therapyDoseDto.getSecondaryNumeratorUnit(), LIQUID_UNIT))
    {
      return unitsConverter.convert(therapyDoseDto.getSecondaryNumerator(), therapyDoseDto.getSecondaryNumeratorUnit(), knownUnit);
    }
    if (therapyDoseDto.getSecondaryDenominatorUnit() != null
        && unitsConverter.isGroupType(therapyDoseDto.getSecondaryDenominatorUnit(), LIQUID_UNIT))
    {
      return unitsConverter.convert(therapyDoseDto.getSecondaryDenominator(), therapyDoseDto.getSecondaryDenominatorUnit(), knownUnit);
    }

    return null;
  }

  @Override
  public InfusionBagDto getInfusionBagDto(final @NonNull AdministrationDto administrationDto)
  {
    return administrationDto instanceof InfusionBagAdministration
           ? ((InfusionBagAdministration)administrationDto).getInfusionBag()
           : null;
  }

  @Override
  public TherapyDoseDto getTherapyDose(final @NonNull AdministrationDto administration)
  {
    return getTherapyDose(administration, false);
  }

  @Override
  public TherapyDoseDto getPlannedTherapyDose(final @NonNull AdministrationDto administration)
  {
    return getTherapyDose(administration, true);
  }

  private TherapyDoseDto getTherapyDose(final @NonNull AdministrationDto administration, final boolean plannedDose)
  {
    if (administration instanceof DoseAdministration)
    {
      final TherapyDoseDto administered = ((DoseAdministration)administration).getAdministeredDose();

      if (administration instanceof PlannedDoseAdministration)
      {
        final TherapyDoseDto planned = ((PlannedDoseAdministration)administration).getPlannedDose();

        if (plannedDose)
        {
          return planned;
        }
        else
        {
          return administered == null ? planned : administered;

        }
      }
      return administered;
    }

    return null;
  }

  @Override
  public TherapyDoseTypeEnum getTherapyDoseType(final @NonNull AdministrationDto administrationDto)
  {
    return Opt.resolve(() -> getTherapyDose(administrationDto).getTherapyDoseTypeEnum()).orElse(null);
  }

  @Override
  public void fillDurationForInfusionWithRate(final @NonNull Collection<AdministrationDto> administrations)
  {
    StartAdministrationDto previousStartAdministration = null;
    DateTime previousAdministrationTime = null;
    Double duration = null;
    Double quantitySum = null;
    Double previousRate = null;

    for (final AdministrationDto administrationDto : administrations)
    {
      if (administrationDto.getAdministrationType() == AdministrationTypeEnum.START)
      {
        final StartAdministrationDto currentStartAdministration = (StartAdministrationDto)administrationDto;
        setStartAdministrationDuration(previousStartAdministration, duration, quantitySum, previousRate);

        if (currentStartAdministration.getAdministrationResult() == AdministrationResultEnum.NOT_GIVEN)
        {
          previousStartAdministration = null;
          previousAdministrationTime = null;
        }
        else
        {
          previousStartAdministration = currentStartAdministration;
          previousRate = getInfusionRate(currentStartAdministration);
          previousAdministrationTime = getAdministrationTime(currentStartAdministration);
        }
        duration = 0.0;
        quantitySum = 0.0;
      }
      else if (administrationDto.getAdministrationType() == AdministrationTypeEnum.ADJUST_INFUSION
          && administrationDto.getAdministrationResult() != AdministrationResultEnum.NOT_GIVEN)
      {
        if (previousAdministrationTime != null)
        {
          final DateTime currentAdministrationTime = getAdministrationTime(administrationDto);
          final double minutes = (double)Minutes
              .minutesBetween(previousAdministrationTime, currentAdministrationTime)
              .getMinutes();

          duration += minutes;
          quantitySum += minutes / 60 * previousRate;

          previousRate = getInfusionRate(administrationDto);
          previousAdministrationTime = currentAdministrationTime;
        }
      }
    }

    setStartAdministrationDuration(previousStartAdministration, duration, quantitySum, previousRate);
  }

  @Override
  public String generateGroupUUId(final @NonNull DateTime date)
  {
    final String uuid = UUID.randomUUID().toString();
    return new StringBuilder().append(date.getMillis()).append("_").append(uuid).toString();
  }

  @Override
  public int calculateDurationForRateQuantityDose(final @NonNull TherapyDoseDto dose)
  {
    final double rate = dose.getNumerator();
    final double quantity = Opt.resolve(dose::getSecondaryDenominator).orElse(dose.getSecondaryNumerator());
    final String unit = Opt.resolve(dose::getSecondaryDenominatorUnit).orElse(dose.getSecondaryNumeratorUnit());

    final double mlQuantity = unitsConverter.convert(quantity, unit, ML);

    //noinspection NumericCastThatLosesPrecision
    return (int)(mlQuantity / rate * 60);
  }

  private void setStartAdministrationDuration(
      final StartAdministrationDto administration,
      final Double duration,
      final Double quantitySum,
      final Double previousRate)
  {
    if (administration != null)
    {
      final Double doseQuantity = getVolumeForRateQuantityOrRateVolumeSum(administration, ML);

      if (doseQuantity != null && doseQuantity > quantitySum)
      {
        final double remainingQuantity = doseQuantity - quantitySum;
        final double calculatedDuration = duration + remainingQuantity / previousRate * 60;
        administration.setDuration(calculatedDuration);
      }
    }
  }
}
