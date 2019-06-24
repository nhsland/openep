package com.marand.thinkmed.medications.model.impl.core;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.marand.maf.core.data.entity.Entity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Mitja Lapajne
 */
@MappedSuperclass
public abstract class AbstractEntity implements Entity
{
  private long id;

  public AbstractEntity()
  {
  }

  @Override
  @Id
  @GenericGenerator(
      name = "hibseq",
      strategy = "com.marand.thinkmed.medications.model.impl.core.IdGenerator",
      parameters = {
          @Parameter(
              name = "increment_size",
              value = "50"
          ), @Parameter(
          name = "initial_value",
          value = "10000500"
      )})
  @GeneratedValue(generator = "hibseq")
  public long getId()
  {
    return id;
  }

  @Override
  public void setId(final long id)
  {
    this.id = id;
  }

  @Override
  @Transient
  public Class getEntityType()
  {
    return Hibernate.getClass(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transient
  public <T extends Entity> InstanceWrapper<T> getInstanceWrapper(final Class<T> entityType)
  {
    return new NonNullInstanceWrapper(entityType.cast(this));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Entity> InstanceWrapper<T> getInstanceOrNullWrapper(final Class<T> entityType)
  {
    return entityType.isInstance(this) ? getInstanceWrapper(entityType) : new InstanceWrapper(null);
  }

  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("id", id);
  }
}