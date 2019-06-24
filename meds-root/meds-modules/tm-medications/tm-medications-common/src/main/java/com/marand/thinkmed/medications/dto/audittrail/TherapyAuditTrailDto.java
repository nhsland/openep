package com.marand.thinkmed.medications.dto.audittrail;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.dto.TherapyActionHistoryDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyAuditTrailDto extends DataTransferObject implements JsonSerializable
{
  private TherapyDto currentTherapy;
  private TherapyStatusEnum currentTherapyStatus;
  private TherapyDto originalTherapy;
  private List<TherapyActionHistoryDto> actionHistoryList = new ArrayList<>();

  public TherapyDto getCurrentTherapy()
  {
    return currentTherapy;
  }

  public void setCurrentTherapy(final TherapyDto currentTherapy)
  {
    this.currentTherapy = currentTherapy;
  }

  public TherapyStatusEnum getCurrentTherapyStatus()
  {
    return currentTherapyStatus;
  }

  public void setCurrentTherapyStatus(final TherapyStatusEnum currentTherapyStatus)
  {
    this.currentTherapyStatus = currentTherapyStatus;
  }

  public TherapyDto getOriginalTherapy()
  {
    return originalTherapy;
  }

  public void setOriginalTherapy(final TherapyDto originalTherapy)
  {
    this.originalTherapy = originalTherapy;
  }

  public List<TherapyActionHistoryDto> getActionHistoryList()
  {
    return actionHistoryList;
  }

  public void setActionHistoryList(final List<TherapyActionHistoryDto> actionHistoryList)
  {
    this.actionHistoryList = actionHistoryList;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("currentTherapy", currentTherapy)
        .append("currentTherapyStatus", currentTherapyStatus)
        .append("originalTherapy", originalTherapy)
        .append("actionHistoryList", actionHistoryList);
  }
}