package com.marand.thinkmed.medications.dto.pharmacist.task;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class MedicationSupplyTaskSimpleDto extends DataTransferObject
{
  private String taskId;
  private MedicationSupplyTypeEnum supplyTypeEnum;
  private TaskTypeEnum taskType;
  private Integer supplyInDays;

  public String getTaskId()
  {
    return taskId;
  }

  public void setTaskId(final String taskId)
  {
    this.taskId = taskId;
  }

  public MedicationSupplyTypeEnum getSupplyTypeEnum()
  {
    return supplyTypeEnum;
  }

  public void setSupplyTypeEnum(final MedicationSupplyTypeEnum supplyTypeEnum)
  {
    this.supplyTypeEnum = supplyTypeEnum;
  }

  public TaskTypeEnum getTaskType()
  {
    return taskType;
  }

  public void setTaskType(final TaskTypeEnum taskType)
  {
    this.taskType = taskType;
  }

  public Integer getSupplyInDays()
  {
    return supplyInDays;
  }

  public void setSupplyInDays(final Integer supplyInDays)
  {
    this.supplyInDays = supplyInDays;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("taskId", taskId)
        .append("supplyTypeEnum", supplyTypeEnum)
        .append("taskType", taskType)
        .append("supplyInDays", supplyInDays)
    ;
  }
}
