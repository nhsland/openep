package com.marand.thinkmed.medications.dto.pharmacist.task;

import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class DischargeReviewTaskDto extends PatientTaskDto
{
  private String lastEditorName;
  private DateTime lastEditTimestamp;

  public DischargeReviewTaskDto()
  {
    setTaskType(TaskTypeEnum.DISCHARGE_REVIEW_TASK);
  }

  public String getLastEditorName()
  {
    return lastEditorName;
  }

  public void setLastEditorName(final String lastEditorName)
  {
    this.lastEditorName = lastEditorName;
  }

  public DateTime getLastEditTimestamp()
  {
    return lastEditTimestamp;
  }

  public void setLastEditTimestamp(final DateTime lastEditTimestamp)
  {
    this.lastEditTimestamp = lastEditTimestamp;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("lastEditorName", lastEditorName)
        .append("lastEditTimestamp", lastEditTimestamp);
  }
}
