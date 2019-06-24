package com.marand.thinkmed.medications.dto.change;

import java.util.List;

import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;

/**
 * @author Mitja Lapajne
 */

public class VariableRateToRateTherapyChangeDto extends TherapyChangeDto<List<TimedComplexDoseElementDto>, String>
{
  public VariableRateToRateTherapyChangeDto()
  {
    super(TherapyChangeType.VARIABLE_RATE_TO_RATE);
  }
}