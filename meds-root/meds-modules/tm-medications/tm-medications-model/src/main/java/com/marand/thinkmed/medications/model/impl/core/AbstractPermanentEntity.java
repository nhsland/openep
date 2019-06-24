package com.marand.thinkmed.medications.model.impl.core;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.marand.maf.core.data.AuditInfo;
import com.marand.maf.core.data.entity.PermanentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@MappedSuperclass
public abstract class AbstractPermanentEntity extends AbstractEntity implements PermanentEntity
{
  private int version;
  private boolean deleted;
  private AuditInfo auditInfo;

  public AbstractPermanentEntity()
  {
  }

  @Override
  @Version
  public int getVersion()
  {
    return this.version;
  }

  @Override
  public void setVersion(final int version)
  {
    this.version = version;
  }

  @Override
  @Transient
  public boolean isDeleted()
  {
    return this.deleted;
  }

  @Override
  public void setDeleted(final boolean deleted)
  {
    this.deleted = deleted;
  }

  @Override
  public Long getDeletedId()
  {
    return this.deleted ? this.getId() : null;
  }

  @Override
  public void setDeletedId(final Long deletedId)
  {
    this.deleted = deletedId != null;
  }

  @Override
  public void delete()
  {
    this.setDeleted(true);
  }

  @Override
  public AuditInfo getAuditInfo()
  {
    return this.auditInfo;
  }

  @Override
  public void setAuditInfo(final AuditInfo auditInfo)
  {
    this.auditInfo = auditInfo;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("version", version);
    tsb.append("deleted", version);
    tsb.append("auditInfo", auditInfo);
  }
}