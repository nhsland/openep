package com.marand.thinkmed.medications.model.impl.core;

import java.io.Serializable;

import com.marand.maf.core.data.entity.Entity;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * @author Mitja Lapajne
 */
public class IdGenerator extends SequenceStyleGenerator
{
  @Override
  public Serializable generate(final SharedSessionContractImplementor session, final Object object) throws HibernateException
  {
    final Entity entity = (Entity)object;
    if (entity.getId() > 0)
    {
      return entity.getId();
    }
    return super.generate(session, object);
  }
}
