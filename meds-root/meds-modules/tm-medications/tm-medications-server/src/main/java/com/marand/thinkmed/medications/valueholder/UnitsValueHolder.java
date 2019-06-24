package com.marand.thinkmed.medications.valueholder;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.marand.maf.core.valueholder.ValueHolder;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitDto;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitTypeDto;
import com.marand.thinkmed.medications.units.dao.UnitsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korsasa
 */

@Component
public class UnitsValueHolder extends ValueHolder<UnitsValueHolder.UnitsHolderDo>
{
  private static final Logger LOG = LoggerFactory.getLogger(UnitsValueHolder.class);

  private UnitsDao unitsDao;

  @Autowired
  public void setUnitsDao(final UnitsDao unitsDao)
  {
    this.unitsDao = unitsDao;
  }

  @Override
  protected String getKey()
  {
    return "MEDICATIONS"; // uses same key as MedicationsValueHolder for easier use
  }

  public Map<Long, MedicationUnitDto> getUnits()
  {
    return getValue().getUnits();
  }

  public Map<Long, MedicationUnitTypeDto> getUnitTypes()
  {
    return getValue().getUnitTypes();
  }

  @Override
  protected UnitsHolderDo loadValue()
  {
    final Stopwatch sw = Stopwatch.createStarted();

    final Map<Long, MedicationUnitDto> unitsMap = unitsDao.loadUnits();
    final Map<Long, MedicationUnitTypeDto> unitTypesMap = unitsDao.loadTypes();

    LOG.info("Units value holder loaded - took: " + sw.elapsed(TimeUnit.MILLISECONDS) + " ms");

    return new UnitsHolderDo(unitsMap, unitTypesMap);
  }

  @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
  public static class UnitsHolderDo
  {
    private final Map<Long, MedicationUnitDto> units;
    private final Map<Long, MedicationUnitTypeDto> unitTypes;

    public UnitsHolderDo(final Map<Long, MedicationUnitDto> units, final Map<Long, MedicationUnitTypeDto> unitTypes)
    {
      this.units = Collections.unmodifiableMap(units);
      this.unitTypes = Collections.unmodifiableMap(unitTypes);
    }

    public Map<Long, MedicationUnitDto> getUnits()
    {
      return units;
    }

    public Map<Long, MedicationUnitTypeDto> getUnitTypes()
    {
      return unitTypes;
    }
  }
}
