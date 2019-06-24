package com.marand.thinkmed.medications.charting;

import lombok.NonNull;

import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public class NormalInfusionAutomaticChartingDto extends TherapyAutomaticChartingDto
{
  public NormalInfusionAutomaticChartingDto(
      final @NonNull String compositionUid,
      final @NonNull String instructionName,
      final @NonNull String patientId)
  {
    super(AutomaticChartingType.NORMAL_INFUSION, compositionUid, instructionName, patientId);
  }

  @Override
  public boolean isEnabled(final @NonNull DateTime when)
  {
    return true;
  }
}
