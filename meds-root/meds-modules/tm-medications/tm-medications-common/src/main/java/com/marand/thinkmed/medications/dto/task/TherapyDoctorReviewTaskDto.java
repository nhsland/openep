package com.marand.thinkmed.medications.dto.task;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Vid Kumse
 */
public class TherapyDoctorReviewTaskDto extends TherapyTaskSimpleDto
{
  private String comment;

  public String getComment()
  {
    return comment;
  }

  public void setComment(final String comment)
  {
    this.comment = comment;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("comment", comment)
    ;
  }
}
