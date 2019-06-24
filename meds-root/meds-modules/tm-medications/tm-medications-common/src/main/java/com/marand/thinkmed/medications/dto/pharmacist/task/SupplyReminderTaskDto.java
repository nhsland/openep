package com.marand.thinkmed.medications.dto.pharmacist.task;

import com.marand.thinkmed.medications.TaskTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Klavdij Lapajne
 */

public class SupplyReminderTaskDto extends MedicationSupplyTaskDto
{
  private DateTime dueDate;

  public SupplyReminderTaskDto()
  {
    setTaskType(TaskTypeEnum.SUPPLY_REMINDER);
  }

  public DateTime getDueDate()
  {
    return dueDate;
  }

  public void setDueDate(final DateTime dueDate)
  {
    this.dueDate = dueDate;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("dueDate", dueDate);
  }
}
