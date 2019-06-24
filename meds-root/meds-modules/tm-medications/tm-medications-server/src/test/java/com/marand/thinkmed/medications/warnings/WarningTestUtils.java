package com.marand.thinkmed.medications.warnings;

import java.util.Collections;

import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;

public class WarningTestUtils
{
  private WarningTestUtils() { }

  public static TherapyDto buildTherapy(final long medicationId, final String medicationName, final long routeId)
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setMedication(new MedicationDto(medicationId, medicationName));

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(routeId);

    therapy.setRoutes(Collections.singletonList(route));

    return therapy;
  }

  public static TherapyDto buildTherapy(
      final long medicationId,
      final String medicationName,
      final long routeId,
      final Integer maxDosePercentage)
  {
    final TherapyDto therapy = buildTherapy(medicationId, medicationName, routeId);
    therapy.setMaxDosePercentage(maxDosePercentage);
    return therapy;
  }
}
