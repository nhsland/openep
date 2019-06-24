package com.marand.thinkmed.medications.model.impl.core;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.marand.maf.core.data.entity.EffectiveEntity;
import com.marand.maf.core.time.Intervals;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.joda.time.Interval;

/**
 * @author Mitja Lapajne
 */
@MappedSuperclass
public abstract class AbstractEffectiveEntity extends AbstractPermanentEntity
    implements EffectiveEntity, Comparable<AbstractEffectiveEntity>
{
  private static final long serialVersionUID = -9146105553920654671L;

  private Interval effective;

  @Override
  @Type(type = "com.marand.maf.core.hibernate.type.IntervalType")
  @Columns(columns = {
      @Column(name = "effective_start", nullable = false),
      @Column(name = "effective_end", nullable = false)})
  public Interval getEffective()
  {
    return effective;
  }

  @Override
  public void setEffective(final Interval effective)
  {
    this.effective = effective;
  }

  @Override
  public int compareTo(final AbstractEffectiveEntity entity)
  {
    if (entity == null)
    {
      throw new NullPointerException("entity is null");
    }

    if (this == entity)
    {
      return 0;
    }

    if ((effective != null) && (entity.getEffective() != null))
    {
      // compare ends before starts
      final int result = Intervals.END_START_COMPARATOR.compare(effective, entity.getEffective());
      if (result != 0)
      {
        return result;
      }
    }
    else if (effective != null)
    {
      return 1;
    }
    else if (entity.getEffective() != null)
    {
      return -1;
    }

    return 0;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("effective", effective);
  }
}
