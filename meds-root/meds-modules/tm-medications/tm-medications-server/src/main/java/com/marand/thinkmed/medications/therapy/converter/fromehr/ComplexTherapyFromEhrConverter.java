package com.marand.thinkmed.medications.therapy.converter.fromehr;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.api.internal.dto.DosageJustificationEnum;
import com.marand.thinkmed.medications.HeparinEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.Medication;
import com.marand.thinkmed.medications.ehr.model.MedicationCategory;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvQuantity;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
@Component
public abstract class ComplexTherapyFromEhrConverter<T extends ComplexTherapyDto> extends TherapyFromEhrConverter<T>
{
  @Override
  public T mapTherapyFromEhr(
      final MedicationOrder medicationOrder,
      final String compositionUid,
      final DateTime createdTimestamp)
  {
    final T therapy = super.mapTherapyFromEhr(medicationOrder, compositionUid, createdTimestamp);
    therapy.setIngredientsList(extractIngredients(medicationOrder));
    therapy.setContinuousInfusion(MedicationsEhrUtils.isContinuousInfusion(medicationOrder));
    therapy.setVolumeSum(extractVolumeSum(medicationOrder));
    therapy.setVolumeSumUnit(extractVolumeSumUnit(medicationOrder));
    therapy.setAdditionalInstruction(extractAdditionalInstruction(medicationOrder));
    therapy.setBaselineInfusion(getTherapyFromEhrUtils().isBaselineInfusion(medicationOrder));
    therapy.setAdjustToFluidBalance(extractAdjustToFluidBalance(medicationOrder));
    return therapy;
  }

  @Override
  protected TherapyDoseTypeEnum extractDoseType(final MedicationOrder medicationOrder)
  {
    if (MedicationsEhrUtils.isContinuousInfusion(medicationOrder))
    {
      return TherapyDoseTypeEnum.RATE;
    }

    final boolean adHocMixture = isAdHocMixture(medicationOrder.getPreparationDetails());
    final Dosage dosage = Opt.resolve(
        () -> medicationOrder.getStructuredDoseAndTimingDirections().get(0).getDosage().get(0)).orElse(null);
    final boolean hasRate = getTherapyFromEhrUtils().extractRate(dosage) != null;

    if (adHocMixture)
    {
      return hasRate ? TherapyDoseTypeEnum.RATE_VOLUME_SUM : TherapyDoseTypeEnum.VOLUME_SUM;
    }
    else
    {
      return hasRate ? TherapyDoseTypeEnum.RATE_QUANTITY : TherapyDoseTypeEnum.QUANTITY;
    }
  }

  private List<InfusionIngredientDto> extractIngredients(final MedicationOrder medicationOrder)
  {
    final Medication preparationDetails = medicationOrder.getPreparationDetails();
    if (isAdHocMixture(preparationDetails))
    {
      return extractAdHocMixtureIngredients(medicationOrder);
    }
    return Collections.singletonList(extractSingleIngredient(medicationOrder));
  }

  private boolean isAdHocMixture(final Medication preparationDetails)
  {
    return MedicationCategory.AD_HOC_MIXTURE.matches(preparationDetails.getCategory());
  }

  private List<InfusionIngredientDto> extractAdHocMixtureIngredients(final MedicationOrder medicationOrder)
  {
    final Medication preparationDetails = medicationOrder.getPreparationDetails();
    return preparationDetails.getConstituent()
        .stream()
        .map(this::extractIngredientFromConstituent)
        .collect(Collectors.toList());
  }

  private InfusionIngredientDto extractIngredientFromConstituent(final Medication constituent)
  {
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();

    ingredient.setMedication(getTherapyFromEhrUtils().buildMedication(constituent));

    if (constituent.getAmountValue() != null)
    {
      ingredient.setQuantity(constituent.getAmountValue().getMagnitude());
    }
    if (constituent.getAmountUnit() != null)
    {
      ingredient.setQuantityUnit(constituent.getAmountUnit().getValue());
    }
    if (constituent.getAlternateAmountValue() != null)
    {
      ingredient.setQuantityDenominator(constituent.getAlternateAmountValue().getMagnitude());
    }
    if (constituent.getAlternateAmountUnit() != null)
    {
      ingredient.setQuantityDenominatorUnit(constituent.getAlternateAmountUnit().getValue());
    }

    ingredient.setDoseForm(getTherapyFromEhrUtils().buildDoseForm(constituent.getForm()));
    return ingredient;
  }

  private InfusionIngredientDto extractSingleIngredient(final MedicationOrder medicationOrder)
  {
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    ingredient.setMedication(getTherapyFromEhrUtils().buildMedication(medicationOrder.getMedicationItem()));
    ingredient.setDoseForm(getTherapyFromEhrUtils().buildDoseForm(medicationOrder.getPreparationDetails().getForm()));

    final Opt<Dosage> dosageOpt =
        Opt.resolve(() -> medicationOrder.getStructuredDoseAndTimingDirections().get(0).getDosage().get(0));
    if (dosageOpt.isPresent())
    {
      final Dosage dosage = dosageOpt.get();
      if (dosage.getDoseAmount() instanceof DvQuantity)
      {
        ingredient.setQuantity(((DvQuantity)dosage.getDoseAmount()).getMagnitude());
      }
      if (dosage.getDoseUnit() != null)
      {
        ingredient.setQuantityUnit(dosage.getDoseUnit().getValue());
      }
      if (dosage.getAlternateDoseAmount() instanceof DvQuantity)
      {
        ingredient.setQuantityDenominator(((DvQuantity)dosage.getAlternateDoseAmount()).getMagnitude());
      }
      if (dosage.getAlternateDoseUnit() != null)
      {
        ingredient.setQuantityDenominatorUnit(dosage.getAlternateDoseUnit().getValue());
      }
    }
    return ingredient;
  }

  private Double extractVolumeSum(final MedicationOrder medicationOrder)
  {
    if (isAdHocMixture(medicationOrder.getPreparationDetails()))
    {
      return Opt.resolve(() -> medicationOrder.getPreparationDetails().getAmountValue().getMagnitude()).orElse(null);
    }
    return null;
  }

  private String extractVolumeSumUnit(final MedicationOrder medicationOrder)
  {
    if (isAdHocMixture(medicationOrder.getPreparationDetails()))
    {
      return Opt.resolve(() -> medicationOrder.getPreparationDetails().getAmountUnit().getValue()).orElse(null);
    }
    return null;
  }

  private String extractAdditionalInstruction(final MedicationOrder medicationOrder)
  {
    return Opt.resolve(() -> HeparinEnum.valueOf(medicationOrder.getAdditionalDetails().getHeparin()).name()).orElse(null);
  }


  private boolean extractAdjustToFluidBalance(final MedicationOrder medicationOrder)
  {
    return medicationOrder.getDosageJustification().stream()
        .anyMatch(DosageJustificationEnum.ADJUST_TO_FLUID_BALANCE::matches);
  }
}
