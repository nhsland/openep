package com.marand.thinkmed.medications.api.internal.dto;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class TherapyDoseDto extends DataTransferObject
{
  private TherapyDoseTypeEnum therapyDoseTypeEnum;
  private Double numerator;
  private String numeratorUnit;
  private Double denominator;
  private String denominatorUnit;
  private Double secondaryNumerator;
  private String secondaryNumeratorUnit;
  private Double secondaryDenominator;
  private String secondaryDenominatorUnit;

  public Double getNumerator()
  {
    return numerator;
  }

  public void setNumerator(final Double numerator)
  {
    this.numerator = numerator;
  }

  public String getNumeratorUnit()
  {
    return numeratorUnit;
  }

  public void setNumeratorUnit(final String numeratorUnit)
  {
    this.numeratorUnit = numeratorUnit;
  }

  public Double getDenominator()
  {
    return denominator;
  }

  public void setDenominator(final Double denominator)
  {
    this.denominator = denominator;
  }

  public String getDenominatorUnit()
  {
    return denominatorUnit;
  }

  public void setDenominatorUnit(final String denominatorUnit)
  {
    this.denominatorUnit = denominatorUnit;
  }


  public Double getSecondaryNumerator()
  {
    return secondaryNumerator;
  }

  public void setSecondaryNumerator(final Double secondaryNumerator)
  {
    this.secondaryNumerator = secondaryNumerator;
  }

  public String getSecondaryNumeratorUnit()
  {
    return secondaryNumeratorUnit;
  }

  public void setSecondaryNumeratorUnit(final String secondaryNumeratorUnit)
  {
    this.secondaryNumeratorUnit = secondaryNumeratorUnit;
  }

  public Double getSecondaryDenominator()
  {
    return secondaryDenominator;
  }

  public void setSecondaryDenominator(final Double secondaryDenominator)
  {
    this.secondaryDenominator = secondaryDenominator;
  }

  public String getSecondaryDenominatorUnit()
  {
    return secondaryDenominatorUnit;
  }

  public void setSecondaryDenominatorUnit(final String secondaryDenominatorUnit)
  {
    this.secondaryDenominatorUnit = secondaryDenominatorUnit;
  }

  public TherapyDoseTypeEnum getTherapyDoseTypeEnum()
  {
    return therapyDoseTypeEnum;
  }

  public void setTherapyDoseTypeEnum(final TherapyDoseTypeEnum therapyDoseTypeEnum)
  {
    this.therapyDoseTypeEnum = therapyDoseTypeEnum;
  }

  @SuppressWarnings("OverlyComplexBooleanExpression")
  @Override
  public boolean equals(final Object obj)
  {
    if (obj instanceof TherapyDoseDto)
    {
      final TherapyDoseDto doseDto = (TherapyDoseDto)obj;

      final boolean compareNumerator =
          (doseDto.getNumerator() == null && numerator == null)
              || (doseDto.getNumerator() != null && numerator != null
              && Math.abs(doseDto.getNumerator() - numerator) < 0.0001
              && doseDto.getNumeratorUnit().equals(numeratorUnit));

      final boolean compareDenominator =
          (doseDto.getDenominator() == null && denominator == null)
              || (doseDto.getDenominator() != null && denominator != null
              && Math.abs(doseDto.getDenominator() - denominator) < 0.0001
              && doseDto.getDenominatorUnit().equals(denominatorUnit));

      return compareNumerator && compareDenominator;
    }

    return false;
  }

  @Override
  public int hashCode()
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append(therapyDoseTypeEnum);
    buffer.append(numerator);
    buffer.append(numeratorUnit);
    buffer.append(denominator);
    buffer.append(denominatorUnit);
    buffer.append(secondaryNumerator);
    buffer.append(secondaryNumeratorUnit);
    buffer.append(secondaryDenominator);
    buffer.append(secondaryDenominatorUnit);
    return buffer.toString().hashCode();
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("numerator", numerator)
        .append("numeratorUnit", numeratorUnit)
        .append("denominator", denominator)
        .append("denominatorUnit", denominatorUnit)
        .append("therapyDoseTypeEnum", therapyDoseTypeEnum)
        .append("secondaryNumerator", secondaryNumerator)
        .append("secondaryNumeratorUnit", secondaryNumeratorUnit)
        .append("secondaryDenominator", secondaryDenominator)
        .append("secondaryDenominatorUnit", secondaryDenominatorUnit);
  }
}
