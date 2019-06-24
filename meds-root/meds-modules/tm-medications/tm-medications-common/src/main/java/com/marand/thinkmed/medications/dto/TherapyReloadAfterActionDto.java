package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class TherapyReloadAfterActionDto extends DataTransferObject implements JsonSerializable
{
  private String ehrCompositionId;
  private String ehrOrderName;

  private TherapyStatusEnum therapyStatus;
  private boolean doctorReviewNeeded;
  private boolean therapyEndsBeforeNextRounds;
  private DateTime therapyStart;
  private DateTime therapyEnd;
  private String statusReason;

  public String getEhrCompositionId()
  {
    return ehrCompositionId;
  }

  public void setEhrCompositionId(final String ehrCompositionId)
  {
    this.ehrCompositionId = ehrCompositionId;
  }

  public String getEhrOrderName()
  {
    return ehrOrderName;
  }

  public void setEhrOrderName(final String ehrOrderName)
  {
    this.ehrOrderName = ehrOrderName;
  }

  public TherapyStatusEnum getTherapyStatus()
  {
    return therapyStatus;
  }

  public void setTherapyStatus(final TherapyStatusEnum therapyStatus)
  {
    this.therapyStatus = therapyStatus;
  }

  public boolean isDoctorReviewNeeded()
  {
    return doctorReviewNeeded;
  }

  public void setDoctorReviewNeeded(final boolean doctorReviewNeeded)
  {
    this.doctorReviewNeeded = doctorReviewNeeded;
  }

  public boolean isTherapyEndsBeforeNextRounds()
  {
    return therapyEndsBeforeNextRounds;
  }

  public void setTherapyEndsBeforeNextRounds(final boolean therapyEndsBeforeNextRounds)
  {
    this.therapyEndsBeforeNextRounds = therapyEndsBeforeNextRounds;
  }

  public DateTime getTherapyStart()
  {
    return therapyStart;
  }

  public void setTherapyStart(final DateTime therapyStart)
  {
    this.therapyStart = therapyStart;
  }

  public DateTime getTherapyEnd()
  {
    return therapyEnd;
  }

  public void setTherapyEnd(final DateTime therapyEnd)
  {
    this.therapyEnd = therapyEnd;
  }

  public String getStatusReason()
  {
    return statusReason;
  }

  public void setStatusReason(final String statusReason)
  {
    this.statusReason = statusReason;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("ehrCompositionId", ehrCompositionId)
        .append("ehrOrderName", ehrOrderName)
        .append("therapyStatus", therapyStatus)
        .append("doctorReviewNeeded", doctorReviewNeeded)
        .append("therapyEndsBeforeNextRounds", therapyEndsBeforeNextRounds)
        .append("therapyStart", therapyStart)
        .append("therapyEnd", therapyEnd)
        .append("suspendReason", statusReason)
    ;
  }
}
