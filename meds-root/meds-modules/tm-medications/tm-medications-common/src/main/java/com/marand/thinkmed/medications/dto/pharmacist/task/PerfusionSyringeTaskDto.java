package com.marand.thinkmed.medications.dto.pharmacist.task;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.api.internal.dto.pharmacist.perfusionSyringe.PerfusionSyringeLabelDto;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class PerfusionSyringeTaskDto extends TherapyTaskSimpleDto
{
  private int numberOfSyringes;
  private String originalTherapyId;
  private NamedExternalDto orderedBy;
  private TherapyDayDto therapyDayDto;
  private PerfusionSyringeLabelDto perfusionSyringeLabelDto;
  private boolean printSystemLabel;

  public int getNumberOfSyringes()
  {
    return numberOfSyringes;
  }

  public void setNumberOfSyringes(final int numberOfSyringes)
  {
    this.numberOfSyringes = numberOfSyringes;
  }

  public TherapyDayDto getTherapyDayDto()
  {
    return therapyDayDto;
  }

  public void setTherapyDayDto(final TherapyDayDto therapyDayDto)
  {
    this.therapyDayDto = therapyDayDto;
  }

  public String getOriginalTherapyId()
  {
    return originalTherapyId;
  }

  public void setOriginalTherapyId(final String originalTherapyId)
  {
    this.originalTherapyId = originalTherapyId;
  }

  public PerfusionSyringeLabelDto getPerfusionSyringeLabelDto()
  {
    return perfusionSyringeLabelDto;
  }

  public void setPerfusionSyringeLabelDto(final PerfusionSyringeLabelDto perfusionSyringeLabelDto)
  {
    this.perfusionSyringeLabelDto = perfusionSyringeLabelDto;
  }

  public NamedExternalDto getOrderedBy()
  {
    return orderedBy;
  }

  public void setOrderedBy(final NamedExternalDto orderedBy)
  {
    this.orderedBy = orderedBy;
  }

  public boolean isPrintSystemLabel()
  {
    return printSystemLabel;
  }

  public void setPrintSystemLabel(final boolean printSystemLabel)
  {
    this.printSystemLabel = printSystemLabel;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("numberOfSyringes", numberOfSyringes)
        .append("originalTherapyId", originalTherapyId)
        .append("orderedBy", orderedBy)
        .append("therapyDayDto", therapyDayDto)
        .append("perfusionSyringeLabelDto", perfusionSyringeLabelDto)
        .append("printSystemLabel", printSystemLabel);
  }
}
