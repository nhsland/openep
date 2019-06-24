package com.marand.thinkmed.medications.model.impl.core;

import javax.persistence.MappedSuperclass;

import com.marand.maf.core.data.entity.TemporalEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@MappedSuperclass
public abstract class AbstractTemporalEntity extends AbstractEffectiveEntity implements TemporalEntity
{
  private static final long serialVersionUID = -4633898131973385922L;

  private Long predecessorId;
  private Long rootId;

  @Override
  public Long getPredecessorId()
  {
    return predecessorId;
  }

  @Override
  public void setPredecessorId(final Long predecessorId)
  {
    this.predecessorId = predecessorId;
  }

  @Override
  public Long getRootId()
  {
    return rootId;
  }

  @Override
  public void setRootId(final Long rootId)
  {
    this.rootId = rootId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
      .append("rootId", rootId)
      .append("predecessorId", predecessorId);
  }
}
