package com.marand.thinkmed.medications.dto.pharmacist.task;

import com.marand.thinkmed.process.dto.AbstractTaskDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class PerfusionSyringeTaskSimpleDto extends AbstractTaskDto
{
  private int numberOfSyringes;
  private boolean urgent;
  private boolean printSystemLabel;

  public boolean isUrgent()
  {
    return urgent;
  }

  public void setUrgent(final boolean urgent)
  {
    this.urgent = urgent;
  }

  public int getNumberOfSyringes()
  {
    return numberOfSyringes;
  }

  public void setNumberOfSyringes(final int numberOfSyringes)
  {
    this.numberOfSyringes = numberOfSyringes;
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
    super.appendToString(tsb);
    tsb
        .append("numberOfSyringes", numberOfSyringes)
        .append("urgent", urgent)
        .append("printSystemLabel", printSystemLabel);
  }
}
