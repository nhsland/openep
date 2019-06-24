package com.marand.thinkmed.medications.units.dao.hibernate;

import java.util.HashMap;
import java.util.Map;

import com.marand.maf.core.data.IdentityDto;
import com.marand.maf.core.hibernate.query.Alias;
import com.marand.maf.core.hibernate.query.Hql;
import com.marand.maf.core.resultrow.ProcessingException;
import com.marand.maf.core.resultrow.TupleProcessor;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitDto;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitTypeDto;
import com.marand.thinkmed.medications.dto.unit.UnitGroupEnum;
import com.marand.thinkmed.medications.model.impl.MedicationUnitImpl;
import com.marand.thinkmed.medications.model.impl.MedicationUnitTypeImpl;
import com.marand.thinkmed.medications.units.dao.UnitsDao;
import com.marand.thinkmed.medications.units.mapper.MedicationUnitTypeDtoMapper;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.marand.maf.core.hibernate.query.Alias.permanentEntities;
import static java.util.stream.Collectors.toMap;

/**
 * @author Nejc Korasa
 */

@Component
public class HibernateUnitsDao implements UnitsDao
{
  private static final Alias.Permanent<MedicationUnitTypeImpl> medicationUnitType = Alias.forPermanentEntity(MedicationUnitTypeImpl.class);
  private static final Alias.Permanent<MedicationUnitImpl> medicationUnit = Alias.forPermanentEntity(MedicationUnitImpl.class);

  private final MedicationUnitTypeDtoMapper medicationUnitTypeDtoMapper;

  private final SessionFactory sessionFactory;

  @Autowired
  public HibernateUnitsDao(
      final MedicationUnitTypeDtoMapper medicationUnitTypeDtoMapper,
      final SessionFactory sessionFactory)
  {
    this.sessionFactory = sessionFactory;
    this.medicationUnitTypeDtoMapper = medicationUnitTypeDtoMapper;
  }

  @Override
  public Map<Long, MedicationUnitDto> loadUnits()
  {
    final Map<Long, MedicationUnitDto> unitsMap = new HashMap<>();

    new Hql()
        .select(
            medicationUnit.id(),
            medicationUnit.get("name"),
            medicationUnit.get("code"),
            medicationUnitType.id(),
            medicationUnitType.get("code"),
            medicationUnitType.get("factor"),
            medicationUnitType.get("unitGroup"),
            medicationUnitType.get("name")
        )
        .from(medicationUnit.leftOuterJoin("type").as(medicationUnitType))
        .where(permanentEntities(medicationUnit, medicationUnitType).notDeleted())
        .buildQuery(sessionFactory.getCurrentSession(), Object[].class)
        .list(
            new TupleProcessor<Void>()
            {
              @Override
              protected Void process(final boolean hasNextTuple) throws ProcessingException
              {
                final Long id = nextLong();
                final String name = nextString();
                final String code = nextString();
                final Long typeId = nextLong();
                final String typeName = nextString();
                final Double factor = next(Double.class);
                final UnitGroupEnum group = next(UnitGroupEnum.class);
                final String displayName = nextString();

                MedicationUnitTypeDto type = null;
                if (typeId != null)
                {
                  type = new MedicationUnitTypeDto(typeId, factor, typeName, group, displayName);
                }

                final MedicationUnitDto unitDto = new MedicationUnitDto(type, name, code);
                unitsMap.put(id, unitDto);
                return null;
              }
            });

    return unitsMap;
  }

  @Override
  public Map<Long, MedicationUnitTypeDto> loadTypes()
  {
    return new Hql()
        .select(medicationUnitType)
        .from(medicationUnitType)
        .where(medicationUnitType.notDeleted())
        .buildQuery(sessionFactory.getCurrentSession(), MedicationUnitTypeImpl.class)
        .list()
        .stream()
        .map(medicationUnitTypeDtoMapper::map)
        .collect(toMap(IdentityDto::getId, o -> o));
  }
}
