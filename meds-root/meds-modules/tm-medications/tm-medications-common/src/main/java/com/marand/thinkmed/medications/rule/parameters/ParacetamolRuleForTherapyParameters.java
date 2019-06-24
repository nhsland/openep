package com.marand.thinkmed.medications.rule.parameters;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class ParacetamolRuleForTherapyParameters extends ParacetamolRuleParameters
{
  private TherapyDto therapyDto;
  private List<MedicationDataDto> medicationDataDtoList = new ArrayList<>();

  public TherapyDto getTherapyDto()
  {
    return therapyDto;
  }

  public void setTherapyDto(final TherapyDto therapyDto)
  {
    this.therapyDto = therapyDto;
  }

  public List<MedicationDataDto> getMedicationDataDtoList()
  {
    return medicationDataDtoList;
  }

  public void setMedicationDataDtoList(final List<MedicationDataDto> medicationDataDtoList)
  {
    this.medicationDataDtoList = medicationDataDtoList;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("therapyDto", therapyDto)
        .append("medicationDataDtoList", medicationDataDtoList)
    ;
  }
}
