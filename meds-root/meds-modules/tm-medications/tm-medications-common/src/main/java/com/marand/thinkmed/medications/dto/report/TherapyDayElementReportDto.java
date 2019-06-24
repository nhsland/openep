package com.marand.thinkmed.medications.dto.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class TherapyDayElementReportDto extends TherapyDayCombinedDisplayDto
{
  private String customGroupName;
  private int customGroupSortOrder;
  private List<AdministrationDto> administrations = new ArrayList<>();
  private List<TherapyReportHourDoseTimeDto> hourDoseTime = new ArrayList<>();
  private String doseDescription;
  private Map<DateTime, List<TherapyReportHourDoseTimeDto>> dateDoseTime = new HashMap<>();
  private String lastInrResult; //value and timestamp

  public Map<DateTime, List<TherapyReportHourDoseTimeDto>> getDateDoseTime()
  {
    return dateDoseTime;
  }

  public void setDateDoseTime(final Map<DateTime, List<TherapyReportHourDoseTimeDto>> dateDoseTime)
  {
    this.dateDoseTime = dateDoseTime;
  }

  public String getDoseDescription()
  {
    return doseDescription;
  }

  public void setDoseDescription(final String doseDescription)
  {
    this.doseDescription = doseDescription;
  }

  public List<TherapyReportHourDoseTimeDto> getHourDoseTime()
  {
    return hourDoseTime;
  }

  public void setHourDoseTime(final List<TherapyReportHourDoseTimeDto> hourDoseTime)
  {
    this.hourDoseTime = hourDoseTime;
  }

  public String getCustomGroupName()
  {
    return customGroupName;
  }

  public void setCustomGroupName(final String customGroupName)
  {
    this.customGroupName = customGroupName;
  }

  public int getCustomGroupSortOrder()
  {
    return customGroupSortOrder;
  }

  public void setCustomGroupSortOrder(final int customGroupSortOrder)
  {
    this.customGroupSortOrder = customGroupSortOrder;
  }

  public List<AdministrationDto> getAdministrations()
  {
    return administrations;
  }

  public void setAdministrations(final List<AdministrationDto> administrations)
  {
    this.administrations = administrations;
  }

  public String getLastInrResult()
  {
    return lastInrResult;
  }

  public void setLastInrResult(final String lastInrResult)
  {
    this.lastInrResult = lastInrResult;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("customGroupName", customGroupName)
        .append("customGroupSortOrder", customGroupSortOrder)
        .append("administrations", administrations)
        .append("hourDoseTime", hourDoseTime)
        .append("doseDescription", doseDescription)
        .append("dateDoseTime", dateDoseTime)
        .append("lastInrResult", lastInrResult)
    ;
  }
}
