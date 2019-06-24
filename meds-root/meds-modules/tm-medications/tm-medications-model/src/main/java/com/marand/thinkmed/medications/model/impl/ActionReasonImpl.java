package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.model.impl.core.AbstractEffectiveCatalogEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author nejck
 */
@Entity
public class ActionReasonImpl extends AbstractEffectiveCatalogEntity
{
  private ActionReasonType reasonType;

  @Enumerated(EnumType.STRING)
  public ActionReasonType getReasonType()
  {
    return reasonType;
  }

  public void setReasonType(final ActionReasonType reasonType)
  {
    this.reasonType = reasonType;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("reasonType", reasonType);
  }
}
