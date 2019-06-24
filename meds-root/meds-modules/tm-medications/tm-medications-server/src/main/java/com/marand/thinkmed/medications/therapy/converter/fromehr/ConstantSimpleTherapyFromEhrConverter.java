package com.marand.thinkmed.medications.therapy.converter.fromehr;

import java.util.List;

import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.TherapeuticDirection;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public class ConstantSimpleTherapyFromEhrConverter extends SimpleTherapyFromEhrConverter<ConstantSimpleTherapyDto>
{
  @Override
  public boolean isFor(final MedicationOrder medicationOrder)
  {
    return MedicationsEhrUtils.isSimpleTherapy(medicationOrder) && !MedicationsEhrUtils.isVariableTherapy(medicationOrder);
  }

  @Override
  protected ConstantSimpleTherapyDto createTherapyDto(final MedicationOrder medicationOrder)
  {
    return new ConstantSimpleTherapyDto();
  }

  @Override
  public ConstantSimpleTherapyDto mapTherapyFromEhr(
      final MedicationOrder medicationOrder,
      final String compositionUid,
      final DateTime createdTimestamp)
  {
    final ConstantSimpleTherapyDto therapy = super.mapTherapyFromEhr(medicationOrder, compositionUid, createdTimestamp);
    therapy.setDoseElement(extractDoseElement(medicationOrder));
    therapy.setDoseTimes(getTherapyFromEhrUtils().extractDoseTimes(medicationOrder));
    therapy.setTitration(getTherapyFromEhrUtils().extractTitration(medicationOrder));
    return therapy;
  }

  private SimpleDoseElementDto extractDoseElement(final MedicationOrder medicationOrder)
  {
    final List<TherapeuticDirection> structuredDoseAndTimingDirections = medicationOrder.getStructuredDoseAndTimingDirections();

    final Dosage dosage =
        !structuredDoseAndTimingDirections.isEmpty() &&
            !structuredDoseAndTimingDirections.get(0).getDosage().isEmpty() ?
        structuredDoseAndTimingDirections.get(0).getDosage().get(0) : null;

    if (dosage != null)
    {
      return getTherapyFromEhrUtils().extractSimpleDoseElement(dosage);
    }
    return null;
  }
}
