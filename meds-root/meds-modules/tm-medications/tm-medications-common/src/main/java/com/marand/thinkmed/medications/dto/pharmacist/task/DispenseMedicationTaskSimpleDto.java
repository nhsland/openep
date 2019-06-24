package com.marand.thinkmed.medications.dto.pharmacist.task;

import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class DispenseMedicationTaskSimpleDto extends MedicationSupplyTaskSimpleDto
{
  private TherapyAssigneeEnum requesterRole;

  public DispenseMedicationTaskSimpleDto()
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

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("requesterRole", requesterRole);
  }
}
