package com.marand.thinkmed.medications.dto.change;

import java.util.List;

import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;

/**
 * @author Mitja Lapajne
 */

public class VariableDoseTherapyChangeDto
    extends TherapyChangeDto<List<TimedSimpleDoseElementDto>, List<TimedSimpleDoseElementDto>>
{
  public VariableDoseTherapyChangeDto()
  {
    super(TherapyChangeType.VARIABLE_DOSE);
  }
}