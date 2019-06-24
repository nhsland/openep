package com.marand.thinkmed.medications.therapy.converter;

import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public class TherapyConverter
{
  private TherapyConverterSelector therapyConverterSelector;

  @Autowired
  public void setTherapyConverterSelector(final TherapyConverterSelector therapyConverterSelector)
  {
    this.therapyConverterSelector = therapyConverterSelector;
  }

  public TherapyDto convertToTherapyDto(
      final MedicationOrder medicationOrder,
      final String compositionUid,
      final DateTime createdTimestamp)
  {
    return therapyConverterSelector.getConverter(medicationOrder).mapTherapyFromEhr(
        medicationOrder,
        compositionUid,
        createdTimestamp);
  }

  public MedicationOrder convertToMedicationOrder(final TherapyDto therapy)
  {
    return therapyConverterSelector.getConverter(therapy).mapTherapyToEhr(therapy);
  }
}
