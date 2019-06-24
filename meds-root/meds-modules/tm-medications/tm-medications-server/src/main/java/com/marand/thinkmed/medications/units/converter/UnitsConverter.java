package com.marand.thinkmed.medications.units.converter;

import lombok.NonNull;

import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import com.marand.thinkmed.medications.dto.unit.UnitGroupEnum;

/**
 * @author Nejc Korasa
 */
public interface UnitsConverter
{

  /**
   * Convert {@param value} represented in {@param from} unit to {@param to} known unit type
   *
   * @throws IllegalStateException if units cannot be converted, verify conversion using {@link UnitsConverter#isConvertible} first.
   */
  double convert(double value, @NonNull String from, @NonNull KnownUnitType to);

  /**
   * Convert {@param value} represented in {@param from} known unit type to {@param to} unit
   *
   * @throws IllegalStateException if units cannot be converted, verify conversion using {@link UnitsConverter#isConvertible} first.
   */
  double convert(double value, @NonNull KnownUnitType from, @NonNull String to);

  /**
   * Convert {@param value} represented in {@param from} unit to {@param to} unit
   *
   * @throws IllegalStateException if units cannot be converted, verify conversion using {@link UnitsConverter#isConvertible} first.
   */
  double convert(double value, @NonNull String from, @NonNull String to);

  /**
   * Convert {@param value} represented in {@param from} known unit type to {@param to} known unit type
   *
   * @throws IllegalStateException if units cannot be converted, verify conversion using {@link UnitsConverter#isConvertible} first.
   */
  double convert(double value, @NonNull KnownUnitType from, @NonNull KnownUnitType to);

  /**
   * Checks if unit {@param unit1} and unit {@param unit2} are convertible
   */
  boolean isConvertible(@NonNull String unit1, @NonNull String unit2);

  /**
   * Checks if unit {@param unit} and known unit type {@param knownType} are convertible
   */
  boolean isConvertible(@NonNull String unit, @NonNull KnownUnitType knownUnit);

  /**
   * Checks if unit {@param unit} is of group type {@param group}
   */
  boolean isGroupType(@NonNull String unit, @NonNull UnitGroupEnum group);

  /**
   * Checks if unit {@param unit} is of known unit type {@param knownUnit}
   */
  boolean isKnownUnit(@NonNull String unit, @NonNull KnownUnitType knownUnit);
}
