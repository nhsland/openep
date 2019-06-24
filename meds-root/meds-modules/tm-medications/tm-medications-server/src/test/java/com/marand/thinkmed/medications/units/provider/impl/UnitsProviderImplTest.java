package com.marand.thinkmed.medications.units.provider.impl;

import java.util.HashMap;
import java.util.Map;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitDto;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitTypeDto;
import com.marand.thinkmed.medications.dto.unit.UnitGroupEnum;
import com.marand.thinkmed.medications.dto.unit.UnitsHolderDto;
import com.marand.thinkmed.medications.valueholder.UnitsValueHolder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.marand.thinkmed.medications.dto.unit.UnitGroupEnum.MASS_UNIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Nejc Korasa
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class UnitsProviderImplTest
{
  private final UnitsValueHolder unitsValueHolder = mock(UnitsValueHolder.class);

  private final UnitsProviderImpl unitsProvider = new UnitsProviderImpl();

  @Before
  public void mockMedicationsMap()
  {
    unitsProvider.setUnitsValueHolder(unitsValueHolder);

    final Map<Long, MedicationUnitTypeDto> types = new HashMap<>();
    final MedicationUnitTypeDto type1 = new MedicationUnitTypeDto(1L, 1.0, KnownUnitType.L.name(), MASS_UNIT, "type1");
    types.put(1L, type1);
    final MedicationUnitTypeDto type2 = new MedicationUnitTypeDto(2L, 1.0, KnownUnitType.DL.name(), MASS_UNIT, "type2");
    types.put(2L, type2);
    final MedicationUnitTypeDto type3 = new MedicationUnitTypeDto(3L, 1.0, KnownUnitType.MG.name(), UnitGroupEnum.LIQUID_UNIT, "type3");
    types.put(3L, type3);

    final Map<Long, MedicationUnitDto> units = new HashMap<>();
    units.put(1L, new MedicationUnitDto(type1, "unit1", "code1"));
    units.put(2L, new MedicationUnitDto(type1, "unit2", "code2"));
    units.put(3L, new MedicationUnitDto(type2, "unit3", "code3"));
    units.put(4L, new MedicationUnitDto(type2, "unit4", "code4"));
    units.put(5L, new MedicationUnitDto(type3, "unit5", "code5"));

    Mockito
        .when(unitsValueHolder.getValue())
        .thenReturn(new UnitsValueHolder.UnitsHolderDo(units, types));
  }

  @Test
  public void testFindByName()
  {
    final MedicationUnitTypeDto type = unitsProvider.findTypeByUnitName("unit1").get();
    assertEquals(MASS_UNIT, type.getGroup());
  }

  public void testFindByNameNoResult()
  {
    final Opt<MedicationUnitTypeDto> type = unitsProvider.findTypeByUnitName("different name");
    assertTrue(type.isAbsent());
  }

  @Test
  public void testFindByKnownUnit()
  {
    final MedicationUnitTypeDto type = unitsProvider.findTypeByKnownUnit(KnownUnitType.L).get();
    assertEquals("type1", type.getDisplayName());
  }

  public void testFindByKnownUnitNoResult()
  {
    final Opt<MedicationUnitTypeDto> type = unitsProvider.findTypeByKnownUnit(KnownUnitType.MIN);
    assertTrue(type.isAbsent());
  }

  @Test
  public void testGetDisplayName()
  {
    final String displayName = unitsProvider.getDisplayName(KnownUnitType.L);
    assertEquals("type1", displayName);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetDisplayNameNoResult()
  {
    unitsProvider.getDisplayName(KnownUnitType.MIN);
  }

  @Test
  public void testGetUnitsHolder()
  {
    final UnitsHolderDto unitsHolder = unitsProvider.getUnitsHolder();
    final Map<String, Long> units = unitsHolder.getUnitsWithType();
    assertEquals(5, units.size());
    assertTrue(units.containsKey("unit1"));
    assertTrue(units.containsKey("unit2"));
    assertTrue(units.containsKey("unit3"));
    assertTrue(units.containsKey("unit4"));
    assertTrue(units.containsKey("unit5"));

    assertEquals(MASS_UNIT, unitsHolder.getTypes().get(units.get("unit1")).getGroup());
    assertEquals(KnownUnitType.L.name(), unitsHolder.getTypes().get(units.get("unit1")).getName());
  }
}