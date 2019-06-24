package com.marand.thinkmed.medications.validator;

import com.marand.thinkmed.medications.api.internal.dto.*;
import com.marand.thinkmed.medications.api.internal.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.DoseRangeDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
@Component
public class TherapyDtoValidator
{
  private static final Logger LOG = LoggerFactory.getLogger(TherapyDtoValidator.class);

  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  public boolean isValid(final TherapyDto therapyDto)
  {
    return getErrorsDescription(therapyDto).isEmpty();
  }

  public void print(final TherapyDto therapyDto, final List<String> errorsDescription)
  {
    final StringBuilder sb = new StringBuilder();
    sb.append("\n");
    sb.append("TherapyDto medication/s: ");
    final List<MedicationDto> medications = therapyDto.getMedications();
    if (medications.isEmpty())
    {
      sb.append("Unknown");
    }
    else
    {
      for (final MedicationDto med : medications)
      {
        if (medications.indexOf(med) != 0)
        {
          sb.append(", ");
        }
        sb.append(med.getName());
      }
    }
    sb.append("\n");
    sb.append("Errors: ");
    if (errorsDescription.isEmpty())
    {
      sb.append("None");
      sb.append("\n");
    }
    else
    {
      for (final String str : errorsDescription)
      {
        sb.append("\n");
        sb.append("\t- ");
        sb.append(str);
      }
      LOG.debug(sb.toString());
    }
  }

  public List<String> getErrorsDescription(final TherapyDto therapyDto)
  {
    final List<String> validationErrors = new ArrayList<>();

    final boolean whenNeeded = therapyDto.getWhenNeeded() != null ? therapyDto.getWhenNeeded() : false;

    final boolean containsAntibiotic = therapyDto.getMedicationIds().stream()
        .map(id -> medicationsValueHolderProvider.getMedicationData(id))
        .filter(Objects::nonNull)
        .anyMatch(MedicationDataDto::isAntibiotic);

    final boolean isFormulary = therapyDto.getMedicationIds().stream()
        .map(id -> medicationsValueHolderProvider.getMedicationData(id))
        .filter(Objects::nonNull)
        .allMatch(MedicationDataDto::isFormulary);

    final boolean containsHighAlertMedication = therapyDto.getMedicationIds().stream()
        .map(id -> medicationsValueHolderProvider.getMedicationData(id))
        .filter(Objects::nonNull)
        .anyMatch(MedicationDataDto::isHighAlertMedication);

    final boolean medicationExists = medicationExists(therapyDto);
    if (!medicationExists)
    {
      validationErrors.add(TherapyDtoValidatorEnum.MEDICATION_IS_MISSING.getText());
    }
    final boolean isDoseElementDtoValid = isDoseElementDtoValid(therapyDto);
    final boolean hasTitration = hasTitration(therapyDto);
    if (!isDoseElementDtoValid && !hasTitration)
    {
      validationErrors.add(TherapyDtoValidatorEnum.DOSE_ELEMENT_IS_MISSING.getText());
    }
    if (isDoseElementDtoValid && hasTitration)
    {
      //there may be a scenario that allows this
      validationErrors.add(TherapyDtoValidatorEnum.DOSE_ELEMENT_AND_TITRATION_EXISTS.getText());
    }
    final boolean routeExists = routeExists(therapyDto);
    if (!routeExists)
    {
      validationErrors.add(TherapyDtoValidatorEnum.ROUTE_IS_MISSING.getText());
    }
    final boolean isDosingIntervalValid = isDosingIntervalValid(therapyDto, whenNeeded);
    if (!isDosingIntervalValid)
    {
      validationErrors.add(TherapyDtoValidatorEnum.DOSING_INTERVAL_IS_MISSING.getText());
    }
    final boolean isIndicationValid = isIndicationValid(
        therapyDto,
        whenNeeded,
        containsAntibiotic,
        containsHighAlertMedication);
    if (!isIndicationValid)
    {
      validationErrors.add(TherapyDtoValidatorEnum.INDICATION_IS_MISSING.getText());
    }
    final boolean isCommentValid = isCommentValid(therapyDto, isFormulary);
    if (!isCommentValid)
    {
      validationErrors.add(TherapyDtoValidatorEnum.COMMENT_IS_MISSING.getText());
    }
    final boolean isReviewReminderValid = isReviewReminderValid(therapyDto);
    if (!isReviewReminderValid)
    {
      validationErrors.add(TherapyDtoValidatorEnum.REVIEW_REMINDER_DAYS_IS_MISSING.getText());
    }
    final boolean isTargetSaturationValid = isTargetSaturationValid(therapyDto);
    if (!isTargetSaturationValid)
    {
      validationErrors.add(TherapyDtoValidatorEnum.TARGET_SATURATION_IS_MISSING.getText());
    }

    return validationErrors;
  }

  private boolean medicationExists(final TherapyDto therapyDto)
  {
    if (therapyDto instanceof SimpleTherapyDto)
    {
      final SimpleTherapyDto therapy = (SimpleTherapyDto)therapyDto;
      return therapy.getMedication() != null;
    }
    if (therapyDto instanceof ComplexTherapyDto)
    {
      final ComplexTherapyDto therapy = (ComplexTherapyDto)therapyDto;
      final List<InfusionIngredientDto> ingredientList = therapy.getIngredientsList();
      return !ingredientList.isEmpty() && ingredientList.stream().allMatch(x -> x.getMedication() != null);
    }
    return true;
  }

  private boolean isDoseElementDtoValid(final TherapyDto therapyDto)
  {
    if (therapyDto instanceof ConstantSimpleTherapyDto)
    {
      final ConstantSimpleTherapyDto therapy = (ConstantSimpleTherapyDto)therapyDto;
      final SimpleDoseElementDto doseElement = therapy.getDoseElement();
      return doseElementHasValues(doseElement);
    }
    if (therapyDto instanceof VariableSimpleTherapyDto)
    {
      final VariableSimpleTherapyDto therapy = (VariableSimpleTherapyDto)therapyDto;
      final List<TimedSimpleDoseElementDto> doseElements = therapy.getTimedDoseElements();
      return !doseElements.isEmpty() && doseElements.stream().allMatch(this::doseElementHasValues);
    }
    if (therapyDto instanceof ConstantComplexTherapyDto)
    {
      final ConstantComplexTherapyDto therapy = (ConstantComplexTherapyDto)therapyDto;
      final ComplexDoseElementDto doseElement = therapy.getDoseElement();
      final List<InfusionIngredientDto> ingredientsList = therapy.getIngredientsList();

      if (therapy.isContinuousInfusion() && !therapy.isAdjustToFluidBalance())
      {
        return doseElementHasValues(doseElement, false);
      }
      if (therapy.getDoseType() == TherapyDoseTypeEnum.RATE_QUANTITY || therapy.getDoseType() == TherapyDoseTypeEnum.RATE_VOLUME_SUM)
      {
        return doseElementHasValues(doseElement, true) && infusionIngredientHasValues(ingredientsList);
      }
      if (therapy.getDoseType() == TherapyDoseTypeEnum.QUANTITY || therapy.getDoseType() == TherapyDoseTypeEnum.VOLUME_SUM)
      {
        return infusionIngredientHasValues(ingredientsList);
      }
    }
    return true;
  }

  private boolean doseElementHasValues(final SimpleDoseElementDto doseElement)
  {
    if (doseElement == null)
    {
      return false;
    }
    final DoseRangeDto doseRange = doseElement.getDoseRange();
    if (doseRange != null)
    {
      return doseRange.getMaxNumerator() != null && doseRange.getMinNumerator() != null;
    }
    return doseElement.getQuantity() != null || !StringUtils.isEmpty(doseElement.getDoseDescription());
  }

  private boolean doseElementHasValues(final TimedSimpleDoseElementDto timedSimpleDoseElement)
  {
    final SimpleDoseElementDto doseElement = timedSimpleDoseElement.getDoseElement();
    return doseElement != null && doseElement.getQuantity() != null;
  }

  private boolean doseElementHasValues(final ComplexDoseElementDto doseElement, final boolean withDuration)
  {
    boolean result = doseElement != null && doseElement.getRate() != null;
    if (result && withDuration)
    {
      result = doseElement.getDuration() != null;
    }
    return result;
  }

  private boolean infusionIngredientHasValues(final List<InfusionIngredientDto> ingredientsList)
  {
    return !ingredientsList.isEmpty() && ingredientsList.stream().allMatch(x -> x.getQuantity() != null);
  }

  private boolean hasTitration(final TherapyDto therapyDto)
  {
    if (therapyDto instanceof ConstantSimpleTherapyDto)
    {
      final ConstantSimpleTherapyDto therapy = (ConstantSimpleTherapyDto)therapyDto;
      return therapy.getTitration() != null;
    }
    if (therapyDto instanceof ConstantComplexTherapyDto)
    {
      final ConstantComplexTherapyDto therapy = (ConstantComplexTherapyDto)therapyDto;
      return therapy.getTitration() != null;
    }
    return false;
  }

  private boolean routeExists(final TherapyDto therapyDto)
  {
    return !therapyDto.getRoutes().isEmpty();
  }

  private boolean isDosingIntervalValid(final TherapyDto therapyDto, final boolean whenNeeded)
  {
    if (!whenNeeded)
    {
      final DosingFrequencyDto dosingFrequencyDto = therapyDto.getDosingFrequency();

      if (therapyDto instanceof ConstantSimpleTherapyDto)
      {
        return dosingFrequencyHasValue(dosingFrequencyDto);
      }
      if (therapyDto instanceof ConstantComplexTherapyDto)
      {
        final ConstantComplexTherapyDto therapy = (ConstantComplexTherapyDto)therapyDto;
        List<TherapyDoseTypeEnum> enumList = Arrays.asList(
            TherapyDoseTypeEnum.QUANTITY,
            TherapyDoseTypeEnum.RATE_QUANTITY,
            TherapyDoseTypeEnum.VOLUME_SUM,
            TherapyDoseTypeEnum.RATE_VOLUME_SUM);

        if (enumList.contains(therapy.getDoseType()))
        {
          return dosingFrequencyHasValue(dosingFrequencyDto);
        }
      }
    }
    return true;
  }

  private boolean dosingFrequencyHasValue(final DosingFrequencyDto dosingFrequencyDto)
  {
    return dosingFrequencyDto != null && dosingFrequencyDto.getType() != null;
  }

  private boolean isIndicationValid(
      final TherapyDto therapyDto,
      final boolean whenNeeded,
      final boolean isAntibiotic,
      final boolean containsHighAlertMedication)
  {
    if (whenNeeded || isAntibiotic || containsHighAlertMedication)
    {
      return therapyDto.getClinicalIndication() != null && !StringUtils.isEmpty(therapyDto.getClinicalIndication()
                                                                                    .getName());
    }
    return true;
  }

  private boolean isCommentValid(final TherapyDto therapyDto, final boolean isFormulary)
  {
    final List<MedicationDto> medications = therapyDto.getMedications();
    final boolean universalForm = medications.stream().anyMatch(x -> x.getId() == null);

    if (!isFormulary || universalForm)
    {
      return !StringUtils.isEmpty(therapyDto.getComment()) && therapyDto.getComment().trim().length() > 1;
    }
    return true;
  }

  private boolean isReviewReminderValid(final TherapyDto therapyDto)
  {
    if (!StringUtils.isEmpty(therapyDto.getReviewReminderComment()))
    {
      return therapyDto.getReviewReminderDays() != null;
    }
    return true;
  }

  private boolean isTargetSaturationValid(final TherapyDto therapyDto)
  {
    if (therapyDto instanceof OxygenTherapyDto)
    {
      final OxygenTherapyDto therapy = (OxygenTherapyDto)therapyDto;
      return therapy.getMinTargetSaturation() != null && therapy.getMaxTargetSaturation() != null;
    }
    return true;
  }
}
