package com.marand.thinkmed.medications.therapy.converter.fromehr;

import java.util.Objects;

import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.api.internal.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.ehr.model.AdditionalDetails;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public abstract class SimpleTherapyFromEhrConverter<T extends SimpleTherapyDto> extends TherapyFromEhrConverter<T>
{
  @Override
  public T mapTherapyFromEhr(
      final MedicationOrder medicationOrder,
      final String compositionUid,
      final DateTime createdTimestamp)
  {
    final T therapy = super.mapTherapyFromEhr(medicationOrder, compositionUid, createdTimestamp);
    therapy.setMedication(getTherapyFromEhrUtils().buildMedication(medicationOrder.getMedicationItem()));
    therapy.setDoseForm(extractDoseForm(medicationOrder));
    therapy.setQuantityUnit(extractQuantityUnit(medicationOrder));
    therapy.setQuantityDenominatorUnit(extractQuantityDenominatorUnit(medicationOrder));
    therapy.setTargetInr(extractTargetInr(medicationOrder));
    return therapy;
  }

  @Override
  protected TherapyDoseTypeEnum extractDoseType(final MedicationOrder medicationOrder)
  {
    return TherapyDoseTypeEnum.QUANTITY;
  }

  private DoseFormDto extractDoseForm(final MedicationOrder medicationOrder)
  {
    return getTherapyFromEhrUtils().buildDoseForm(medicationOrder.getPreparationDetails().getForm());
  }

  private String extractQuantityUnit(final MedicationOrder medicationOrder)
  {
    return medicationOrder.getStructuredDoseAndTimingDirections().stream()
        .flatMap(s -> s.getDosage().stream())
        .map(Dosage::getDoseUnit)
        .filter(Objects::nonNull)
        .map(DvText::getValue)
        .findFirst()
        .orElse(null);
  }

  private String extractQuantityDenominatorUnit(final MedicationOrder medicationOrder)
  {
    return medicationOrder.getStructuredDoseAndTimingDirections().stream()
        .flatMap(s -> s.getDosage().stream())
        .map(Dosage::getAlternateDoseUnit)
        .filter(Objects::nonNull)
        .map(DvText::getValue)
        .findFirst()
        .orElse(null);
  }

  private Double extractTargetInr(final MedicationOrder medicationOrder)
  {
    final AdditionalDetails additionalDetails = medicationOrder.getAdditionalDetails();
    if (additionalDetails != null && additionalDetails.getTargetInr() != null && additionalDetails.getTargetInr() instanceof DvQuantity)
    {
      return ((DvQuantity)additionalDetails.getTargetInr()).getMagnitude();
    }
    return null;
  }
}
