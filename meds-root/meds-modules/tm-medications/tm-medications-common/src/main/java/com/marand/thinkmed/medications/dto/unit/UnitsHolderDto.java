package com.marand.thinkmed.medications.dto.unit;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.marand.thinkmed.api.core.JsonSerializable;
import lombok.NonNull;

/**
 * @author Nejc Korasa
 */

public class UnitsHolderDto implements JsonSerializable
{
  /**
   * Map that has unit name as key and unit type id as value
   */
  private final Map<String, Long> unitsWithType;

  /**
   * Map that has unit type id as key and {@link MedicationUnitTypeDto} as value
   */
  private final Map<Long, MedicationUnitTypeDto> types;

  /**
   * List of all units
   */
  private final List<String> allUnits;

  public UnitsHolderDto(
      final @NonNull List<String> allUnits,
      final @NonNull Map<String, Long> unitsWithType,
      final @NonNull Map<Long, MedicationUnitTypeDto> types)
  {
    this.unitsWithType = unitsWithType;
    this.allUnits = allUnits;
    this.types = types;
  }

  public Map<String, Long> getUnitsWithType()
  {
    return Collections.unmodifiableMap(unitsWithType);
  }

  public Map<Long, MedicationUnitTypeDto> getTypes()
  {
    return Collections.unmodifiableMap(types);
  }

  public List<String> getAllUnits()
  {
    return Collections.unmodifiableList(allUnits);
  }

  @Override
  public String toString()
  {
    return String.format(
        "UnitsHolderDto{unitsWithType=%s, types=%s, allUnits=%s}",
        unitsWithType,
        types,
        allUnits);
  }
}
