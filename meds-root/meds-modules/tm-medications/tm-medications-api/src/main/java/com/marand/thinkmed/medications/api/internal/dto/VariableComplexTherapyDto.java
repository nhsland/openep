package com.marand.thinkmed.medications.api.internal.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class VariableComplexTherapyDto extends ComplexTherapyDto implements VariableTherapy
{
  private List<TimedComplexDoseElementDto> timedDoseElements = new ArrayList<>();
  private boolean recurringContinuousInfusion;

  public VariableComplexTherapyDto()
  {
    super(true);
  }

  public List<TimedComplexDoseElementDto> getTimedDoseElements()
  {
    return timedDoseElements;
  }

  public void setTimedDoseElements(final List<TimedComplexDoseElementDto> timedDoseElements)
  {
    this.timedDoseElements = timedDoseElements;
  }

  public boolean isRecurringContinuousInfusion()
  {
    return recurringContinuousInfusion;
  }

  public void setRecurringContinuousInfusion(final boolean recurringContinuousInfusion)
  {
    this.recurringContinuousInfusion = recurringContinuousInfusion;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("timedDoseElements", timedDoseElements)
        .append("recurringContinuousInfusion", recurringContinuousInfusion)
        ;
  }
}
