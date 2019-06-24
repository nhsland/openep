package com.marand.thinkmed.medications.dto.change;

import java.util.List;

import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;

/**
 * @author Mitja Lapajne
 */

public class VariableRateTherapyChangeDto extends TherapyChangeDto<List<TimedComplexDoseElementDto>, List<TimedComplexDoseElementDto>>
{
  public VariableRateTherapyChangeDto()
  {
    super(TherapyChangeType.VARIABLE_RATE);
  }
}