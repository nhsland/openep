package com.marand.thinkmed.medications.units.converter.impl;

import java.util.function.Supplier;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitTypeDto;
import com.marand.thinkmed.medications.dto.unit.UnitGroupEnum;
import com.marand.thinkmed.medications.units.converter.UnitsConverter;
import com.marand.thinkmed.medications.units.provider.UnitsProvider;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class UnitsConverterImpl implements UnitsConverter
{
  private UnitsProvider unitsProvider;

  @Autowired
  public void setUnitsProvider(final UnitsProvider unitsProvider)
  {
    this.unitsProvider = unitsProvider;
  }

  @Override
  public double convert(final double value, final @NonNull String from, final @NonNull KnownUnitType to)
  {
    final Opt<MedicationUnitTypeDto> fromType = unitsProvider.findTypeByUnitName(from);
    final Opt<MedicationUnitTypeDto> toType = unitsProvider.findTypeByKnownUnit(to);

    return verifyAndConvert(value, from, fromType, to.name(), toType);
  }

  @Override
  public double convert(final double value, final @NonNull KnownUnitType from, final @NonNull String to)
  {
    final Opt<MedicationUnitTypeDto> fromType = unitsProvider.findTypeByKnownUnit(from);
    final Opt<MedicationUnitTypeDto> toType = unitsProvider.findTypeByUnitName(to);

    return verifyAndConvert(value, from.name(), fromType, to, toType);
  }

  @Override
  public double convert(final double value, final @NonNull String from, final @NonNull String to)
  {
    if (from.equals(to))
    {
      return value;
    }

    final Opt<MedicationUnitTypeDto> fromType = unitsProvider.findTypeByUnitName(from);
    final Opt<MedicationUnitTypeDto> toType = unitsProvider.findTypeByUnitName(to);

    return verifyAndConvert(value, from, fromType, to, toType);
  }

  @Override
  public double convert(final double value, final @NonNull KnownUnitType from, final @NonNull KnownUnitType to)
  {
    if (from == to)
    {
      return value;
    }

    final Opt<MedicationUnitTypeDto> fromType = unitsProvider.findTypeByKnownUnit(from);
    final Opt<MedicationUnitTypeDto> toType = unitsProvider.findTypeByKnownUnit(to);

    return verifyAndConvert(value, from.name(), fromType, to.name(), toType);
  }

  private double verifyAndConvert(
      final double value,
      final String fromUnitName,
      final Opt<MedicationUnitTypeDto> fromOptType,
      final String toUnitName,
      final Opt<MedicationUnitTypeDto> toOptType)
  {
    final MedicationUnitTypeDto fromType = fromOptType.orElseThrow(unitNotFoundException(fromUnitName));
    final MedicationUnitTypeDto toType = toOptType.orElseThrow(unitNotFoundException(toUnitName));
    return fromType.convert(value, toType);
  }

  private Supplier<IllegalStateException> unitNotFoundException(final String unit)
  {
    return () -> new IllegalStateException("Unit " + unit + " not found");
  }

  @Override
  public boolean isConvertible(final @NonNull String unit1, final @NonNull String unit2)
  {
    if (unit1.equals(unit2))
    {
      return true;
    }

    final Opt<MedicationUnitTypeDto> type1 = unitsProvider.findTypeByUnitName(unit1);
    final Opt<MedicationUnitTypeDto> type2 = unitsProvider.findTypeByUnitName(unit2);

    return type1.isPresent() && type2.isPresent() && type1.get().isConvertible(type2.get());
  }

  @Override
  public boolean isConvertible(final @NonNull String unit, final @NonNull KnownUnitType knownUnit)
  {
    final Opt<MedicationUnitTypeDto> type1 = unitsProvider.findTypeByUnitName(unit);
    final Opt<MedicationUnitTypeDto> type2 = unitsProvider.findTypeByKnownUnit(knownUnit);

    return type1.isPresent() && type2.isPresent() && type1.get().isConvertible(type2.get());
  }

  @Override
  public boolean isGroupType(final @NonNull String unit, final @NonNull UnitGroupEnum group)
  {
    return unitsProvider.findTypeByUnitName(unit).map(t -> t.getGroup() == group).orElse(false);
  }

  @Override
  public boolean isKnownUnit(final @NonNull String unit, final @NonNull KnownUnitType knownUnit)
  {
    return unitsProvider.findTypeByUnitName(unit).map(t -> t.getName().equals(knownUnit.name())).orElse(false);
  }
}
