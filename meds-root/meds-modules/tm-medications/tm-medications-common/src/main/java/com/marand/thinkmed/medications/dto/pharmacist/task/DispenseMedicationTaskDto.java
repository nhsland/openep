package com.marand.thinkmed.medications.dto.pharmacist.task;

import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Klavdij Lapajne
 */

public class DispenseMedicationTaskDto extends MedicationSupplyTaskDto
{
  private SupplyRequestStatus supplyRequestStatus;
  private TherapyAssigneeEnum requesterRole;
  private DateTime lastPrintedTimestamp;

  public DispenseMedicationTaskDto()
  {
    setTaskType(TaskTypeEnum.DISPENSE_MEDICATION);
  }

  public TherapyAssigneeEnum getRequesterRole()
  {
    return requesterRole;
  }

  public void setRequesterRole(final TherapyAssigneeEnum requesterRole)
  {
    this.requesterRole = requesterRole;
  }

  public SupplyRequestStatus getSupplyRequestStatus()
  {
    return supplyRequestStatus;
  }

  public void setSupplyRequestStatus(final SupplyRequestStatus supplyRequestStatus)
  {
    this.supplyRequestStatus = supplyRequestStatus;
  }

  public DateTime getLastPrintedTimestamp()
  {
    return lastPrintedTimestamp;
  }

  public void setLastPrintedTimestamp(final DateTime lastPrintedTimestamp)
  {
    this.lastPrintedTimestamp = lastPrintedTimestamp;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("supplyRequestStatus", supplyRequestStatus)
        .append("requesterRole", requesterRole)
        .append("lastPrintedTimestamp", lastPrintedTimestamp);
  }
}
