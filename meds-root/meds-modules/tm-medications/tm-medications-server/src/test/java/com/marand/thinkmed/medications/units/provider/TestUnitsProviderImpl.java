package com.marand.thinkmed.medications.units.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.marand.maf.core.Opt;
import com.marand.maf.core.data.IdentityDto;
import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitTypeDto;
import com.marand.thinkmed.medications.dto.unit.UnitsHolderDto;
import edu.emory.mathcs.backport.java.util.Collections;
import lombok.NonNull;

import static com.marand.thinkmed.medications.dto.unit.UnitGroupEnum.LIQUID_UNIT;
import static com.marand.thinkmed.medications.dto.unit.UnitGroupEnum.MASS_UNIT;
import static com.marand.thinkmed.medications.dto.unit.UnitGroupEnum.TIME_UNIT;

/**
 * @author Nejc Korasa
 */

public class TestUnitsProviderImpl implements UnitsProvider
{
  private final Map<String, MedicationUnitTypeDto> unitsMap = buildUnitsMap();

  private Map<String, MedicationUnitTypeDto> buildUnitsMap()
  {
    final Map<String, MedicationUnitTypeDto> map = new HashMap<>();

    map.put("l", new MedicationUnitTypeDto(1L, 1.0, "L", LIQUID_UNIT, "l"));
    map.put("litre", new MedicationUnitTypeDto(1L, 1.0, "L", LIQUID_UNIT, "l"));
    map.put("dl", new MedicationUnitTypeDto(2L, 0.1, "DL", LIQUID_UNIT,"dl"));
    map.put("cl", new MedicationUnitTypeDto(3L, 0.01, "CL", LIQUID_UNIT,"cl"));
    map.put("ml", new MedicationUnitTypeDto(4L, 0.001, "ML", LIQUID_UNIT,"ml"));
    map.put("mL", new MedicationUnitTypeDto(4L, 0.001, "ML", LIQUID_UNIT,"ml"));
    map.put("µl", new MedicationUnitTypeDto(5L, 0.000001, "MICRO_L", LIQUID_UNIT,"µl"));
    map.put("microlitre", new MedicationUnitTypeDto(5L, 0.000001, "MICRO_L", LIQUID_UNIT,"µl"));

    map.put("g", new MedicationUnitTypeDto(6L, 1.0, "G", MASS_UNIT,"g"));
    map.put("gram", new MedicationUnitTypeDto(6L, 1.0, "G", MASS_UNIT,"gram"));
    map.put("kg", new MedicationUnitTypeDto(7L, 1000.0, "KG", MASS_UNIT,"kg"));
    map.put("mg", new MedicationUnitTypeDto(8L, 0.001, "MG", MASS_UNIT,"mg"));
    map.put("мг", new MedicationUnitTypeDto(9L, 0.001, "MICRO_G", MASS_UNIT,"мг"));
    map.put("µg", new MedicationUnitTypeDto(9L, 0.000001, "MICRO_G", MASS_UNIT,"µg"));
    map.put("microgram", new MedicationUnitTypeDto(9L, 0.000001, "MICRO_G", MASS_UNIT,"µg"));
    map.put("ng", new MedicationUnitTypeDto(10L, 0.000000001, "NANO_G", MASS_UNIT,"ng"));
    map.put("nanogram", new MedicationUnitTypeDto(10L, 0.000000001, "NANO_G", MASS_UNIT,"ng"));

    map.put("d", new MedicationUnitTypeDto(11L, 86400.0, "D", TIME_UNIT,"d"));
    map.put("h", new MedicationUnitTypeDto(12L, 3600.0, "H", TIME_UNIT,"h"));
    map.put("min", new MedicationUnitTypeDto(13L, 60.0, "MIN", TIME_UNIT,"min"));
    map.put("m", new MedicationUnitTypeDto(13L, 60.0, "MIN", TIME_UNIT,"min"));
    map.put("s", new MedicationUnitTypeDto(14L, 1.0, "S", TIME_UNIT, "s"));

    map.put("l/min", new MedicationUnitTypeDto(15L, null, "L_MIN", TIME_UNIT, "l/min"));

    return map;
  }

  @Override
  public Opt<MedicationUnitTypeDto> findTypeByUnitName(final @NonNull String unitName)
  {
    return Opt.from(unitsMap.keySet().stream().filter(k -> k.equals(unitName)).findFirst().map(unitsMap::get));
  }

  @Override
  public Opt<KnownUnitType> findKnownUnitByDisplayName(final @NonNull String displayName)
  {
    return Opt.from(
         unitsMap.values()
            .stream()
            .filter(u -> u.getDisplayName().equals(displayName))
            .map(MedicationUnitTypeDto::getName)
            .map(KnownUnitType::valueOf)
            .findFirst());
  }

  @Override
  public Opt<MedicationUnitTypeDto> findTypeByKnownUnit(final @NonNull KnownUnitType knownUnit)
  {
    return Opt.from(unitsMap.values().stream().filter(v -> v.getName().equals(knownUnit.name())).findFirst());
  }

  @Override
  public String getDisplayName(final @NonNull KnownUnitType knownUnit)
  {
    return unitsMap.values()
        .stream()
        .filter(v -> v.getName().equals(knownUnit.name()))
        .findFirst()
        .map(MedicationUnitTypeDto::getDisplayName)
        .orElse(null);
  }

  @Override
  public UnitsHolderDto getUnitsHolder()
  {
    final Map<String, Long> units = unitsMap.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getId()));

    final Map<Long, MedicationUnitTypeDto> types = unitsMap.values()
        .stream()
        .collect(Collectors.toMap(IdentityDto::getId, u -> u));

    //noinspection unchecked
    return new UnitsHolderDto(Collections.emptyList(), units, types);
  }
}
