package com.marand.thinkmed.medications.units.dao.hibernate;

import java.util.Map;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitDto;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitTypeDto;
import com.marand.thinkmed.medications.dto.unit.UnitGroupEnum;
import com.marand.thinkmed.medications.units.dao.UnitsDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;

/**
 * @author Nejc Korasa
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/com/marand/thinkmed/medications/units/dao/hibernate/HibernateUnitsDaoTest-context.xml"})
@DbUnitConfiguration(databaseConnection = {"dbUnitDatabaseConnection"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@Transactional
public class HibernateUnitsDaoTest
{
  @Autowired
  private UnitsDao unitsDao;

  @Test
  @DatabaseSetup("HibernateUnitsDaoTest.loadUnits.xml")
  public void testLoadUnits()
  {
    final Map<Long, MedicationUnitDto> longMedicationUnitDtoMap = unitsDao.loadUnits();

    assertEquals(6, longMedicationUnitDtoMap.size());

    assertEquals("custom mg", longMedicationUnitDtoMap.get(1L).getName());
    assertEquals("mg", longMedicationUnitDtoMap.get(1L).getType().getName());
    assertEquals(1, longMedicationUnitDtoMap.get(1L).getType().getFactor().longValue());
    assertEquals(UnitGroupEnum.MASS_UNIT, longMedicationUnitDtoMap.get(1L).getType().getGroup());

    assertEquals("custom ml", longMedicationUnitDtoMap.get(2L).getName());
    assertEquals("ml", longMedicationUnitDtoMap.get(2L).getType().getName());
    assertEquals(2, longMedicationUnitDtoMap.get(2L).getType().getFactor().longValue());
    assertEquals(UnitGroupEnum.LIQUID_UNIT, longMedicationUnitDtoMap.get(2L).getType().getGroup());

    assertEquals("custom l", longMedicationUnitDtoMap.get(3L).getName());
    assertEquals("l", longMedicationUnitDtoMap.get(3L).getType().getName());
    assertEquals(3, longMedicationUnitDtoMap.get(3L).getType().getFactor().longValue());
    assertEquals(UnitGroupEnum.LIQUID_UNIT, longMedicationUnitDtoMap.get(3L).getType().getGroup());

    assertEquals("custom kg", longMedicationUnitDtoMap.get(6L).getName());
    assertEquals("kg", longMedicationUnitDtoMap.get(6L).getType().getName());
    assertEquals(6, longMedicationUnitDtoMap.get(6L).getType().getFactor().longValue());
    assertEquals(UnitGroupEnum.MASS_UNIT, longMedicationUnitDtoMap.get(6L).getType().getGroup());
  }

  @Test
  @DatabaseSetup("HibernateUnitsDaoTest.loadUnits.xml")
  public void testLoadTypes() throws Exception
  {
    final Map<Long, MedicationUnitTypeDto> types = unitsDao.loadTypes();
    assertEquals(6, types.size());
  }
}