package com.marand.thinkmed.medications.dto.report;

/**
 * @author Vid Kumse
 */

public class TherapyDayReportAdministrationDto
{
  private String timeDose;
  private String comment;

  public TherapyDayReportAdministrationDto(final String timeDose, final String comment)
  {
    this.timeDose = timeDose;
    this.comment = comment;
  }

  public String getTimeDose()
  {
    return timeDose;
  }

  public void setTimeDose(final String timeDose)
  {
    this.timeDose = timeDose;
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(final String comment)
  {
    this.comment = comment;
  }
}
