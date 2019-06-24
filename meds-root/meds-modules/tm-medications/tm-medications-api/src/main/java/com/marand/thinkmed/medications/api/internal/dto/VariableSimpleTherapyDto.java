package com.marand.thinkmed.medications.api.internal.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class VariableSimpleTherapyDto extends SimpleTherapyDto implements VariableTherapy
{
  private List<TimedSimpleDoseElementDto> timedDoseElements = new ArrayList<>();

  public VariableSimpleTherapyDto()
  {
    super(true);
  }

  public List<TimedSimpleDoseElementDto> getTimedDoseElements()
  {
    return timedDoseElements;
  }

  public void setTimedDoseElements(final List<TimedSimpleDoseElementDto> timedDoseElements)
  {
    this.timedDoseElements = timedDoseElements;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("timedDoseElements", timedDoseElements)
    ;
  }
}
