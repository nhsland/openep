package com.marand.thinkmed.medications.dto.pharmacist.task;

import com.marand.thinkmed.medications.TaskTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class SupplyReminderTaskSimpleDto extends MedicationSupplyTaskSimpleDto
{
  private boolean isDismissed;

  public SupplyReminderTaskSimpleDto()
  {
    setTaskType(TaskTypeEnum.SUPPLY_REMINDER);
  }

  public boolean isDismissed()
  {
    return isDismissed;
  }

  public void setDismissed(final boolean dismissed)
  {
    isDismissed = dismissed;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("isDismissed", isDismissed);
  }
}
