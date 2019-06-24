package com.marand.thinkmed.medications.dto.pharmacist.task;

import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.PrescriptionChangeTypeEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class PharmacistReviewTaskDto extends PatientTaskDto
{
  private DateTime firstAdministrationTimestamp;
  private String lastEditorName;
  private DateTime lastEditTimestamp;
  private PrescriptionChangeTypeEnum changeType;
  private PharmacistReviewTaskStatusEnum status;

  public PharmacistReviewTaskDto()
  {
    setTaskType(TaskTypeEnum.PHARMACIST_REVIEW);
  }

  public DateTime getFirstAdministrationTimestamp()
  {
    return firstAdministrationTimestamp;
  }

  public void setFirstAdministrationTimestamp(final DateTime firstAdministrationTimestamp)
  {
    this.firstAdministrationTimestamp = firstAdministrationTimestamp;
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

  public PrescriptionChangeTypeEnum getChangeType()
  {
    return changeType;
  }

  public void setChangeType(final PrescriptionChangeTypeEnum changeType)
  {
    this.changeType = changeType;
  }

  public PharmacistReviewTaskStatusEnum getStatus()
  {
    return status;
  }

  public void setStatus(final PharmacistReviewTaskStatusEnum status)
  {
    this.status = status;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("firstAdministrationTimestamp", firstAdministrationTimestamp)
        .append("lastEditorName", lastEditorName)
        .append("lastEditTimestamp", lastEditTimestamp)
        .append("changeType", changeType)
        .append("status", status)
    ;
  }
}
