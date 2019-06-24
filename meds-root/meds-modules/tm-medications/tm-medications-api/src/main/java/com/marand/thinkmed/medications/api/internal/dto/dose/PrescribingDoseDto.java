package com.marand.thinkmed.medications.api.internal.dto.dose;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public class PrescribingDoseDto extends DataTransferObject implements JsonSerializable
{
  private final double numerator;
  private final String numeratorUnit;
  private final Double denominator;
  private final String denominatorUnit;

  public PrescribingDoseDto(
      final double numerator,
      final String numeratorUnit,
      final Double denominator,
      final String denominatorUnit)
  {
    this.numerator = numerator;
    this.numeratorUnit = numeratorUnit;
    this.denominator = denominator;
    this.denominatorUnit = denominatorUnit;
  }

  public double getNumerator()
  {
    return numerator;
  }

  public String getNumeratorUnit()
  {
    return numeratorUnit;
  }

  public Double getDenominator()
  {
    return denominator;
  }

  public String getDenominatorUnit()
  {
    return denominatorUnit;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("numerator", numerator)
        .append("numeratorUnit", numeratorUnit)
        .append("denominator", denominator)
        .append("denominatorUnit", denominatorUnit)
    ;
  }
}
