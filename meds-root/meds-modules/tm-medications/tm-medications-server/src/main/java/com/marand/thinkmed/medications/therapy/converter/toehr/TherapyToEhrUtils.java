package com.marand.thinkmed.medications.therapy.converter.toehr;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.api.internal.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.DoseRangeDto;
import com.marand.thinkmed.medications.ehr.model.DayOfWeek;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.MedicalDevice;
import com.marand.thinkmed.medications.ehr.model.TherapeuticDirection;
import com.marand.thinkmed.medications.ehr.model.TimingDaily;
import com.marand.thinkmed.medications.ehr.model.TimingNonDaily;
import com.marand.thinkmed.medications.ehr.utils.EhrValueUtils;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvText;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Mitja Lapajne
 */

@Component
public class TherapyToEhrUtils
{

  public DvText extractMedication(final MedicationDto medication)
  {
    if (medication.getId() != null)
    {
      return DataValueUtils.getLocalCodedText(String.valueOf(medication.getId()), medication.getName());
    }
    return DataValueUtils.getText(medication.getName());
  }

  public TimingNonDaily buildTimingNonDaily(
      final Integer repetitionInterval,
      final List<String> daysOfWeek,
      final DateTime specificDate,
      final String timingDescription)
  {
    final TimingNonDaily timingNonDaily = new TimingNonDaily();

    if (repetitionInterval != null)
    {
      timingNonDaily.setRepetitionInterval(DataValueUtils.getDuration(0, 0, repetitionInterval, 0, 0, 0));
    }

    if (daysOfWeek != null)
    {
      timingNonDaily.setSpecificDayOfWeek(
          daysOfWeek.stream()
              .map(DayOfWeek::valueOf)
              .map(DayOfWeek::getDvCodedText)
              .collect(Collectors.toList()));
    }

    if (specificDate != null)
    {
      timingNonDaily.getSpecificDate().add(EhrValueUtils.getDate(specificDate));
    }

    if (timingDescription != null)
    {
      timingNonDaily.setTimingDescription(DataValueUtils.getText(timingDescription));
    }

    return timingNonDaily;
  }

  public MedicalDevice buildMedicalDeviceForOxygen(final OxygenStartingDevice startingDevice)
  {
    if (startingDevice != null && startingDevice.getRoute() != null)
    {
      final MedicalDevice medicalDevice = new MedicalDevice();
      medicalDevice.setName(startingDevice.getRoute().getDvCodedText());

      if (startingDevice.getRouteType() != null)
      {
        medicalDevice.setType(DataValueUtils.getText(startingDevice.getRouteType()));
      }

      return medicalDevice;
    }

    return null;
  }

  public Dosage buildDosage(
      final Double doseAmount,
      final String doseUnit,
      final Double alternateDoseAmount,
      final String alternateDoseUnit)
  {
    return buildDosage(
        doseAmount,
        doseUnit,
        alternateDoseAmount,
        alternateDoseUnit,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }

  public Dosage buildDosage(
      final Double doseAmount,
      final String doseUnit,
      final Double alternateDoseAmount,
      final String alternateDoseUnit,
      final Double doseFormula,
      final String doseFormulaUnit,
      final Double administrationRate,
      final String administrationRateUnit)
  {
    return buildDosage(
        doseAmount,
        doseUnit,
        alternateDoseAmount,
        alternateDoseUnit,
        doseFormula,
        doseFormulaUnit,
        null,
        administrationRate,
        administrationRateUnit,
        null,
        null,
        null,
        null);
  }

  public Dosage buildDosage(
      final Double doseAmount,
      final String doseUnit,
      final Double alternateDoseAmount,
      final String alternateDoseUnit,
      final Double doseFormula,
      final String doseFormulaUnit,
      final DoseRangeDto doseRange,
      final Double administrationRate,
      final String administrationRateUnit,
      final String administrationRateString,
      final Integer administrationDuration,
      final String doseDescription,
      final TimingDaily timingDaily)
  {
    final Dosage dosage = new Dosage();

    if (doseAmount != null)
    {
      Preconditions.checkNotNull(doseUnit);
      dosage.setDoseAmount(DataValueUtils.getQuantity(doseAmount, "1"));
      dosage.setDoseUnit(DataValueUtils.getText(doseUnit));
    }

    if (alternateDoseAmount != null)
    {
      Preconditions.checkNotNull(doseAmount);
      Preconditions.checkNotNull(alternateDoseUnit);
      dosage.setAlternateDoseAmount(DataValueUtils.getQuantity(alternateDoseAmount, "1"));
      dosage.setAlternateDoseUnit(DataValueUtils.getText(alternateDoseUnit));
    }

    if (doseRange != null)
    {
      Preconditions.checkArgument(doseAmount == null);
      Preconditions.checkNotNull(doseRange.getMinNumerator());
      Preconditions.checkNotNull(doseRange.getMaxNumerator());
      Preconditions.checkNotNull(doseUnit);

      dosage.setDoseAmount(DataValueUtils.getInterval("1", doseRange.getMinNumerator(), doseRange.getMaxNumerator()));
      dosage.setDoseUnit(DataValueUtils.getLocalCodedText(doseUnit, doseUnit));

      if (doseRange.getMinDenominator() != null)
      {
        Preconditions.checkNotNull(alternateDoseUnit);

        dosage.setAlternateDoseAmount(DataValueUtils.getInterval("1", doseRange.getMinDenominator(), doseRange.getMaxDenominator()));
        dosage.setAlternateDoseUnit(DataValueUtils.getText(alternateDoseUnit));
      }
    }

    if (!StringUtils.isEmpty(doseDescription))
    {
      dosage.setDoseDescription(DataValueUtils.getText(doseDescription));
    }

    if (doseFormula != null)
    {
      Preconditions.checkNotNull(doseFormulaUnit);
      dosage.setDoseFormula(DataValueUtils.getText(doseFormula + " " + doseFormulaUnit));
    }

    if (administrationRate != null)
    {
      dosage.setAdministrationRate(DataValueUtils.getQuantity(administrationRate, administrationRateUnit));
    }

    if (administrationRateString != null)
    {
      dosage.setAdministrationRate(DataValueUtils.getText(administrationRateString));
    }

    if (administrationDuration != null)
    {
      dosage.setAdministrationDuration(DataValueUtils.getDuration(0, 0, 0, 0, administrationDuration, 0));
    }

    dosage.setTiming(timingDaily);
    return dosage;
  }

  public Dosage buildDosage(
      final ComplexTherapyDto therapy,
      final ComplexDoseElementDto doseElement,
      final String rateString,
      final TimingDaily timingDaily,
      final boolean setDoseAmount)
  {
    final boolean adHocMixture = therapy.getIngredientsList().size() > 1;

    if (adHocMixture)
    {
      return buildDosage(
          setDoseAmount ? therapy.getVolumeSum() : null,
          setDoseAmount ? therapy.getVolumeSumUnit() : null,
          null,
          null,
          doseElement != null ? doseElement.getRateFormula() : null,
          doseElement != null ? doseElement.getRateFormulaUnit() : null,
          null,
          doseElement != null ? doseElement.getRate() : null,
          doseElement != null ? doseElement.getRateUnit() : null,
          rateString,
          doseElement != null ? doseElement.getDuration() : null,
          null,
          timingDaily);
    }
    else
    {
      final InfusionIngredientDto infusionIngredient = therapy.getIngredientsList().get(0);
      return buildDosage(
          setDoseAmount ? infusionIngredient.getQuantity() : null,
          setDoseAmount ? infusionIngredient.getQuantityUnit() : null,
          setDoseAmount ? infusionIngredient.getQuantityDenominator() : null,
          setDoseAmount ? infusionIngredient.getQuantityDenominatorUnit() : null,
          doseElement != null ? doseElement.getRateFormula() : null,
          doseElement != null ? doseElement.getRateFormulaUnit() : null,
          null,
          doseElement != null ? doseElement.getRate() : null,
          doseElement != null ? doseElement.getRateUnit() : null,
          rateString,
          doseElement != null ? doseElement.getDuration() : null,
          null,
          timingDaily);
    }
  }

  public TimingDaily buildTimingDaily(
      final DosingFrequencyDto dosingFrequency,
      final Collection<HourMinuteDto> administrationTimes,
      final Boolean whenNeeded)
  {
    final TimingDaily timingDaily = new TimingDaily();

    if (dosingFrequency != null)
    {
      if (dosingFrequency.getType() == DosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        if (dosingFrequency.getValue() % 1 == 0)
        {
          timingDaily.setInterval(DataValueUtils.getDuration(0, 0, 0, dosingFrequency.getValue().intValue(), 0, 0));
        }
        else
        {
          final int hours = dosingFrequency.getValue().intValue();
          final int minutes = (int)(dosingFrequency.getValue() % 1 * 60);
          timingDaily.setInterval(DataValueUtils.getDuration(0, 0, 0, hours, minutes, 0));
        }
      }
      else if (dosingFrequency.getType() == DosingFrequencyTypeEnum.DAILY_COUNT)
      {
        timingDaily.setFrequency(DataValueUtils.getQuantity(dosingFrequency.getValue(), "1/d"));
      }
      else if (dosingFrequency.getType() == DosingFrequencyTypeEnum.MORNING)
      {
        timingDaily.setSpecificEvent(DosingFrequencyTypeEnum.MORNING.getDvCodedText());
      }
      else if (dosingFrequency.getType() == DosingFrequencyTypeEnum.EVENING)
      {
        timingDaily.setSpecificEvent(DosingFrequencyTypeEnum.EVENING.getDvCodedText());
      }
      else if (dosingFrequency.getType() == DosingFrequencyTypeEnum.NOON)
      {
        timingDaily.setSpecificEvent(DosingFrequencyTypeEnum.NOON.getDvCodedText());
      }
    }

    if (administrationTimes != null)
    {
      timingDaily.setSpecificTime(
          administrationTimes.stream()
              .map(t -> EhrValueUtils.getTime(t.toLocalTime()))
              .collect(Collectors.toList()));
    }

    if (whenNeeded != null && whenNeeded)
    {
      timingDaily.setAsRequired(DataValueUtils.getBoolean(true));
    }

    return timingDaily;
  }

  public TherapeuticDirection buildTherapeuticDirection(
      final List<Dosage> dosages,
      final TimingNonDaily timingNonDaily,
      final DosingFrequencyDto dosingFrequency)
  {
    final TherapeuticDirection therapeuticDirection = new TherapeuticDirection();
    therapeuticDirection.setDosage(dosages);
    therapeuticDirection.setDirectionRepetition(timingNonDaily);

    if (dosingFrequency != null && dosingFrequency.getType() == DosingFrequencyTypeEnum.ONCE_THEN_EX)
    {
      therapeuticDirection.setMaximumNumberOfAdministration(EhrValueUtils.getCount(1L));
    }

    return therapeuticDirection;
  }
}