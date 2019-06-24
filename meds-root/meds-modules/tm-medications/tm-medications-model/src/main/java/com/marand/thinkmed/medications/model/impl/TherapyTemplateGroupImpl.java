package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
public class TherapyTemplateGroupImpl extends AbstractCatalogEntity
{
  private TherapyTemplateModeEnum templateMode;

  @Enumerated(EnumType.STRING)
  public TherapyTemplateModeEnum getTemplateMode()
  {
    return templateMode;
  }

  public void setTemplateMode(final TherapyTemplateModeEnum templateMode)
  {
    this.templateMode = templateMode;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("templateMode", templateMode);
  }
}
