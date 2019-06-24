package com.marand.thinkmed.medications.api.internal.dto;

import java.util.Arrays;

/**
 * @author Mitja Lapajne
 */
public enum TitrationType
{
  BLOOD_SUGAR("mmol/l", DosageJustificationEnum.TITRATION_BLOOD_SUGAR),
  MAP("mm[Hg]", DosageJustificationEnum.TITRATION_MAP), //Mean Arterial Pressure
  INR("", DosageJustificationEnum.TITRATION_INR),
  APTTR("", DosageJustificationEnum.TITRATION_APTTR);

  private final String unit;
  private final DosageJustificationEnum dosageJustificationEnum;

  TitrationType(final String unit, final DosageJustificationEnum dosageJustificationEnum)
  {
    this.unit = unit;
    this.dosageJustificationEnum = dosageJustificationEnum;
  }

  public String getUnit()
  {
    return unit;
  }

  public DosageJustificationEnum getDosageJustificationEnum()
  {
    return dosageJustificationEnum;
  }

  public static TitrationType valueOf(final DosageJustificationEnum dosageJustification)
  {
    return Arrays.stream(values())
        .filter(v -> v.getDosageJustificationEnum() == dosageJustification)
        .findFirst()
        .orElse(null);
  }
}
