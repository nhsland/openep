package com.marand.thinkmed.medications.dto.change;

import java.util.List;

import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;

/**
 * @author Mitja Lapajne
 */

public class VariableDoseToDoseTherapyChangeDto extends TherapyChangeDto<List<TimedSimpleDoseElementDto>, String>
{
  public VariableDoseToDoseTherapyChangeDto()
  {
    super(TherapyChangeType.VARIABLE_DOSE_TO_DOSE);
  }
}