package com.marand.thinkmed.medications.therapy.converter.toehr;

import java.util.List;
import java.util.stream.Collectors;

import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.api.internal.dto.DosageJustificationEnum;
import com.marand.thinkmed.medications.HeparinEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.ehr.model.AdditionalDetails;
import com.marand.thinkmed.medications.ehr.model.Medication;
import com.marand.thinkmed.medications.ehr.model.MedicationCategory;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public abstract class ComplexTherapyToEhrConverter<T extends ComplexTherapyDto> extends TherapyToEhrConverter<T>
{

  @Override
  protected DvText extractMedicationItem(final T therapy)
  {
    if (therapy.getIngredientsList().size() > 1)
    {
      return DataValueUtils.getText(
          therapy.getIngredientsList().stream()
              .map(i -> i.getMedication().getName())
              .collect(Collectors.joining(", ")));
    }
    if (therapy.getIngredientsList().size() == 1)
    {
      return getTherapyToEhrUtils().extractMedication(therapy.getIngredientsList().get(0).getMedication());
    }
    throw new IllegalArgumentException("Complex therapy without ingredients");
  }

  @Override
  protected Medication extractPreparationDetails(final T therapy)
  {
    final Medication medication = new Medication();

    medication.setComponentName(extractMedicationItem(therapy));
    if (therapy.getIngredientsList().size() == 1)
    {
      final InfusionIngredientDto infusionIngredient = therapy.getIngredientsList().get(0);
      if (infusionIngredient.getDoseForm() != null && infusionIngredient.getDoseForm().getName() != null)
      {
        medication.setForm(DataValueUtils.getLocalCodedText(
            String.valueOf(infusionIngredient.getDoseForm().getId()),
            infusionIngredient.getDoseForm().getName()));
      }
    }

    else if (therapy.getIngredientsList().size() > 1)
    {
      medication.setCategory(MedicationCategory.AD_HOC_MIXTURE.getDvCodedText());
      medication.setAmountValue(DataValueUtils.getQuantity(therapy.getVolumeSum(), "1"));
      medication.setAmountUnit(DataValueUtils.getText(therapy.getVolumeSumUnit()));
      medication.setConstituent(
          therapy.getIngredientsList().stream()
              .map(this::buildMedication)
              .collect(Collectors.toList()));
    }
    else
    {
      throw new IllegalArgumentException("Complex therapy without ingredients");
    }
    return medication;
  }

  private Medication buildMedication(final InfusionIngredientDto infusionIngredient)
  {
    final Medication medication = new Medication();
    medication.setComponentName(getTherapyToEhrUtils().extractMedication(infusionIngredient.getMedication()));

    if (infusionIngredient.getQuantity() != null)
    {
      medication.setAmountValue(DataValueUtils.getQuantity(infusionIngredient.getQuantity(), "1"));
      medication.setAmountUnit(DataValueUtils.getText(infusionIngredient.getQuantityUnit()));
    }

    if (infusionIngredient.getQuantityDenominator() != null)
    {
      medication.setAlternateAmountValue(DataValueUtils.getQuantity(infusionIngredient.getQuantityDenominator(), "1"));
      medication.setAlternateAmountUnit(DataValueUtils.getText(infusionIngredient.getQuantityDenominatorUnit()));
    }

    final MedicationTypeEnum medicationType = infusionIngredient.getMedication().getMedicationType();
    if (medicationType != null)
    {
      medication.setRole(MedicationTypeEnum.toMedicationRole(medicationType).getDvCodedText());
    }
    return medication;
  }

  @Override
  protected DvCodedText extractPrescriptionType(final T therapy)
  {
    return MedicationOrderFormType.COMPLEX.getDvCodedText();
  }

  @Override
  protected AdditionalDetails extractAdditionalDetails(final T therapy)
  {
    final AdditionalDetails additionalDetails = super.extractAdditionalDetails(therapy);
    if (therapy.getAdditionalInstruction() != null)
    {
      additionalDetails.setHeparin(HeparinEnum.valueOf(therapy.getAdditionalInstruction()).getDvCodedText());
    }
    if (therapy.isBaselineInfusion())
    {
      additionalDetails.setBaselineInfusion(DataValueUtils.getBoolean(true));
    }

    return additionalDetails;
  }

  @Override
  protected List<DvText> extractDosageJustification(final T therapy)
  {
    final List<DvText> dosageJustifications = super.extractDosageJustification(therapy);
    if (therapy.isAdjustToFluidBalance())
    {
      dosageJustifications.add(DosageJustificationEnum.ADJUST_TO_FLUID_BALANCE.getDvCodedText());
    }
    return dosageJustifications;
  }
}
