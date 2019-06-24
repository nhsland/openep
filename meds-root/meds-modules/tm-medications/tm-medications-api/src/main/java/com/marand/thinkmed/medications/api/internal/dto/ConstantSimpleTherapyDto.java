package com.marand.thinkmed.medications.api.internal.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.SimpleDoseElementDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class ConstantSimpleTherapyDto extends SimpleTherapyDto implements ConstantTherapy
{
  private SimpleDoseElementDto doseElement;
  private TitrationType titration;
  private List<HourMinuteDto> doseTimes = new ArrayList<>();

  public ConstantSimpleTherapyDto()
  {
    super(false);
  }

  public SimpleDoseElementDto getDoseElement()
  {
    return doseElement;
  }

  public void setDoseElement(final SimpleDoseElementDto doseElement)
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

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("doseElement", doseElement)
        .append("titration", titration)
        .append("doseTimes", doseTimes);
  }
}
