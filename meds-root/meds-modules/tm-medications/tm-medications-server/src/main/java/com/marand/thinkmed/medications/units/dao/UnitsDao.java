package com.marand.thinkmed.medications.units.dao;

import java.util.Map;

import com.marand.thinkmed.medications.dto.unit.MedicationUnitDto;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitTypeDto;

/**
 * @author Nejc Korasa
 */

public interface UnitsDao
{
  Map<Long, MedicationUnitDto> loadUnits();

  Map<Long, MedicationUnitTypeDto> loadTypes();
}
