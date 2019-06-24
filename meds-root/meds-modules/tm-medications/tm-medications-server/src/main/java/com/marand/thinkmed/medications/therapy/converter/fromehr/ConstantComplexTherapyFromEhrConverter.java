package com.marand.thinkmed.medications.therapy.converter.fromehr;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DataValue;
import org.openehr.jaxb.rm.DvText;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public class ConstantComplexTherapyFromEhrConverter extends ComplexTherapyFromEhrConverter<ConstantComplexTherapyDto>
{
  @Override
  public boolean isFor(final MedicationOrder medicationOrder)
  {
    return MedicationsEhrUtils.isComplexTherapy(medicationOrder) && !MedicationsEhrUtils.isVariableTherapy(medicationOrder);
  }

  @Override
  protected ConstantComplexTherapyDto createTherapyDto(final MedicationOrder medicationOrder)
  {
    return new ConstantComplexTherapyDto();
  }

  @Override
  public ConstantComplexTherapyDto mapTherapyFromEhr(
      final MedicationOrder medicationOrder,
      final String compositionUid,
      final DateTime createdTimestamp)
  {
    final ConstantComplexTherapyDto therapy = super.mapTherapyFromEhr(medicationOrder, compositionUid, createdTimestamp);
    therapy.setDoseElement(extractDoseElement(medicationOrder));
    therapy.setDoseTimes(getTherapyFromEhrUtils().extractDoseTimes(medicationOrder));
    therapy.setTitration(getTherapyFromEhrUtils().extractTitration(medicationOrder));
    therapy.setRateString(extractRateString(medicationOrder));
    return therapy;
  }

  private ComplexDoseElementDto extractDoseElement(final MedicationOrder medicationOrder)
  {
    final Opt<Dosage> dosageOpt = Opt.resolve(
        () -> medicationOrder.getStructuredDoseAndTimingDirections().get(0).getDosage().get(0));
    if (dosageOpt.isPresent())
    {
      return getTherapyFromEhrUtils().extractComplexDoseElement(dosageOpt.get());
    }
    return new ComplexDoseElementDto();
  }

  private String extractRateString(final MedicationOrder medicationOrder)
  {
    final Opt<DataValue> rate = Opt.resolve(
        () -> medicationOrder.getStructuredDoseAndTimingDirections().get(0).getDosage().get(0).getAdministrationRate());
    if (rate.isPresent() && rate.get() instanceof DvText)
    {
      return ((DvText)rate.get()).getValue();
    }
    return null;
  }
}
