package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;

/**
 * @author Nejc Korasa
 */

public interface PlannedDoseAdministration
{
  boolean isDifferentFromOrder();

  void setDifferentFromOrder(final boolean differentFromOrder);

  TherapyDoseDto getPlannedDose();

  void setPlannedDose(final TherapyDoseDto plannedDose);
}
