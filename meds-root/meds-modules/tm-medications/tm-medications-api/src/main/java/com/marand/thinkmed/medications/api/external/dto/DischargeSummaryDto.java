package com.marand.thinkmed.medications.api.external.dto;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class DischargeSummaryDto
{
  private DateTime admissionLastUpdateTime;
  private DateTime dischargeLastUpdateTime;
  private List<DischargeSummaryItemDto> items = new ArrayList<>();

  public DateTime getAdmissionLastUpdateTime()
  {
    return admissionLastUpdateTime;
  }

  public void setAdmissionLastUpdateTime(final DateTime admissionLastUpdateTime)
  {
    this.admissionLastUpdateTime = admissionLastUpdateTime;
  }

  public DateTime getDischargeLastUpdateTime()
  {
    return dischargeLastUpdateTime;
  }

  public void setDischargeLastUpdateTime(final DateTime dischargeLastUpdateTime)
  {
    this.dischargeLastUpdateTime = dischargeLastUpdateTime;
  }

  public List<DischargeSummaryItemDto> getItems()
  {
    return items;
  }

  public void setItems(final List<DischargeSummaryItemDto> items)
  {
    this.items = items;
  }
}
