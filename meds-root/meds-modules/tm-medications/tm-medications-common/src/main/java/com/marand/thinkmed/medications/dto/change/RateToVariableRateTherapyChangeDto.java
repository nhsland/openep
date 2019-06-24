package com.marand.thinkmed.medications.dto.change;

import java.util.List;

import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;

/**
 * @author Mitja Lapajne
 */

public class RateToVariableRateTherapyChangeDto extends TherapyChangeDto<String, List<TimedComplexDoseElementDto>>
{
  public RateToVariableRateTherapyChangeDto()
  {
    super(TherapyChangeType.RATE_TO_VARIABLE_RATE);
  }
}