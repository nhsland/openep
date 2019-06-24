package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */
public class AdministrationPatientTaskLimitsDto extends DataTransferObject
{
  private int dueTaskOffset;  //in minutes
  private int futureTaskOffset; //in minutes;
  private int maxNumberOfTasks;

  public int getDueTaskOffset()
  {
    return dueTaskOffset;
  }

  public void setDueTaskOffset(final int dueTaskOffset)
  {
    this.dueTaskOffset = dueTaskOffset;
  }

  public int getFutureTaskOffset()
  {
    return futureTaskOffset;
  }

  public void setFutureTaskOffset(final int futureTaskOffset)
  {
    this.futureTaskOffset = futureTaskOffset;
  }

  public int getMaxNumberOfTasks()
  {
    return maxNumberOfTasks;
  }

  public void setMaxNumberOfTasks(final int maxNumberOfTasks)
  {
    this.maxNumberOfTasks = maxNumberOfTasks;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("maxNumberOfTasks", maxNumberOfTasks)
        .append("maxNumberOfTasks", maxNumberOfTasks)
        .append("maxNumberOfTasks", maxNumberOfTasks);
  }
}
