package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class MedicationDataForTherapyDto extends DataTransferObject
{
  private String genericName;
  private String atcGroupCode;
  private String atcGroupName;
  private String customGroupName;
  private Integer customGroupSortOrder;
  private boolean isAntibiotic;

  public String getGenericName()
  {
    return genericName;
  }

  public void setGenericName(final String genericName)
  {
    this.genericName = genericName;
  }

  public String getAtcGroupCode()
  {
    return atcGroupCode;
  }

  public void setAtcGroupCode(final String atcGroupCode)
  {
    this.atcGroupCode = atcGroupCode;
  }

  public String getAtcGroupName()
  {
    return atcGroupName;
  }

  public void setAtcGroupName(final String atcGroupName)
  {
    this.atcGroupName = atcGroupName;
  }

  public String getCustomGroupName()
  {
    return customGroupName;
  }

  public void setCustomGroupName(final String customGroupName)
  {
    this.customGroupName = customGroupName;
  }

  public boolean isAntibiotic()
  {
    return isAntibiotic;
  }

  public void setAntibiotic(final boolean antibiotic)
  {
    isAntibiotic = antibiotic;
  }

  public Integer getCustomGroupSortOrder()
  {
    return customGroupSortOrder;
  }

  public void setCustomGroupSortOrder(final Integer customGroupSortOrder)
  {
    this.customGroupSortOrder = customGroupSortOrder;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("genericName", genericName)
        .append("atcGroupCode", atcGroupCode)
        .append("atcGroupName", atcGroupName)
        .append("customGroupName", customGroupName)
        .append("customGroupSortOrder", customGroupSortOrder)
        .append("isAntibiotic", isAntibiotic)
    ;
  }
}
