package com.marand.thinkmed.medications.dao.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.google.common.collect.Sets;
import com.marand.maf.core.Pair;
import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.api.internal.dto.DoseFormType;
import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.dto.DispenseSourceDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDocumentType;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitDto;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitTypeDto;
import com.marand.thinkmed.medications.dto.unit.UnitGroupEnum;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.units.dao.UnitsDao;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Mitja Lapajne
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings({"ConstantConditions", "OptionalGetWithoutIsPresent"})
@ContextConfiguration({"/com/marand/thinkmed/medications/dao/hibernate/HibernateMedicationsDaoTest-context.xml"})
@DbUnitConfiguration(databaseConnection = {"dbUnitDatabaseConnection"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@DatabaseSetup("HibernateMedicationsDaoTest.xml")
@Transactional
public class HibernateMedicationsDaoTest
{
  @Autowired
  private HibernateMedicationsDao medicationsDao;

  @Autowired
  private UnitsDao unitsDao;

  @Before
  public void mockUnits()
  {
    final MedsProperties props = new MedsProperties();
    props.setOrganizationCode("org1");
    medicationsDao.setMedsProperties(props);

    final Map<Long, MedicationUnitDto> units = new HashMap<>();
    final MedicationUnitTypeDto type1 = new MedicationUnitTypeDto(1L, 1.0, "mg", UnitGroupEnum.MASS_UNIT, "mg");
    final MedicationUnitTypeDto type2 = new MedicationUnitTypeDto(2L, 1.0, "ml", UnitGroupEnum.LIQUID_UNIT, "ml");
    final MedicationUnitTypeDto type3 = new MedicationUnitTypeDto(3L, 10.0, "cl", UnitGroupEnum.LIQUID_UNIT, "cl");

    units.put(1L, new MedicationUnitDto(type1, "mg", "code"));
    units.put(2L, new MedicationUnitDto(type2, "ml", "code"));
    units.put(3L, new MedicationUnitDto(type3, "cl", "code"));

    Mockito.when(unitsDao.loadUnits()).thenReturn(units);
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetMedicationsExternalId.xml")
  public void testGetMedicationExternalId()
  {
    final Map<Long, String> fdbExternalId = medicationsDao.getMedicationsExternalIds(
        "FDB",
        Sets.newHashSet(1L));

    assertEquals(1, fdbExternalId.size());
    assertEquals("11111", fdbExternalId.get(1L));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetMedicationsExternalId.xml")
  public void testGetMedicationExternalIdEmptyList()
  {
    final Map<Long, String> fdbExternalId = medicationsDao.getMedicationsExternalIds(
        "FDB",
        Collections.emptySet());

    assertEquals(0, fdbExternalId.size());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetMedicationsExternalId.xml")
  public void testGetMedicationExternalIdTwoMedications()
  {
    final Map<Long, String> fdbExternalId = medicationsDao.getMedicationsExternalIds(
        "FDB",
        Sets.newHashSet(2L));

    assertEquals("22222", fdbExternalId.get(2L));
  }

  @Test
  public void testGetMedicationExternalValuesForRoutes()
  {
    final Set<String> routesSet = new HashSet<>();
    routesSet.add("R001");
    routesSet.add("R002");

    final Map<String, String> routesMap =
        medicationsDao.getMedicationExternalValues("FDB", MedicationsExternalValueType.ROUTE, routesSet);
    assertEquals("ExternalRoute1", routesMap.get("R001"));
    assertEquals("ExternalRoute2", routesMap.get("R002"));
  }

  @Test
  public void testGetMedicationExternalValuesForUnits()
  {
    final Set<String> unitsSet = new HashSet<>();
    unitsSet.add("tableta");
    unitsSet.add("viala");

    final Map<String, String> unitsMap =
        medicationsDao.getMedicationExternalValues("FDB", MedicationsExternalValueType.UNIT, unitsSet);
    assertEquals("tbl", unitsMap.get("tableta"));
    assertEquals("vial", unitsMap.get("viala"));
  }

  @Test
  public void testGetMedicationExternalValuesForUnitsWithEmptySet()
  {
    final Map<String, String> unitsMap =
        medicationsDao.getMedicationExternalValues("FDB", MedicationsExternalValueType.UNIT, new HashSet<>());
    assertTrue(unitsMap.isEmpty());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetDoseForms.xml")
  public void testGetDoseForms()
  {
    final List<DoseFormDto> doseForms = medicationsDao.getDoseForms();
    assertEquals(4L, (long)doseForms.size());

    assertEquals("1", doseForms.get(0).getCode());
    assertEquals("Pill", doseForms.get(0).getName());
    assertEquals(DoseFormType.TBL, doseForms.get(0).getDoseFormType());
    assertEquals(MedicationOrderFormType.SIMPLE, doseForms.get(0).getMedicationOrderFormType());

    assertEquals("2", doseForms.get(1).getCode());
    assertEquals("Suppository", doseForms.get(1).getName());
    assertEquals(DoseFormType.SUPPOSITORY, doseForms.get(1).getDoseFormType());
    assertEquals(MedicationOrderFormType.SIMPLE, doseForms.get(1).getMedicationOrderFormType());

    assertEquals("3", doseForms.get(2).getCode());
    assertEquals("Syrup", doseForms.get(2).getName());
    assertNull(doseForms.get(2).getDoseFormType());
    assertEquals(MedicationOrderFormType.SIMPLE, doseForms.get(2).getMedicationOrderFormType());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetRoutes.xml")
  public void testGetRoutes()
  {
    final List<MedicationRouteDto> routes = medicationsDao.getRoutes();
    assertEquals(4L, (long)routes.size());

    assertEquals("1", routes.get(0).getCode());
    assertEquals("po", routes.get(0).getName());
    assertNull(routes.get(0).getType());

    assertEquals("2", routes.get(1).getCode());

    assertEquals("4", routes.get(2).getCode());
    assertEquals("ivk", routes.get(2).getName());
    assertEquals(MedicationRouteTypeEnum.IV, routes.get(2).getType());

    assertEquals("5", routes.get(3).getCode());
    assertEquals("im", routes.get(3).getName());
    assertEquals(MedicationRouteTypeEnum.IV, routes.get(3).getType());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetDoseFormByCode.xml")
  public void testGetDoseFormByCode1()
  {
    final DoseFormDto doseForm = medicationsDao.getDoseFormByCode("1");
    assertEquals(1L, doseForm.getId());
    assertEquals("1", doseForm.getCode());
    assertEquals("Tablet", doseForm.getName());
    assertEquals(DoseFormType.TBL, doseForm.getDoseFormType());
    assertEquals(MedicationOrderFormType.SIMPLE, doseForm.getMedicationOrderFormType());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetDoseFormByCode.xml")
  public void testGetDoseFormByCode2()
  {
    final DoseFormDto doseForm = medicationsDao.getDoseFormByCode("2");
    assertEquals(2L, doseForm.getId());
    assertEquals("2", doseForm.getCode());
    assertEquals("Fluid", doseForm.getName());
    assertNull(doseForm.getDoseFormType());
    assertEquals(MedicationOrderFormType.COMPLEX, doseForm.getMedicationOrderFormType());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testCustomGroups.xml")
  public void testGetCustomGroupNameSortOrderMap()
  {
    final List<String> groups = medicationsDao.getCustomGroupNames("2");

    assertEquals(2L, (long)groups.size());
    assertEquals("Acet. kislina KOOKIT EIT", groups.get(0));
    assertEquals("Paracetamol KOOKIT EIT", groups.get(1));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testCustomGroups.xml")
  public void testGetCustomGroupNames()
  {
    final Set<Long> medicationCodes = new HashSet<>();
    medicationCodes.add(1L);
    medicationCodes.add(2L);
    medicationCodes.add(3L);
    medicationCodes.add(4L);

    final Map<Long, Pair<String, Integer>> resultMapPek =
        medicationsDao.getCustomGroupNameSortOrderMap("1", medicationCodes);

    assertEquals(1L, (long)resultMapPek.size());
    assertEquals("Paracetamol Kardio Hosp", resultMapPek.get(1L).getFirst());
    assertEquals(Integer.valueOf(2), resultMapPek.get(1L).getSecond());

    final Map<Long, Pair<String, Integer>> resultMapKookit =
        medicationsDao.getCustomGroupNameSortOrderMap("2", medicationCodes);
    assertEquals(4L, (long)resultMapKookit.size());
    assertEquals("Paracetamol KOOKIT EIT", resultMapKookit.get(1L).getFirst());
    assertEquals(Integer.valueOf(1), resultMapKookit.get(1L).getSecond());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testLoadMedicationsMapAMPWithVTM.xml")
  public void testGetMedicationDataMapAMPWithVTP()
  {
    final Map<Long, MedicationDataDto> medicationsMap = medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0));

    assertEquals(2, medicationsMap.size());

    final MedicationDataDto ampDto = medicationsMap.get(1L);
    assertEquals(1L, (long)ampDto.getMedication().getId());
    assertEquals("Lekadol", ampDto.getMedication().getName());
    assertEquals("Paracetamol", ampDto.getMedication().getGenericName());
    assertEquals(MedicationLevelEnum.AMP, ampDto.getMedicationLevel());
    assertEquals("1", ampDto.getVtmId());
    assertNull(ampDto.getVmpId());
    assertEquals("1", ampDto.getAmpId());
    assertEquals(1L, ampDto.getDoseForm().getId());
    assertEquals("Pill", ampDto.getDoseForm().getName());
    assertEquals("C1", ampDto.getAtcGroupCode());
    assertEquals("ATC 1", ampDto.getAtcGroupName());
    assertEquals(MedicationTypeEnum.MEDICATION, ampDto.getMedication().getMedicationType());
    assertTrue(ampDto.getMedicationIngredients().stream().anyMatch(i -> i.getIngredientRule() == MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE));
    assertTrue(ampDto.isValid(new DateTime(2016, 6, 12, 0, 0, 0)));
    assertFalse(ampDto.isAntibiotic());
    assertFalse(ampDto.isSuggestSwitchToOral());
    assertFalse(ampDto.isMentalHealthDrug());
    assertTrue(ampDto.isOrderable());
    assertTrue(ampDto.isReviewReminder());
    assertTrue(ampDto.isControlledDrug());
    assertEquals("15", ampDto.getPrice());
    assertEquals(1, ampDto.getMedicationIngredients().get(0).getIngredientId());
    assertEquals("mg", ampDto.getMedicationIngredients().get(0).getStrengthNumeratorUnit());

    final MedicationDataDto vtmDto = medicationsMap.get(2L);
    assertEquals(2L, (long)vtmDto.getMedication().getId());
    assertNull(vtmDto.getPrice());
    assertEquals("Oral", vtmDto.getRoutes().get(0).getName());
    assertEquals(1, vtmDto.getMedicationIngredients().get(0).getIngredientId());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetMedicationDataMapMultipleLevels.xml")
  public void testModifiedReleaseVTMProperties()
  {
    final MedicationDataDto vtm = medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0)).get(2L);

    assertEquals(0, vtm.getProperties().stream().filter(p -> p.getType() == MedicationPropertyType.MODIFIED_RELEASE).count());
    assertEquals(2, vtm.getProperties().stream().filter(p -> p.getType() == MedicationPropertyType.MODIFIED_RELEASE_TIME).count());
    assertTrue(vtm.getProperties().stream().anyMatch(p -> p.getType() == MedicationPropertyType.MODIFIED_RELEASE_TIME && "24".equals(p.getValue())));
    assertTrue(vtm.getProperties().stream().anyMatch(p -> p.getType() == MedicationPropertyType.MODIFIED_RELEASE_TIME && "12".equals(p.getValue())));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetMedicationDataMapMultipleLevels.xml")
  public void testVTMCalculatedMostFrequentVMPNumeratorIngredientUnit()
  {
    final MedicationDataDto vtm = medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0)).get(2L);
    assertTrue(vtm.getMedicationIngredients().stream().allMatch(i -> "cl".equals(i.getStrengthNumeratorUnit())));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetMedicationDataMapMultipleLevels.xml")
  public void testGastroVMPProperties()
  {
    final MedicationDataDto vmp = medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0)).get(3L);

    assertTrue(vmp.getProperties().stream().anyMatch(p -> p.getType() == MedicationPropertyType.GASTRO_RESISTANT));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetMedicationDataMapMultipleLevels.xml")
  public void testMRTimeVMPProperties()
  {
    final MedicationDataDto vmp = medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0)).get(4L);

    assertTrue(vmp.getProperties().stream().anyMatch(p -> p.getType() == MedicationPropertyType.MODIFIED_RELEASE_TIME && "24".equals(p.getValue())));
    assertTrue(vmp.getProperties().stream().anyMatch(p -> p.getType() == MedicationPropertyType.MODIFIED_RELEASE_TIME && "12".equals(p.getValue())));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetMedicationDataMapMultipleLevels.xml")
  public void testCustomProperties()
  {
    final MedicationDataDto amp = medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0)).get(5L);

    assertTrue(amp.getProperties().stream().anyMatch(p -> p.getType() == null && "Custom property".equals(p.getName())));
  }


  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testLoadMedicationsMapCustomGroups.xml")
  public void testLoadMedicationsMapCustomGroups()
  {
    final MedicationDataDto amp = medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0)).get(1L);

    assertEquals("Endo group 1", amp.getCareProviderCustomGroups().get("100").getFirst());
    assertEquals(Integer.valueOf(3), amp.getCareProviderCustomGroups().get("100").getSecond());

    assertEquals("Paracetamol 500 mg tablet", amp.getInterchangeableDrugsGroup());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetMedicationDataMapMultipleLevels.xml")
  public void testDiscretionaryAndUnlicensedRoutePropagation()
  {
    final Map<Long, MedicationDataDto> medicationsMap = medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0));
    final MedicationDataDto vtm = medicationsMap.get(2L);

    final MedicationRouteDto route1 = vtm.getRoutes().stream().filter(r -> r.getId() == 1L).findFirst().get();
    assertTrue(route1.isDiscretionary());
    assertFalse(route1.isUnlicensedRoute());

    final MedicationRouteDto route2 = vtm.getRoutes().stream().filter(r -> r.getId() == 2L).findFirst().get();
    assertFalse(route2.isDiscretionary());
    assertTrue(route2.isUnlicensedRoute());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetVtmRoutes.xml")
  public void testGetRoutesFormularyVTM()
  {
    final Map<Long, MedicationDataDto> medicationsMap = medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0));

    final MedicationDataDto vtm = medicationsMap.get(1L);
    assertEquals(2, vtm.getRoutes().size());
    assertTrue(vtm.getRoutes().stream().anyMatch(r -> r.getId() == 1L));
    assertTrue(vtm.getRoutes().stream().anyMatch(r -> r.getId() == 2L));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetVtmRoutes.xml")
  public void testGetRoutesFormularyVTM2()
  {
    final Map<Long, MedicationDataDto> medicationsMap = medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0));

    final MedicationDataDto vtm = medicationsMap.get(2L);
    assertEquals(1, vtm.getRoutes().size());
    assertTrue(vtm.getRoutes().stream().anyMatch(r -> r.getId() == 1L));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetVtmRoutes.xml")
  public void testGetRoutesNonFormularyVTM()
  {
    final Map<Long, MedicationDataDto> medicationsMap = medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0));

    final MedicationDataDto vtm = medicationsMap.get(3L);
    assertEquals(1, vtm.getRoutes().size());
    assertTrue(vtm.getRoutes().stream().anyMatch(r -> r.getId() == 3L));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetMedicationDataMapMultipleLevels.xml")
  public void testGetMedicationDataMapMultipleLevels()
  {
    final Map<Long, MedicationDataDto> medicationsMap = medicationsDao.loadMedicationsMap(new DateTime(2016, 6, 12, 0, 0, 0));

    assertEquals(9, medicationsMap.size());
    final MedicationDataDto vtm1 = medicationsMap.get(1L);
    final MedicationDataDto vtm2 = medicationsMap.get(2L);

    final MedicationDataDto vmp1 = medicationsMap.get(3L);
    final MedicationDataDto vmp2 = medicationsMap.get(4L);

    final MedicationDataDto amp1 = medicationsMap.get(5L);
    final MedicationDataDto amp3 = medicationsMap.get(7L);
    final MedicationDataDto amp4 = medicationsMap.get(8L);

    // VTMs

    assertEquals(1L, (long)vtm1.getMedication().getId());
    assertEquals("price 1", vtm1.getPrice());
    assertEquals("Oral", vtm1.getRoutes().get(0).getName());
    assertEquals(1, vtm1.getMedicationIngredients().size());
    assertEquals("Paracetamol", vtm1.getMedicationIngredients().get(0).getIngredientName());
    assertEquals(1, vtm1.getMedicationIngredients().get(0).getIngredientId());
    assertEquals(TitrationType.BLOOD_SUGAR, vtm1.getTitration());
    assertFalse(vtm1.isAntibiotic());
    assertEquals(MedicationOrderFormType.SIMPLE, vtm1.getDoseForm().getMedicationOrderFormType());
    assertEquals(2, vtm1.getIndications().size());
    assertTrue(vtm1.getIndications().stream().anyMatch(i -> "Indication 1".equals(i.getName())));
    assertTrue(vtm1.getIndications().stream().anyMatch(i -> "Indication 2".equals(i.getName())));
    assertTrue(vtm1.isInpatient());
    assertFalse(vtm1.isOutpatient());

    assertEquals(2L, (long)vtm2.getMedication().getId());
    assertTrue( vtm2.getRoutes().stream().map(NamedIdentityDto::getName).anyMatch("Intr"::equals));
    assertTrue(vtm2.getRoutes().stream().map(NamedIdentityDto::getName).anyMatch("Oral"::equals));
    assertEquals(2, vtm2.getMedicationIngredients().size());
    assertTrue(vtm2.getMedicationIngredients().stream().map(MedicationIngredientDto::getIngredientName).anyMatch("Paracetamol"::equals));
    assertTrue(vtm2.getMedicationIngredients().stream().map(MedicationIngredientDto::getIngredientName).anyMatch("Aspirin"::equals));
    assertEquals(TitrationType.BLOOD_SUGAR, vtm2.getTitration());
    assertFalse(vtm2.isAntibiotic());
    assertEquals(MedicationOrderFormType.SIMPLE, vtm2.getDoseForm().getMedicationOrderFormType());
    assertEquals(0, vtm2.getIndications().size());

    assertTrue(vtm1.isFormulary());
    assertNull(vtm1.getSortOrder());

    assertFalse(vtm1.getProperties().stream().anyMatch(p -> p.getType() == MedicationPropertyType.UNLICENSED_MEDICATION));
    assertTrue(vtm1.getProperties().stream().anyMatch(p -> p.getType() == MedicationPropertyType.REVIEW_REMINDER));

    // VMPs

    assertEquals(3L, (long)vmp1.getMedication().getId());
    assertEquals("price 1", vmp1.getPrice());
    assertEquals("1", vmp1.getVtmId());
    assertEquals("1", vmp1.getVmpId());
    assertEquals("Oral", vmp1.getRoutes().get(0).getName());
    assertEquals("Oral", vmp1.getDefaultRoute().getName());
    assertEquals("Paracetamol", vmp1.getMedicationIngredients().get(0).getIngredientName());
    assertEquals("mg", vmp1.getMedicationIngredients().get(0).getStrengthNumeratorUnit());
    assertEquals(TitrationType.BLOOD_SUGAR, vmp1.getTitration());
    assertFalse(vmp1.isAntibiotic());
    assertEquals(MedicationOrderFormType.SIMPLE, vmp1.getDoseForm().getMedicationOrderFormType());
    assertTrue(vmp1.isControlledDrug());
    assertTrue(vmp1.isReviewReminder());
    assertEquals("C1", vmp1.getAtcGroupCode());
    assertEquals("ATC 1", vmp1.getAtcGroupName());
    assertEquals(2, vmp1.getIndications().size());
    assertTrue(vmp1.getIndications().stream().anyMatch(i -> "Indication 1".equals(i.getName())));
    assertTrue(vmp1.getIndications().stream().anyMatch(i -> "Indication 2".equals(i.getName())));


    assertEquals(4L, (long)vmp2.getMedication().getId());
    assertEquals("2", vmp2.getVtmId());
    assertEquals("2", vmp2.getVmpId());
    assertEquals(500, vmp2.getDefaultRoute().getMaxDose().getDose().longValue());
    assertTrue(vmp2.getMedicationIngredients().stream().map(MedicationIngredientDto::getIngredientName).anyMatch("Aspirin"::equals));
    assertEquals("mg", vmp2.getAdministrationUnit());
    assertEquals(1, vmp2.getAdministrationUnitFactor().longValue());
    assertEquals(TitrationType.BLOOD_SUGAR, vmp2.getTitration());
    assertFalse(vmp2.isAntibiotic());
    assertEquals(MedicationOrderFormType.SIMPLE, vmp2.getDoseForm().getMedicationOrderFormType());
    assertTrue(!vmp2.isControlledDrug());
    assertTrue(vmp2.isReviewReminder());
    assertEquals("C2", vmp2.getAtcGroupCode());
    assertEquals("ATC 2", vmp2.getAtcGroupName());


    // AMPs

    assertEquals(5L, (long)amp1.getMedication().getId());
    assertEquals("AMP 1", amp1.getMedication().getName());
    assertEquals("2", amp1.getVtmId());
    assertEquals("2", amp1.getVmpId());
    assertTrue(amp1.getMedicationIngredients().stream().map(MedicationIngredientDto::getIngredientName).anyMatch("Aspirin"::equals));
    assertEquals("mg", amp1.getAdministrationUnit());
    assertEquals(1, amp1.getAdministrationUnitFactor().longValue());
    assertEquals(TitrationType.BLOOD_SUGAR, amp1.getTitration());
    assertEquals(MedicationOrderFormType.SIMPLE, amp1.getDoseForm().getMedicationOrderFormType());
    assertTrue(!amp1.isControlledDrug());
    assertTrue(amp1.isReviewReminder());
    assertTrue(amp1.isNotForPrn());
    assertEquals("C2", amp1.getAtcGroupCode());
    assertEquals("ATC 2", amp1.getAtcGroupName());
    assertTrue(amp1.isValid(new DateTime(2016, 6, 12, 0, 0, 0)));
    assertTrue(amp1.isAntibiotic());

    assertEquals(7L, (long)amp3.getMedication().getId());
    assertEquals("AMP 3", amp3.getMedication().getName());
    assertTrue(amp3.isValid(new DateTime(2016, 6, 12, 0, 0, 0)));

    assertEquals(8L, (long)amp4.getMedication().getId());
    assertEquals("AMP 4", amp4.getMedication().getName());
    assertFalse(amp4.isValid(new DateTime(2016, 6, 12, 0, 0, 0)));
    assertFalse(amp4.isOrderable());
  }


  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testVtmUnitNonFormularyVmps.xml")
  public void testVtmUnitNonFormularyVmps()
  {
    // non formulary VTM, that only has non formulary VMPs
    // most common non null unit must be set to VTM
    final Map<Long, MedicationDataDto> medicationsMap = medicationsDao.loadMedicationsMap(
        new DateTime(2016, 6, 12, 0, 0, 0));

    assertEquals(5, medicationsMap.size());
    final MedicationDataDto vtm1 = medicationsMap.get(1L);
    assertEquals(1, vtm1.getMedicationIngredients().size());
    assertEquals(1, vtm1.getMedicationIngredients().get(0).getIngredientId());
    assertEquals(Double.valueOf(1.0), vtm1.getMedicationIngredients().get(0).getStrengthNumerator());
    assertEquals("mg", vtm1.getMedicationIngredients().get(0).getStrengthNumeratorUnit());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testLoadMedicationWarnings.xml")
  public void testGetMedicationWarningsEmptyMedicationList()
  {
    final Collection<MedicationsWarningDto> customWarningsForMedication = medicationsDao.getCustomWarningsForMedication(
        Collections.emptySet(),
        DateTime.now());

    assertTrue(customWarningsForMedication.isEmpty());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testLoadMedicationWarnings.xml")
  public void testGetMedicationWarningsEmptyResult()
  {
    final Collection<MedicationsWarningDto> customWarningsForMedication = medicationsDao.getCustomWarningsForMedication(
        Collections.singleton(3L),
        DateTime.now());

    assertTrue(customWarningsForMedication.isEmpty());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testLoadMedicationWarnings.xml")
  public void testGetMedicationWarnings()
  {
    final Set<Long> medicationIds = new HashSet<>();
    medicationIds.add(1L);
    medicationIds.add(2L);

    final Collection<MedicationsWarningDto> warnings = medicationsDao.getCustomWarningsForMedication(medicationIds, DateTime.now());

    assertEquals(4, warnings.size());
    assertTrue(warnings.stream().anyMatch(
        w ->
        {
          final List<NamedExternalDto> medications = w.getMedications();
          return medications.stream().anyMatch(m -> "1".equals(m.getId())) && w.getSeverity() == WarningSeverity.HIGH;
        }));

    assertTrue(warnings.stream().anyMatch(
        w ->
        {
          final List<NamedExternalDto> medications = w.getMedications();
          return medications.stream().anyMatch(m -> "2".equals(m.getId())) && w.getSeverity() == WarningSeverity.OTHER;
        }));

    assertTrue(warnings.stream().anyMatch(
        w ->
        {
          final List<NamedExternalDto> medications = w.getMedications();
          return medications.stream().anyMatch(m -> "1".equals(m.getId())) && "warning description 3".equals(w.getDescription());
        }));

    assertTrue(warnings.stream().anyMatch(
        w ->
        {
          final List<NamedExternalDto> medications = w.getMedications();
          return medications.stream().anyMatch(m -> "1".equals(m.getId())) && "warning description 4".equals(w.getDescription());
        }));
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testGetDispenseSources.xml")
  public void testGetDispenseSources()
  {
    final List<DispenseSourceDto> dispenseSources = medicationsDao.getDispenseSources();
    assertEquals(2, dispenseSources.size());
    assertEquals(1, dispenseSources.get(0).getId());
    assertTrue(dispenseSources.get(0).isDefaultSource());
    assertEquals("Pharmacy", dispenseSources.get(0).getName());
    assertEquals(2, dispenseSources.get(1).getId());
    assertEquals("Patient", dispenseSources.get(1).getName());
    assertFalse(dispenseSources.get(1).isDefaultSource());
  }

  @Test
  @DatabaseSetup("HibernateMedicationsDaoTest.testMapDocumentsToMedications.xml")
  public void testMapDocumentsToMedications()
  {
    final Map<Long, MedicationDataDto> medicationsMap = new HashMap<>();
    medicationsMap.put(1L, new MedicationDataDto());
    medicationsMap.put(2L, new MedicationDataDto());
    medicationsMap.put(3L, new MedicationDataDto());

    medicationsDao.mapDocumentsToMedications(medicationsMap);

    assertEquals("URL1", medicationsMap.get(1L).getMedicationDocuments().get(0).getExternalSystem());
    assertEquals("http://test1.html", medicationsMap.get(1L).getMedicationDocuments().get(0).getDocumentReference());
    assertEquals(MedicationDocumentType.URL, medicationsMap.get(1L).getMedicationDocuments().get(0).getType());

    assertEquals("DOC2", medicationsMap.get(2L).getMedicationDocuments().get(0).getExternalSystem());
    assertEquals("222", medicationsMap.get(2L).getMedicationDocuments().get(0).getDocumentReference());
    assertEquals(MedicationDocumentType.PDF, medicationsMap.get(2L).getMedicationDocuments().get(0).getType());

    assertEquals("URL2", medicationsMap.get(2L).getMedicationDocuments().get(1).getExternalSystem());
    assertEquals("http://test2.html", medicationsMap.get(2L).getMedicationDocuments().get(1).getDocumentReference());
    assertEquals(MedicationDocumentType.URL, medicationsMap.get(2L).getMedicationDocuments().get(1).getType());
  }

  @Test
  public void testGetMedicationIdForBarcodeExist()
  {
    final Long medicationId = medicationsDao.getMedicationIdForBarcode("111");
    assertEquals(Long.valueOf(1L), medicationId);
  }

  @Test
  public void testGetMedicationIdForBarcodeNotExist()
  {
    final Long medicationId = medicationsDao.getMedicationIdForBarcode("222");
    assertNull(medicationId);
  }
}
