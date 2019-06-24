package com.marand.thinkmed.medications.model.impl.core;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.marand.maf.core.data.entity.EffectiveCatalogEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@MappedSuperclass
public abstract class AbstractEffectiveCatalogEntity extends AbstractEffectiveEntity implements EffectiveCatalogEntity
{
  private static final long serialVersionUID = -807700704161276022L;

  private String code;
  private String name;
  private String description;

  @Override
  public String getCode()
  {
    return code;
  }

  @Override
  public void setCode(final String code)
  {
    this.code = code;
  }

  @Override
  @Column(length = 1024)
  public String getName()
  {
    return name;
  }

  @Override
  public void setName(final String name)
  {
    this.name = name;
  }

  @Override
  @Column(length = 1024)
  public String getDescription()
  {
    return description;
  }

  @Override
  public void setDescription(final String description)
  {
    this.description = description;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("code", code)
        .append("name", name)
        .append("description", description);
  }
}
