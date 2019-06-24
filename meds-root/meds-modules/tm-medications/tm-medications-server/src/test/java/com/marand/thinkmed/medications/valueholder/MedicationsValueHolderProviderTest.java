package com.marand.thinkmed.medications.valueholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseType;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.business.impl.ReleaseDetailsDisplayProvider;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.business.mapper.MedicationDataDtoMapper;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.dto.FormularyMedicationDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medications.units.provider.TestUnitsProviderImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings({"TooBroadScope"})
public class MedicationsValueHolderProviderTest
{

  private final MedicationDataDtoMapper medicationDataDtoMapper = new MedicationDataDtoMapper();
  private final MedicationsValueHolder medicationsValueHolder = mock(MedicationsValueHolder.class);
  private final MedicationsValueHolderProviderImpl medicationsValueHolderProvider = new MedicationsValueHolderProviderImpl();

  @Before
  public void setup()
  {
    medicationsValueHolderProvider.setMedicationDataDtoMapper(medicationDataDtoMapper);
    medicationsValueHolderProvider.setMedicationsValueHolder(medicationsValueHolder);

    final TherapyDisplayProvider therapyDisplayProvider = new TherapyDisplayProvider();
    therapyDisplayProvider.setUnitsProvider(new TestUnitsProviderImpl());
    therapyDisplayProvider.setReleaseDetailsDisplayProvider(new ReleaseDetailsDisplayProvider());

    final MedsProperties medsProperties = new MedsProperties();
    medsProperties.setFormularyFilterEnabled(true);

    medicationDataDtoMapper.setMedsProperties(medsProperties);
    medicationDataDtoMapper.setTherapyDisplayProvider(therapyDisplayProvider);
  }

  private void mockMedicationsMap(final List<MedicationDataDto> medications)
  {
    Mockito
        .when(medicationsValueHolder.getValue())
        .thenReturn(new MedicationsValueHolder.MedicationsHolderDo(medications.stream().collect(Collectors.toMap(m -> m.getMedication().getId(), m -> m))));

    Mockito
        .when(medicationsValueHolder.getMedications())
        .thenReturn(medications.stream().collect(Collectors.toMap(m -> m.getMedication().getId(), m -> m)));
  }

  private MedicationDataDto buildMedicationDataDto(
      final long id,
      final String genericName,
      final DateTime validFrom,
      final DateTime validTo,
      final String administrationUnit,
      final String supplyUnit,
      final String ampId,
      final String vmpId,
      final String vtmId,
      final MedicationLevelEnum medicationLevel,
      final String atcGroupCode,
      final boolean formulary,
      final List<MedicationRouteDto> routes,
      final MedicationRouteDto defaultRoute,
      final List<MedicationIngredientDto> medicationIngredients,
      final Integer sortOrder)
  {
    final MedicationDataDto dataDto = new MedicationDataDto();

    final MedicationDto medication = new MedicationDto();
    medication.setId(id);
    medication.setName(id + " medication name");
    medication.setGenericName(genericName);
    medication.setDisplayName(id + " display name");
    dataDto.setMedication(medication);

    dataDto.setAdministrationUnit(administrationUnit);
    dataDto.setSupplyUnit(supplyUnit);

    dataDto.setValidFrom(validFrom);
    dataDto.setValidTo(validTo);

    dataDto.setAmpId(ampId);
    dataDto.setVmpId(vmpId);
    dataDto.setVtmId(vtmId);
    dataDto.setMedicationLevel(medicationLevel);

    dataDto.setAtcGroupCode(atcGroupCode);
    dataDto.setFormulary(formulary);
    dataDto.setSortOrder(sortOrder);

    if (routes != null)
    {
      dataDto.setRoutes(routes);
    }

    dataDto.setDefaultRoute(defaultRoute);

    if (medicationIngredients != null)
    {
      dataDto.setMedicationIngredients(medicationIngredients);
    }

    return dataDto;
  }

  @Test
  public void testGetMedicationDataByIdValidTime()
  {
    final String unit = "ml";
    final List<MedicationDataDto> medications = new ArrayList<>();

    medications.add(buildMedicationDataDto(1L, "generic name", new DateTime(2016, 2, 4, 12, 0), new DateTime(2017, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, null, null, null, null));
    mockMedicationsMap(medications);

    final MedicationDataDto medicationData = medicationsValueHolderProvider.getMedicationData(1L);

    assertNotNull(medicationData);
    assertEquals(medicationData.getMedication().getDisplayName(), medications.get(0).getMedication().getDisplayName());
    assertEquals(medicationData.getMedication().getGenericName(), medications.get(0).getMedication().getGenericName());
  }

  @Test
  public void testGetMedicationDataByIdNonValidTime()
  {
    final String unit = "ml";
    final List<MedicationDataDto> medications = new ArrayList<>();

    medications.add(buildMedicationDataDto(1L, "generic name", new DateTime(2016, 2, 4, 12, 0), new DateTime(2017, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, null, null, null, null));
    mockMedicationsMap(medications);

    final MedicationDataDto medicationData = medicationsValueHolderProvider.getMedicationData(1L);

    assertFalse(medicationData.isValid(new DateTime(2018, 10, 4, 12, 0)));
  }

  @Test
  public void getValidMedicationDataMap()
  {
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(1L);

    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(0L, "generic", new DateTime(2010, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, null));
    medications.add(buildMedicationDataDto(1L, "generic", new DateTime(2010, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 3));
    medications.add(buildMedicationDataDto(2L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 2));
    medications.add(buildMedicationDataDto(3L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 1));
    mockMedicationsMap(medications);

    final Map<Long, MedicationDataDto> medicationDataMap = medicationsValueHolderProvider.getValidMedicationDataMap(
        new HashSet<>(Arrays.asList(0L, 1L, 2L, 3L)),
        new DateTime(2012, 2, 4, 12, 0));

    assertEquals(2, medicationDataMap.size());
    assertTrue(medicationDataMap.containsKey(0L));
    assertTrue(medicationDataMap.containsKey(1L));
  }

  @Test
  public void getAllMedicationDataMap()
  {
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(1L);

    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(0L, "generic", new DateTime(2010, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, null));
    medications.add(buildMedicationDataDto(1L, "generic", new DateTime(2010, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 3));
    medications.add(buildMedicationDataDto(2L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 2));
    medications.add(buildMedicationDataDto(3L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 1));
    mockMedicationsMap(medications);

    final Map<Long, MedicationDataDto> medicationDataMap = medicationsValueHolderProvider.getAllMedicationDataMap(
        new HashSet<>(Arrays.asList(0L, 1L, 2L, 3L)));

    assertEquals(4, medicationDataMap.size());
    assertTrue(medicationDataMap.containsKey(0L));
    assertTrue(medicationDataMap.containsKey(1L));
    assertTrue(medicationDataMap.containsKey(2L));
    assertTrue(medicationDataMap.containsKey(3L));
  }

  @Test
  public void testGetMedicationIdsWithIngredientRule()
  {
    final String unit = "ml";

    final MedicationIngredientDto ingredientWithRule = new MedicationIngredientDto();
    ingredientWithRule.setIngredientId(1L);
    ingredientWithRule.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    final MedicationIngredientDto ingredientWithoutRule = new MedicationIngredientDto();
    ingredientWithoutRule.setIngredientId(2L);

    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(1L, "generic name", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, null, null, Arrays.asList(ingredientWithRule, ingredientWithoutRule), null));
    medications.add(buildMedicationDataDto(2L, "generic name", new DateTime(2016, 2, 4, 12, 0), new DateTime(2017, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, null, null, Arrays.asList(ingredientWithoutRule, ingredientWithoutRule), null));
    medications.add(buildMedicationDataDto(3L, "generic name", new DateTime(2016, 2, 4, 12, 0), new DateTime(2017, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, null, null, Arrays.asList(ingredientWithRule, ingredientWithoutRule), null));
    mockMedicationsMap(medications);

    /*
      1. not valid
      2. valid, no rule
      3. valid, with rule
     */

    final Set<Long> idsWithIngredientRule = medicationsValueHolderProvider.getMedicationIdsWithIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    assertEquals(2L, idsWithIngredientRule.size());
    assertTrue(idsWithIngredientRule.contains(1L));
    assertTrue(idsWithIngredientRule.contains(3L));
  }

  @Test
  public void testGetMedicationIdsWithIngredientId()
  {
    final String unit = "ml";

    final MedicationIngredientDto ingredient1 = new MedicationIngredientDto();
    ingredient1.setIngredientId(1L);

    final MedicationIngredientDto ingredient2 = new MedicationIngredientDto();
    ingredient2.setIngredientId(2L);

    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(1L, "generic name", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, null, null, Arrays.asList(ingredient1, ingredient2), null));
    medications.add(buildMedicationDataDto(2L, "generic name", new DateTime(2016, 2, 4, 12, 0), new DateTime(2017, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, null, null, Arrays.asList(ingredient2, ingredient2), null));
    medications.add(buildMedicationDataDto(3L, "generic name", new DateTime(2016, 2, 4, 12, 0), new DateTime(2017, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, null, null, Arrays.asList(ingredient1, ingredient2), null));
    mockMedicationsMap(medications);

    /*
      1. not valid
      2. valid, no matching ingredient
      3. valid, matching ingredient
     */

    final Collection<Long> idsWithIngredientRule = medicationsValueHolderProvider.getMedicationIdsWithIngredientId(1L);

    assertEquals(2L, idsWithIngredientRule.size());
    assertTrue(idsWithIngredientRule.contains(1L));
    assertTrue(idsWithIngredientRule.contains(3L));
  }

  @Test
  public void testGetMedicationRoutes()
  {
    final String unit = "ml";

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(1L);

    final MedicationRouteDto route2 = new MedicationRouteDto();
    route2.setId(2L);

    final List<MedicationRouteDto> routes = new ArrayList<>(Arrays.asList(route, route2));

    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(1L, "generic name", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, routes, null, null,
                                           null));
    mockMedicationsMap(medications);

    final List<MedicationRouteDto> medicationRoutes = medicationsValueHolderProvider.getMedicationRoutes(1L);

    assertEquals(2L, medicationRoutes.size());
    assertTrue(medicationRoutes.stream().anyMatch(r -> r.getId() == 1L));
    assertTrue(medicationRoutes.stream().anyMatch(r -> r.getId() == 2L));
  }

  @Test
  public void testFindSimilarMedications1()
  {
    //reference medication, has interchangeable drugs group
    final MedicationDataDto med0 = new MedicationDataDto();
    med0.setMedication(new MedicationDto(0L, "Lekadol 100mg tablet"));
    med0.setInterchangeableDrugsGroup("Paracetamol 100mg tablet");
    med0.setValidFrom(new DateTime(2016, 2, 4, 12, 0));
    med0.setValidTo(new DateTime(3000, 1, 1, 0, 0));

    //valid, same interchangeable drugs group
    final MedicationDataDto med1 = new MedicationDataDto();
    med1.setMedication(new MedicationDto(1L, "Daleron 100mg tablet"));
    med1.setInterchangeableDrugsGroup("Paracetamol 100mg tablet");
    med1.setValidFrom(new DateTime(2016, 2, 4, 12, 0));
    med1.setValidTo(new DateTime(3000, 1, 1, 0, 0));

    //not valid, same interchangeable drugs group
    final MedicationDataDto med2 = new MedicationDataDto();
    med2.setMedication(new MedicationDto(2L, "Par2 100mg tablet"));
    med2.setInterchangeableDrugsGroup("Paracetamol 100mg tablet");
    med2.setValidFrom(new DateTime(2016, 2, 4, 12, 0));
    med2.setValidTo(new DateTime(2017, 1, 1, 0, 0));

    //valid, no interchangeable drugs group
    final MedicationDataDto med3 = new MedicationDataDto();
    med3.setMedication(new MedicationDto(3L, "Ibuprofen 100mg tablet"));
    med3.setInterchangeableDrugsGroup(null);
    med3.setValidFrom(new DateTime(2016, 2, 4, 12, 0));
    med3.setValidTo(new DateTime(3000, 1, 1, 0, 0));

    //valid, different interchangeable drugs group
    final MedicationDataDto med4 = new MedicationDataDto();
    med4.setMedication(new MedicationDto(4L, "Asp3 100mg tablet"));
    med4.setInterchangeableDrugsGroup("Aspirin 100mg tablet");
    med4.setValidFrom(new DateTime(2016, 2, 4, 12, 0));
    med4.setValidTo(new DateTime(3000, 1, 1, 0, 0));

    final List<MedicationDataDto> medications = Lists.newArrayList(med0, med1, med2, med3, med4);
    mockMedicationsMap(medications);

    final List<MedicationDataDto> similarMedicationDataDtos = medicationsValueHolderProvider.findSimilarMedicationDataDtos(
        0L,
        new DateTime(2019, 3, 3, 12, 0));

    assertEquals(2, similarMedicationDataDtos.size());
    similarMedicationDataDtos.sort(Comparator.comparing(o -> o.getMedication().getId()));
    assertEquals(Long.valueOf(0L), similarMedicationDataDtos.get(0).getMedication().getId());
    assertEquals(Long.valueOf(1L), similarMedicationDataDtos.get(1).getMedication().getId());
  }

  @Test
  public void testFindSimilarMedications2()
  {
    //reference medication, no interchangeable drugs group
    final MedicationDataDto med0 = new MedicationDataDto();
    med0.setMedication(new MedicationDto(0L, "Lekadol 100mg tablet"));
    med0.setInterchangeableDrugsGroup(null);
    med0.setValidFrom(new DateTime(2016, 2, 4, 12, 0));
    med0.setValidTo(new DateTime(3000, 1, 1, 0, 0));

    //valid, same interchangeable drugs group
    final MedicationDataDto med1 = new MedicationDataDto();
    med1.setMedication(new MedicationDto(1L, "Daleron 100mg tablet"));
    med1.setInterchangeableDrugsGroup("Paracetamol 100mg tablet");
    med1.setValidFrom(new DateTime(2016, 2, 4, 12, 0));
    med1.setValidTo(new DateTime(3000, 1, 1, 0, 0));

    //not valid, same interchangeable drugs group
    final MedicationDataDto med2 = new MedicationDataDto();
    med2.setMedication(new MedicationDto(2L, "Par2 100mg tablet"));
    med2.setInterchangeableDrugsGroup("Paracetamol 100mg tablet");
    med2.setValidFrom(new DateTime(2016, 2, 4, 12, 0));
    med2.setValidTo(new DateTime(2017, 1, 1, 0, 0));

    //valid, no interchangeable drugs group
    final MedicationDataDto med3 = new MedicationDataDto();
    med3.setMedication(new MedicationDto(3L, "Ibuprofen 100mg tablet"));
    med3.setInterchangeableDrugsGroup(null);
    med3.setValidFrom(new DateTime(2016, 2, 4, 12, 0));
    med3.setValidTo(new DateTime(3000, 1, 1, 0, 0));

    //valid, different interchangeable drugs group
    final MedicationDataDto med4 = new MedicationDataDto();
    med4.setMedication(new MedicationDto(4L, "Asp3 100mg tablet"));
    med4.setInterchangeableDrugsGroup("Aspirin 100mg tablet");
    med4.setValidFrom(new DateTime(2016, 2, 4, 12, 0));
    med4.setValidTo(new DateTime(3000, 1, 1, 0, 0));

    final List<MedicationDataDto> medications = Lists.newArrayList(med0, med1, med2, med3, med4);
    mockMedicationsMap(medications);

    final List<MedicationDataDto> similarMedicationDataDtos = medicationsValueHolderProvider.findSimilarMedicationDataDtos(
        0L,
        new DateTime(2019, 3, 3, 12, 0));

    assertEquals(1, similarMedicationDataDtos.size());
    assertEquals(Long.valueOf(0L), similarMedicationDataDtos.get(0).getMedication().getId());
  }

  @Test
  public void testFindChildMedicationsVtm()
  {
    final String unit = "ml";

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(1L);

    final MedicationRouteDto route2 = new MedicationRouteDto();
    route2.setId(2L);

    final MedicationRouteDto route3 = new MedicationRouteDto();
    route3.setId(3L);

    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(0L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(3000, 1, 1, 0, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.VTM, null, false, Arrays.asList(route, route2, route3), null, null, null));
    medications.add(buildMedicationDataDto(1L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 1, 1, 0, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Arrays.asList(route, route2, route3), null, null, null));
    medications.add(buildMedicationDataDto(2L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(3000, 1, 1, 0, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, Arrays.asList(route, route2, route3), null, null, null));
    medications.add(buildMedicationDataDto(3L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(3000, 1, 1, 0, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, Collections.singletonList(route3), null, null, null));
    medications.add(buildMedicationDataDto(4L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(3000, 1, 1, 0, 0), unit, unit, "1", "2", "5", MedicationLevelEnum.AMP, null, false, Arrays.asList(route, route2, route3), null, null, null));
    mockMedicationsMap(medications);

    /*
      0. reference medication
      1. not valid, VMP, same routes, same VTM id
      2. valid, AMP, same routes, same VTM id
      3. valid, AMP, different routes, same VTM id
      4. valid, AMP, same routes, different VTM id
     */

    final Set<Long> routeIds = Sets.newHashSet(1L, 2L);
    final Collection<MedicationDto> childProducts = medicationsValueHolderProvider.getMedicationChildProducts(
        0L,
        routeIds,
        null,
        new DateTime(2016, 10, 4, 12, 0));

    assertEquals(1L, childProducts.size());
    assertTrue(childProducts.stream().anyMatch(m -> m.getId() == 2L));
  }

  @Test
  public void testFindChildMedicationsVtmModifiedRelease()
  {
    final String unit = "ml";

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(1L);

    final MedicationRouteDto route2 = new MedicationRouteDto();
    route2.setId(2L);

    final MedicationRouteDto route3 = new MedicationRouteDto();
    route3.setId(3L);

    final MedicationDataDto medication0 = buildMedicationDataDto(0L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(3000, 1, 1, 0, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.VTM, null, false, Arrays.asList(route, route2, route3), null, null, null);
    medication0.getProperties().add(new MedicationPropertyDto(10L, MedicationPropertyType.MODIFIED_RELEASE_TIME, "MODIFIED_RELEASE_TIME", "24"));

    final MedicationDataDto medication1 = buildMedicationDataDto(1L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(3000, 1, 1, 0, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, Arrays.asList(route, route2, route3), null, null, null);
    medication1.getProperties().add(new MedicationPropertyDto(11L, MedicationPropertyType.MODIFIED_RELEASE_TIME, "MODIFIED_RELEASE_TIME","12"));

    final MedicationDataDto medication2 = buildMedicationDataDto(2L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(3000, 1, 1, 0, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, Arrays.asList(route, route2, route3), null, null, null);;
    medication2.getProperties().add(new MedicationPropertyDto(12L, MedicationPropertyType.MODIFIED_RELEASE_TIME, "MODIFIED_RELEASE_TIME", "24"));

    final MedicationDataDto medication3 = buildMedicationDataDto(3L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(3000, 1, 1, 0, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, Arrays.asList(route, route2, route3), null, null, null);;
    medication3.getProperties().add(new MedicationPropertyDto(13L, MedicationPropertyType.GASTRO_RESISTANT, "GASTRO_RESISTANT"));

    final MedicationDataDto medication4 = buildMedicationDataDto(4L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(3000, 1, 1, 0, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, Arrays.asList(route, route2, route3), null, null, null);;

    mockMedicationsMap(Lists.newArrayList(medication0, medication1, medication2, medication3, medication4));

    /*
      0. reference medication
      1. same release details type, different time
      2. same release details type, same time
      3. different release details type
      4. no release details
     */

    final Set<Long> routeIds = Sets.newHashSet(1L, 2L);
    final Collection<MedicationDto> childProducts = medicationsValueHolderProvider.getMedicationChildProducts(
        0L,
        routeIds,
        new ReleaseDetailsDto(ReleaseType.MODIFIED_RELEASE, 24),
        new DateTime(2016, 10, 4, 12, 0));

    assertEquals(1L, childProducts.size());
    assertTrue(childProducts.stream().anyMatch(m -> m.getId() == 2L));
  }

  @Test
  public void testFindChildMedicationsVmp()
  {
    final String unit = "ml";

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(1L);

    final MedicationRouteDto route2 = new MedicationRouteDto();
    route2.setId(2L);

    final MedicationRouteDto route3 = new MedicationRouteDto();
    route3.setId(3L);

    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(0L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Arrays.asList(route, route2, route3), null, null,
                                           null));
    medications.add(buildMedicationDataDto(1L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Arrays.asList(route, route2, route3), null, null,
                                           null));
    medications.add(buildMedicationDataDto(2L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2017, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, Arrays.asList(route, route2, route3), null, null,
                                           null));
    medications.add(buildMedicationDataDto(3L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2017, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, Collections.singletonList(route3), null, null,
                                           null));
    medications.add(buildMedicationDataDto(4L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2017, 2, 4, 12, 0), unit, unit, "1", "5", "3", MedicationLevelEnum.AMP, null, false, Arrays.asList(route, route2, route3), null, null,
                                           null));
    mockMedicationsMap(medications);

   /*
      0. reference medication
      1. not valid, VMP, same routes, same VMP id
      2. valid, AMP, same routes, same VMP id
      3. valid, AMP, different routes, same VMP id
      4. valid, AMP, same routes, different VMP id
     */

    final Set<Long> routeIds = Sets.newHashSet(1L, 2L);
    final Collection<MedicationDto> childProducts = medicationsValueHolderProvider.getMedicationChildProducts(
        0L,
        routeIds,
        null,
        new DateTime(2016, 10, 4, 12, 0));

    assertEquals(1L, childProducts.size());
    assertTrue(childProducts.stream().anyMatch(m -> m.getId() == 2L));
  }

  @Test
  public void testFindChildMedicationsVMPNoneFound()
  {
    final String unit = "ml";

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(1L);

    final MedicationRouteDto route2 = new MedicationRouteDto();
    route2.setId(2L);

    final MedicationRouteDto route3 = new MedicationRouteDto();
    route3.setId(3L);

    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(0L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Arrays.asList(route, route2, route3), null, null, null));
    medications.add(buildMedicationDataDto(1L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), unit, unit, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Arrays.asList(route, route2, route3), null, null, null));
    mockMedicationsMap(medications);

    final Set<Long> routeIds = Sets.newHashSet(1L, 2L);
    final Collection<MedicationDto> childProducts = medicationsValueHolderProvider.getMedicationChildProducts(
        0L,
        routeIds,
        null,
        new DateTime(2016, 10, 4, 12, 0));

    assertTrue(childProducts.isEmpty());
  }

  @Test
  public void findMedicationsSort()
  {
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(1L);

    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(0L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, null));
    medications.add(buildMedicationDataDto(1L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 3));
    medications.add(buildMedicationDataDto(2L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 2));
    medications.add(buildMedicationDataDto(3L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 1));
    mockMedicationsMap(medications);

    final List<MedicationDataDto> result = medicationsValueHolderProvider.getAllMedicationDataDtos();

    assertEquals(3L, result.get(0).getMedication().getId().longValue());
    assertEquals(2L, result.get(1).getMedication().getId().longValue());
    assertEquals(1L, result.get(2).getMedication().getId().longValue());
    assertEquals(0L, result.get(3).getMedication().getId().longValue());
  }

  @Test
  public void findValidMedications()
  {
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(1L);

    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(0L, "generic", new DateTime(2010, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, null));
    medications.add(buildMedicationDataDto(1L, "generic", new DateTime(2010, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 3));
    medications.add(buildMedicationDataDto(2L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 2));
    medications.add(buildMedicationDataDto(3L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 1));
    mockMedicationsMap(medications);

    final List<MedicationDataDto> result = medicationsValueHolderProvider.getValidMedicationDataDtos(new DateTime(2012, 2, 4, 12, 0));

    assertTrue(result.stream().anyMatch(m -> m.getMedication().getId() == 0L));
    assertTrue(result.stream().anyMatch(m -> m.getMedication().getId() == 1L));
  }

  @Test
  public void findMedicationsSortSameSortOrder()
  {
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(1L);

    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(0L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 3));
    medications.add(buildMedicationDataDto(1L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 3));
    medications.add(buildMedicationDataDto(2L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 3));
    medications.add(buildMedicationDataDto(3L, "generic", new DateTime(2016, 2, 4, 12, 0), new DateTime(2016, 2, 4, 12, 0), "ml", null, "1", "2", "3", MedicationLevelEnum.VMP, null, false, Collections.singletonList(route), null, null, 1));
    mockMedicationsMap(medications);

    final List<MedicationDataDto> result = medicationsValueHolderProvider.getAllMedicationDataDtos();

    assertEquals(3L, result.get(0).getMedication().getId().longValue());
    assertEquals(0L, result.get(1).getMedication().getId().longValue());
    assertEquals(1L, result.get(2).getMedication().getId().longValue());
    assertEquals(2L, result.get(3).getMedication().getId().longValue());
  }

  @Test
  public void testGetMedicationById()
  {
    final String unit = "ml";
    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(1L, "generic name", new DateTime(), new DateTime(), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, null, null, null, null));
    medications.add(buildMedicationDataDto(2L, "generic name", new DateTime(), new DateTime(), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, null, null, null, null));
    medications.add(buildMedicationDataDto(3L, "generic name", new DateTime(), new DateTime(), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, null, null, null, null));
    mockMedicationsMap(medications);

    final MedicationDto medication = medicationsValueHolderProvider.getMedication(2L);
    assertNotNull(medication);
    assertEquals(medication.getDisplayName(), medications.get(1).getMedication().getDisplayName());
    assertEquals(medication.getGenericName(), medications.get(1).getMedication().getGenericName());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetMedicationByIdDoesNotExist()
  {
    final String unit = "ml";
    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(1L, "generic name", new DateTime(), new DateTime(), unit, unit, "1", "2", "3", MedicationLevelEnum.AMP, null, false, null, null, null, null));
    mockMedicationsMap(medications);

    medicationsValueHolderProvider.getMedication(3L);
  }

  @Test
  public void testGetVmpMedications()
  {
    final String unit = "ml";
    final List<MedicationDataDto> medications = new ArrayList<>();
    medications.add(buildMedicationDataDto(1L, "generic name", new DateTime().minusDays(100), null, unit, unit, "1", "2", "10", MedicationLevelEnum.VMP, null, true, null, null, null, null));
    medications.add(buildMedicationDataDto(2L, "generic name", new DateTime().minusDays(100), null, unit, unit, "1", "2", "10", MedicationLevelEnum.VMP, null, false, null, null, null, null));
    medications.add(buildMedicationDataDto(3L, "generic name", new DateTime().minusDays(100), null, unit, unit, "1", "2", "11", MedicationLevelEnum.VMP, null, true, null, null, null, null));
    medications.add(buildMedicationDataDto(4L, "generic name", new DateTime().minusDays(100), null, unit, unit, "1", "2", "10", MedicationLevelEnum.AMP, null, true, null, null, null, null));
    medications.add(buildMedicationDataDto(5L, "generic name", new DateTime().minusDays(100), new DateTime().minusDays(50), unit, unit, "1", "2", "10", MedicationLevelEnum.AMP, null, true, null, null, null, null));
    mockMedicationsMap(medications);

    final List<FormularyMedicationDto> vmps = medicationsValueHolderProvider.getVmpMedications("10", new DateTime());
    assertEquals(2, vmps.size());

    assertEquals(1L, vmps.get(0).getId());
    assertEquals("1 medication name", vmps.get(0).getName());
    assertTrue(vmps.get(0).isFormulary());

    assertEquals(2L, vmps.get(1).getId());
    assertEquals("2 medication name", vmps.get(1).getName());
    assertFalse(vmps.get(1).isFormulary());
  }
}