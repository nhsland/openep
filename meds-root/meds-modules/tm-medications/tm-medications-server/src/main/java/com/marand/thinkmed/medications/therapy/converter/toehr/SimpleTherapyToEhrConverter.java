package com.marand.thinkmed.medications.therapy.converter.toehr;

import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType;
import com.marand.thinkmed.medications.api.internal.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.ehr.model.AdditionalDetails;
import com.marand.thinkmed.medications.ehr.model.Medication;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public abstract class SimpleTherapyToEhrConverter<T extends SimpleTherapyDto> extends TherapyToEhrConverter<T>
{

  @Override
  protected DvText extractMedicationItem(final T therapy)
  {
    return getTherapyToEhrUtils().extractMedication(therapy.getMedication());
  }

  @Override
  protected Medication extractPreparationDetails(final T therapy)
  {
    final Medication medication = new Medication();
    if (therapy.getDoseForm() != null && therapy.getDoseForm().getName() != null)
    {
      medication.setForm(DataValueUtils.getLocalCodedText(
          String.valueOf(therapy.getDoseForm().getId()),
          therapy.getDoseForm().getName()));
    }
    medication.setComponentName(extractMedicationItem(therapy));
    return medication;
  }

  @Override
  protected DvCodedText extractPrescriptionType(final T therapy)
  {
    return MedicationOrderFormType.SIMPLE.getDvCodedText();
  }

  @Override
  protected AdditionalDetails extractAdditionalDetails(final T therapy)
  {
    final AdditionalDetails additionalDetails = super.extractAdditionalDetails(therapy);
    if (therapy.getTargetInr() != null)
    {
      additionalDetails.setTargetInr(DataValueUtils.getQuantity(therapy.getTargetInr(), "1"));
    }
    return additionalDetails;
  }
}
