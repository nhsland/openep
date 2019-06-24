package com.marand.thinkmed.medications.connector.data.object;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class BolusQuantityWithTimeDto extends QuantityWithTimeDto
{
  private final Double bolusQuantity;
  private final String bolusUnit;

  public BolusQuantityWithTimeDto(
      final DateTime time,
      final Double quantity,
      final String comment,
      final Double bolusQuantity,
      final String bolusUnit)
  {
    super(time, quantity, comment);
    this.bolusQuantity = bolusQuantity;
    this.bolusUnit = bolusUnit;
  }

  public Double getBolusQuantity()
  {
    return bolusQuantity;
  }

  public String getBolusUnit()
  {
    return bolusUnit;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("bolusQuantity", bolusQuantity)
        .append("bolusUnit", bolusUnit)
    ;
  }
}
