package com.marand.thinkmed.medications.api.internal.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.ComplexDoseElementDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 * @author Mitja Lapajne
 */
public class ConstantComplexTherapyDto extends ComplexTherapyDto implements ConstantTherapy
{
  private ComplexDoseElementDto doseElement;
  private TitrationType titration;
  private List<HourMinuteDto> doseTimes = new ArrayList<>();
  private String rateString;

  private String durationDisplay;

  public ConstantComplexTherapyDto()
  {
    super(false);
  }

  public ComplexDoseElementDto getDoseElement()
  {
    return doseElement;
  }

  public void setDoseElement(final ComplexDoseElementDto doseElement)
  {
    this.doseElement = doseElement;
  }

  @Override
  public TitrationType getTitration()
  {
    return titration;
  }

  @Override
  public void setTitration(final TitrationType titration)
  {
    this.titration = titration;
  }

  @Override
  public List<HourMinuteDto> getDoseTimes()
  {
    return doseTimes;
  }

  @Override
  public void setDoseTimes(final List<HourMinuteDto> doseTimes)
  {
    this.doseTimes = doseTimes;
  }

  public String getRateString()
  {
    return rateString;
  }

  public void setRateString(final String rateString)
  {
    this.rateString = rateString;
  }

  public String getDurationDisplay()
  {
    return durationDisplay;
  }

  public void setDurationDisplay(final String durationDisplay)
  {
    this.durationDisplay = durationDisplay;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("doseElement", doseElement)
        .append("titration", titration)
        .append("doseTimes", doseTimes)
        .append("rateString", rateString)
        .append("durationDisplay", durationDisplay)
    ;
  }
}
