package com.marand.thinkmed.medications.dto.change;

import java.util.List;

import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;

/**
 * @author Mitja Lapajne
 */

public class DoseToVariableDoseTherapyChangeDto extends TherapyChangeDto<String, List<TimedSimpleDoseElementDto>>
{
  public DoseToVariableDoseTherapyChangeDto()
  {
    super(TherapyChangeType.DOSE_TO_VARIABLE_DOSE);
  }
}