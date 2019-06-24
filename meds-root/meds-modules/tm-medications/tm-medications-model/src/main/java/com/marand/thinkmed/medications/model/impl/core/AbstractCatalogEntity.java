package com.marand.thinkmed.medications.model.impl.core;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.marand.maf.core.data.entity.CatalogEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@MappedSuperclass
public abstract class AbstractCatalogEntity extends AbstractPermanentEntity implements CatalogEntity
{
  private static final long serialVersionUID = -320030773964952619L;
  private String code;
  private String name;
  private String description;

  public AbstractCatalogEntity()
  {
  }

  @Override
  public String getCode()
  {
    return this.code;
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
    return this.name;
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
    return this.description;
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
    tsb.append("code", this.code).append("name", this.name).append("description", this.description);
  }
}