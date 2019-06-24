package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;

import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
public class DispenseSourceImpl extends AbstractCatalogEntity
{
  private Boolean defaultSource;

  public Boolean getDefaultSource()
  {
    return defaultSource;
  }

  public void setDefaultSource(final Boolean defaultSource)
  {
    this.defaultSource = defaultSource;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("defaultSource", defaultSource);
  }
}
