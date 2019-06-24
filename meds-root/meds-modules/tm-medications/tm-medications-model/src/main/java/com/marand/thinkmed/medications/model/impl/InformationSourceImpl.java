package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.marand.thinkmed.medications.dto.InformationSourceGroupEnum;
import com.marand.thinkmed.medications.dto.InformationSourceTypeEnum;
import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Vid Kumše
 */
@Entity
public class InformationSourceImpl extends AbstractCatalogEntity
{
  private InformationSourceGroupEnum informationSourceGroup;
  private InformationSourceTypeEnum informationSourceType;

  @Enumerated(EnumType.STRING)
  public InformationSourceTypeEnum getInformationSourceType()
  {
    return informationSourceType;
  }

  public void setInformationSourceType(final InformationSourceTypeEnum informationSourceType)
  {
    this.informationSourceType = informationSourceType;
  }

  @Enumerated(EnumType.STRING)
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
    super.appendToString(tsb);

    tsb.append("informationSourceGroup", informationSourceGroup);
    tsb.append("informationSourceType", informationSourceType);
  }
}
