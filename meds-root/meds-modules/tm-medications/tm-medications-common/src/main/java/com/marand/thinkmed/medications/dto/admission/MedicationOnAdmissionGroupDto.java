package com.marand.thinkmed.medications.dto.admission;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapySourceGroupEnum;
import com.marand.thinkmed.medications.dto.reconsiliation.SourceMedicationDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author nejck
 */
public class MedicationOnAdmissionGroupDto extends DataTransferObject implements JsonSerializable
{
  private TherapySourceGroupEnum groupEnum;
  private String groupName;
  private List<SourceMedicationDto> groupElements = new ArrayList<>();
  private DateTime lastUpdateTime;

  public MedicationOnAdmissionGroupDto(final TherapySourceGroupEnum groupEnum, final String groupName)
  {
    this.groupEnum = groupEnum;
    this.groupName = groupName;
  }

  public TherapySourceGroupEnum getGroupEnum()
  {
    return groupEnum;
  }

  public void setGroupEnum(final TherapySourceGroupEnum groupEnum)
  {
    this.groupEnum = groupEnum;
  }

  public List<SourceMedicationDto> getGroupElements()
  {
    return groupElements;
  }

  public String getGroupName()
  {
    return groupName;
  }

  public void setGroupName(final String groupName)
  {
    this.groupName = groupName;
  }

  public void setGroupElements(final List<SourceMedicationDto> groupElements)
  {
    this.groupElements = groupElements;
  }

  public DateTime getLastUpdateTime()
  {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(final DateTime lastUpdateTime)
  {
    this.lastUpdateTime = lastUpdateTime;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("groupEnum", groupEnum)
        .append("groupName", groupName)
        .append("therapies", groupElements)
        .append("lastUpdateTime", lastUpdateTime)
    ;
  }
}
