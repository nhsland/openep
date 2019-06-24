package com.marand.thinkmed.medications.api.internal.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class MedicationSimpleDto extends DataTransferObject implements JsonSerializable
{
  private long id;
  private String name;
  private String genericName;
  private boolean active;
  private boolean outpatientMedication;
  private boolean inpatientMedication;
  private String inpatientAdditionalInfo;
  private String outpatientAdditionalInfo;
  private String tradeFamily;

  public long getId()
  {
    return id;
  }

  public void setId(final long id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public String getGenericName()
  {
    return genericName;
  }

  public void setGenericName(final String genericName)
  {
    this.genericName = genericName;
  }

  public boolean isActive()
  {
    return active;
  }

  public void setActive(final boolean active)
  {
    this.active = active;
  }

  public boolean isOutpatientMedication()
  {
    return outpatientMedication;
  }

  public void setOutpatientMedication(final boolean outpatientMedication)
  {
    this.outpatientMedication = outpatientMedication;
  }

  public boolean isInpatientMedication()
  {
    return inpatientMedication;
  }

  public void setInpatientMedication(final boolean inpatientMedication)
  {
    this.inpatientMedication = inpatientMedication;
  }

  public String getInpatientAdditionalInfo()
  {
    return inpatientAdditionalInfo;
  }

  public void setInpatientAdditionalInfo(final String inpatientAdditionalInfo)
  {
    this.inpatientAdditionalInfo = inpatientAdditionalInfo;
  }

  public String getOutpatientAdditionalInfo()
  {
    return outpatientAdditionalInfo;
  }

  public void setOutpatientAdditionalInfo(final String outpatientAdditionalInfo)
  {
    this.outpatientAdditionalInfo = outpatientAdditionalInfo;
  }

  public String getTradeFamily()
  {
    return tradeFamily;
  }

  public void setTradeFamily(final String tradeFamily)
  {
    this.tradeFamily = tradeFamily;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("id", id)
        .append("name", name)
        .append("genericName", genericName)
        .append("active", active)
        .append("inpatientMedication", inpatientMedication)
        .append("outpatientMedication", outpatientMedication)
        .append("inpatientAdditionalInfo", inpatientAdditionalInfo)
        .append("outpatientAdditionalInfo", outpatientAdditionalInfo)
        .append("tradeFamily", tradeFamily)
    ;
  }
}
