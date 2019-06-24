package com.marand.thinkmed.medications.dao.openehr;

import java.util.HashMap;
import java.util.Map;

import com.marand.thinkehr.mapping.EhrMapper;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
@SuppressWarnings("rawtypes")
public class EhrMappersHolder
{
  private final Map<Class<?>, EhrMapper<?>> ehrMappers = new HashMap<>();

  /**
   * Creation of EhrMapper is a heavy operation, it should be only done once per class
   */
  public EhrMapper getEhrMapper(final Class<?> objectClass)
  {
    return ehrMappers.computeIfAbsent(objectClass, c -> new EhrMapper<>(objectClass));
  }
}
