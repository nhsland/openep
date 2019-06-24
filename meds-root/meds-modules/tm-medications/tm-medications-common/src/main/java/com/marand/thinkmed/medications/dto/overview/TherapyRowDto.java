package com.marand.thinkmed.medications.dto.overview;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.medications.PrescriptionGroupEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningSimpleDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyRowDto extends TherapyDayDto implements JsonSerializable
{
  private String therapyId;
  private String atcGroupName;
  private String atcGroupCode;
  private String customGroup;
  private Integer customGroupSortOrder;
  private PrescriptionGroupEnum prescriptionGroup;
  private List<AdministrationDto> administrations = new ArrayList<>();
  private List<AdditionalWarningSimpleDto> additionalWarnings = new ArrayList<>();

  public String getTherapyId()
  {
    return therapyId;
  }

  public void setTherapyId(final String therapyId)
  {
    this.therapyId = therapyId;
  }

  public String getAtcGroupName()
  {
    return atcGroupName;
  }

  public void setAtcGroupName(final String atcGroupName)
  {
    this.atcGroupName = atcGroupName;
  }

  public String getAtcGroupCode()
  {
    return atcGroupCode;
  }

  public void setAtcGroupCode(final String atcGroupCode)
  {
    this.atcGroupCode = atcGroupCode;
  }

  public String getCustomGroup()
  {
    return customGroup;
  }

  public void setCustomGroup(final String customGroup)
  {
    this.customGroup = customGroup;
  }

  public Integer getCustomGroupSortOrder()
  {
    return customGroupSortOrder;
  }

  public void setCustomGroupSortOrder(final Integer customGroupSortOrder)
  {
    this.customGroupSortOrder = customGroupSortOrder;
  }

  public PrescriptionGroupEnum getPrescriptionGroup()
  {
    return prescriptionGroup;
  }

  public void setPrescriptionGroup(final PrescriptionGroupEnum prescriptionGroup)
  {
    this.prescriptionGroup = prescriptionGroup;
  }

  public List<AdministrationDto> getAdministrations()
  {
    return administrations;
  }

  public void setAdministrations(final List<AdministrationDto> administrations)
  {
    this.administrations = administrations;
  }

  public List<AdditionalWarningSimpleDto> getAdditionalWarnings()
  {
    return additionalWarnings;
  }

  public void setAdditionalWarnings(final List<AdditionalWarningSimpleDto> additionalWarnings)
  {
    this.additionalWarnings = additionalWarnings;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("therapyId", therapyId)
        .append("atcGroupName", atcGroupName)
        .append("atcGroupCode", atcGroupCode)
        .append("customGroup", customGroup)
        .append("customGroupSortOrder", customGroupSortOrder)
        .append("prescriptionGroup", prescriptionGroup)
        .append("administrations", administrations)
        .append("additionalWarnings", additionalWarnings);
  }
}
