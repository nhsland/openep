package com.marand.thinkmed.medications.dto.pharmacist.task;

import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Klavdij Lapajne
 */

public class PharmacistReminderTaskDto extends PatientTaskDto
{
  private DateTime reminderDate;
  private String reminderNote;

  public PharmacistReminderTaskDto()
  {
    setTaskType(TaskTypeEnum.PHARMACIST_REMINDER);
  }

  public DateTime getReminderDate()
  {
    return reminderDate;
  }

  public void setReminderDate(final DateTime reminderDate)
  {
    this.reminderDate = reminderDate;
  }

  public void setReminderNote(final String reminderNote)
  {
    this.reminderNote = reminderNote;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("reminderDate", reminderDate)
        .append("reminderNote", reminderNote);
  }
}
