package com.marand.thinkmed.medications.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.maf.core.data.object.HourMinuteDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class AdministrationTimingDto extends DataTransferObject
{
  private List<AdministrationTimestampsDto> timestampsList = new ArrayList<>();

  public List<AdministrationTimestampsDto> getTimestampsList()
  {
    return timestampsList;
  }

  public void setTimestampsList(final List<AdministrationTimestampsDto> timestampsList)
  {
    this.timestampsList = timestampsList;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("timestampsList", timestampsList);
  }

  public static class AdministrationTimestampsDto extends DataTransferObject
  {
    private String frequency;
    private List<HourMinuteDto> timesList = new ArrayList<>();

    public String getFrequency()
    {
      return frequency;
    }

    public void setFrequency(final String frequency)
    {
      this.frequency = frequency;
    }

    public List<HourMinuteDto> getTimesList()
    {
      return timesList;
    }

    public void setTimesList(final List<HourMinuteDto> timesList)
    {
      this.timesList = timesList;
    }

    @Override
    protected void appendToString(final ToStringBuilder tsb)
    {
      tsb.append("frequency", frequency)
          .append("timesList", timesList);
    }
  }
}
