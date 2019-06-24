package com.marand.thinkmed.medications.connector.data.object;

/**
 * @author Mitja Lapajne
 */
public enum ObservationType
{
  BLOOD_SUGAR("mmol/l"),
  INR(null);

  private final String unit;

  ObservationType(final String unit)
  {
    this.unit = unit;
  }

  public String getUnit()
  {
    return unit;
  }
}
