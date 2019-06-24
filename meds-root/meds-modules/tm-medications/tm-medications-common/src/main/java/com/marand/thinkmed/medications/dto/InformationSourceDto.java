package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Vid Kumse
 */
public class InformationSourceDto extends DataTransferObject
{
  private Long id;
  private String name;
  private InformationSourceTypeEnum informationSourceType;
  private InformationSourceGroupEnum informationSourceGroup;

  public Long getId()
  {
    return id;
  }

  public void setId(final Long id)
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

  public InformationSourceTypeEnum getInformationSourceType()
  {
    return informationSourceType;
  }

  public void setInformationSourceType(final InformationSourceTypeEnum informationSourceType)
  {
    this.informationSourceType = informationSourceType;
  }

  public InformationSourceGroupEnum getInformationSourceGroup()
  {
    return informationSourceGroup;
  }

  public void setInformationSourceGroup(final InformationSourceGroupEnum informationSourceGroup)
  {
    this.informationSourceGroup = informationSourceGroup;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("id", id)
        .append("name", name)
        .append("informationSourceType", informationSourceType)
        .append("informationSourceGroup", informationSourceGroup);
  }
}
