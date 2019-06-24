package com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.api.internal.dto.pharmacist.perfusionSyringe.PerfusionSyringeLabelDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class PerfusionSyringePreparationDto extends DataTransferObject
{
  private String completePreparationTaskId;
  private PerfusionSyringeLabelDto perfusionSyringeLabelDto;

  public String getCompletePreparationTaskId()
  {
    return completePreparationTaskId;
  }

  public void setCompletePreparationTaskId(final String completePreparationTaskId)
  {
    this.completePreparationTaskId = completePreparationTaskId;
  }

  public PerfusionSyringeLabelDto getPerfusionSyringeLabelDto()
  {
    return perfusionSyringeLabelDto;
  }

  public void setPerfusionSyringeLabelDto(final PerfusionSyringeLabelDto perfusionSyringeLabelDto)
  {
    this.perfusionSyringeLabelDto = perfusionSyringeLabelDto;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("completePreparationTaskId", completePreparationTaskId)
        .append("perfusionSyringeLabelDto", perfusionSyringeLabelDto);
  }
}
