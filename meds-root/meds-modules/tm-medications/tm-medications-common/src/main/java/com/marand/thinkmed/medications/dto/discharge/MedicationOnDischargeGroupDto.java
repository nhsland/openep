package com.marand.thinkmed.medications.dto.discharge;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapySourceGroupEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author nejck
 */
public class MedicationOnDischargeGroupDto extends DataTransferObject implements JsonSerializable
{
  private TherapySourceGroupEnum groupEnum;
  private String groupName;
  private final List<DischargeSourceMedicationDto> groupElements = new ArrayList<>();

  public MedicationOnDischargeGroupDto(final TherapySourceGroupEnum groupEnum, final String groupName)
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

  public List<DischargeSourceMedicationDto> getGroupElements()
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

  public void addGroupElement(final DischargeSourceMedicationDto element)
  {
    groupElements.add(element);
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("groupEnum", groupEnum)
        .append("groupName", groupName)
        .append("therapies", groupElements)
    ;
  }
}
