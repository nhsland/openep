package com.marand.thinkmed.medications.dto.pharmacist.task;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class PerfusionSyringePatientTasksDto extends PatientTherapyTasksDto<PerfusionSyringeTaskDto>
{
  private boolean urgent;

  public boolean isUrgent()
  {
    return urgent;
  }

  public void setUrgent(final boolean urgent)
  {
    this.urgent = urgent;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("numberOfSyringes", urgent);
  }
}
