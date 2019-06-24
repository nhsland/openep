package com.marand.thinkmed.medications.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.marand.thinkmed.html.components.tree.TreeNodeData;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationFinderFilterEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationSimpleDto;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.business.impl.ReleaseDetailsDisplayProvider;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.business.mapper.MedicationDataDtoMapper;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolder;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("unused")
@RunWith(SpringJUnit4ClassRunner.class)
public class MedicationsFinderTest
{
  @InjectMocks
  private final MedicationsFinderImpl medicationsFinder = new MedicationsFinderImpl();

  @Mock
  private MedicationsValueHolder medicationsValueHolder;

  @Mock
  private MedicationsDao medicationsDao;

  @Mock
  private TherapyDisplayProvider therapyDisplayProvider;

  @Mock
  private ReleaseDetailsDisplayProvider releaseDetailsDisplayProvider;

  @Mock
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  @Spy
  private final MedicationDataDtoMapper medicationDataDtoMapper = new MedicationDataDtoMapper();

  @Before
  public void init()
  {
    final MedsProperties medsProperties = new MedsProperties();
    medsProperties.setFormularyFilterEnabled(true);
    medicationDataDtoMapper.setMedsProperties(medsProperties);
    medicationDataDtoMapper.setReleaseDetailsDisplayProvider(releaseDetailsDisplayProvider);
  }

  @Test
  public void testFilterMedicationsTreeSingleGeneric()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "para", true);

    assertEquals(1, filteredMedications.size());
    assertEquals(2, filteredMedications.get(0).getChildren().size());
    assertEquals(2, filteredMedications.get(0).getChildren().get(0).getChildren().size());
    assertEquals(2, filteredMedications.get(0).getChildren().get(1).getChildren().size());

    assertFalse(filteredMedications.get(0).isExpanded());
    assertFalse(filteredMedications.get(0).getChildren().get(0).isExpanded());
    assertFalse(filteredMedications.get(0).getChildren().get(1).isExpanded());
  }

  @Test
  public void testFilterMedicationsTreeTradeName()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "TradeA", true);

    assertEquals(1, filteredMedications.size());
    assertEquals(1, filteredMedications.get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(0).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpanded());
    assertTrue(filteredMedications.get(0).getChildren().get(0).isExpanded());
  }

  @Test
  public void testFilterMedicationsTreeGenericDose()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "para 500", true);

    assertEquals(1, filteredMedications.size());
    assertEquals(1, filteredMedications.get(0).getChildren().size());
    assertEquals(2, filteredMedications.get(0).getChildren().get(0).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpanded());
    assertFalse(filteredMedications.get(0).getChildren().get(0).isExpanded());
  }

  @Test
  public void testFilterMedicationsTreeBrandDose1()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "leka 500", true);

    assertEquals(1, filteredMedications.size());
    assertEquals(1, filteredMedications.get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(0).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpanded());
    assertTrue(filteredMedications.get(0).getChildren().get(0).isExpanded());
  }

  @Test
  public void testFilterMedicationsTreeBrandDose2()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "dale 500", true);

    assertEquals(1, filteredMedications.size());
    assertEquals(1, filteredMedications.get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(0).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpanded());
    assertTrue(filteredMedications.get(0).getChildren().get(0).isExpanded());
  }

  @Test
  public void testFilterMedicationsTreeBrand()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "dale", true);

    assertEquals(1, filteredMedications.size());
    assertEquals(2, filteredMedications.get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(0).getChildren().size());
    assertEquals(1, filteredMedications.get(0).getChildren().get(1).getChildren().size());

    assertTrue(filteredMedications.get(0).isExpanded());
    assertTrue(filteredMedications.get(0).getChildren().get(0).isExpanded());
    assertTrue(filteredMedications.get(0).getChildren().get(1).isExpanded());
  }

  @Test
  public void testFilterMedicationsTreeDoesntStartWithSearchWord1()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "aleron", true);

    assertEquals(0, filteredMedications.size());
  }

  @Test
  public void testFilterMedicationsTreeDoesntStartWithSearchWord2()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "aleron", false);

    assertEquals(1, filteredMedications.size());
  }

  @Test
  public void testFilterMedicationsTreeEmptySearch()
  {
    final List<TreeNodeData> medications = getMedicationTree();
    final List<TreeNodeData> filteredMedications = medicationsFinder.filterMedicationsTree(medications, "", true);

    assertEquals(1, filteredMedications.size());
  }

  @Test
  public void testFindMedicationsNoFilter()
  {
    Mockito
        .when(medicationsValueHolderProvider.getValidMedicationDataDtos(any(DateTime.class)))
        .thenReturn(new ArrayList<>(getMedicationsMap().values()));

    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications(
            "para",
            true,
            EnumSet.noneOf(MedicationFinderFilterEnum.class),
            new DateTime(2018, 1, 1, 1, 0),
            new Locale("en"));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(2, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(2, vmp1.getChildren().size());

    final TreeNodeData amp11 = vmp1.getChildren().get(0);
    assertEquals("Lekadol 500 mg oral", ((MedicationSimpleDto)amp11.getData()).getName());

    final TreeNodeData amp12 = vmp1.getChildren().get(1);
    assertEquals("Daleron 500 mg oral", ((MedicationSimpleDto)amp12.getData()).getName());

    final TreeNodeData vmp2 = vtm.getChildren().get(1);
    assertEquals("Paracetamol 250 mg oral", ((MedicationSimpleDto)vmp2.getData()).getName());
    assertEquals(3, vmp2.getChildren().size());

    final TreeNodeData amp21 = vmp2.getChildren().get(0);
    assertEquals("Lekadol 250 mg oral", ((MedicationSimpleDto)amp21.getData()).getName());

    final TreeNodeData amp22 = vmp2.getChildren().get(1);
    assertEquals("Lekadol 250 mg oral not FORMULARY", ((MedicationSimpleDto)amp22.getData()).getName());

    final TreeNodeData amp23 = vmp2.getChildren().get(2);
    assertEquals("Daleron 250 mg oral", ((MedicationSimpleDto)amp23.getData()).getName());
  }

  @Test
  public void testFindMedicationsInpatientFilter()
  {
    Mockito
        .when(medicationsValueHolderProvider.getValidMedicationDataDtos(any(DateTime.class)))
        .thenReturn(new ArrayList<>(getMedicationsMap().values()));

    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications(
            "para",
            true,
            EnumSet.of(MedicationFinderFilterEnum.INPATIENT_PRESCRIPTION),
            new DateTime(2018, 1, 1, 1, 0),
            new Locale("en"));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(2, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(1, vmp1.getChildren().size());

    final TreeNodeData amp11 = vmp1.getChildren().get(0);
    assertEquals("Lekadol 500 mg oral", ((MedicationSimpleDto)amp11.getData()).getName());

    final TreeNodeData vmp2 = vtm.getChildren().get(1);
    assertEquals("Paracetamol 250 mg oral", ((MedicationSimpleDto)vmp2.getData()).getName());
    assertEquals(2, vmp2.getChildren().size());

    final TreeNodeData amp21 = vmp2.getChildren().get(0);
    assertEquals("Lekadol 250 mg oral", ((MedicationSimpleDto)amp21.getData()).getName());
  }

  @Test
  public void testFindMedicationsOutpatientFilter()
  {
    Mockito
        .when(medicationsValueHolderProvider.getValidMedicationDataDtos(any(DateTime.class)))
        .thenReturn(new ArrayList<>(getMedicationsMap().values()));

    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications(
            "para",
            true,
            EnumSet.of(MedicationFinderFilterEnum.OUTPATIENT_PRESCRIPTION),
            new DateTime(2018, 1, 1, 1, 0),
            new Locale("en"));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(2, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(1, vmp1.getChildren().size());

    final TreeNodeData amp11 = vmp1.getChildren().get(0);
    assertEquals("Daleron 500 mg oral", ((MedicationSimpleDto)amp11.getData()).getName());

    final TreeNodeData vmp2 = vtm.getChildren().get(1);
    assertEquals("Paracetamol 250 mg oral", ((MedicationSimpleDto)vmp2.getData()).getName());
    assertEquals(1, vmp2.getChildren().size());

    final TreeNodeData amp21 = vmp2.getChildren().get(0);
    assertEquals("Daleron 250 mg oral", ((MedicationSimpleDto)amp21.getData()).getName());
  }

  @Test
  public void testFindMedicationsInpatientFormularyFilter()
  {
    Mockito
        .when(medicationsValueHolderProvider.getValidMedicationDataDtos(any(DateTime.class)))
        .thenReturn(new ArrayList<>(getMedicationsMap().values()));

    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications(
            "para",
            true,
            EnumSet.of(MedicationFinderFilterEnum.INPATIENT_PRESCRIPTION, MedicationFinderFilterEnum.FORMULARY),
            new DateTime(2018, 1, 1, 1, 0),
            new Locale("en"));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(2, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(1, vmp1.getChildren().size());

    final TreeNodeData vmp2 = vtm.getChildren().get(1);
    assertEquals("Paracetamol 250 mg oral", ((MedicationSimpleDto)vmp2.getData()).getName());
    assertEquals(1, vmp2.getChildren().size());

    final TreeNodeData amp11 = vmp1.getChildren().get(0);
    assertEquals("Lekadol 500 mg oral", ((MedicationSimpleDto)amp11.getData()).getName());
  }

  @Test
  public void testFindMedicationsInpatientFormularyFilter2()
  {
    Mockito
        .when(medicationsValueHolderProvider.getValidMedicationDataDtos(any(DateTime.class)))
        .thenReturn(new ArrayList<>(getMedicationsMap().values()));

    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications(
            "para",
            true,
            EnumSet.of(MedicationFinderFilterEnum.INPATIENT_PRESCRIPTION, MedicationFinderFilterEnum.FORMULARY),
            new DateTime(2018, 1, 1, 1, 0),
            new Locale("en"));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(2, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    final TreeNodeData vmp2 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());

    assertEquals(1, vmp1.getChildren().size());
    assertEquals(1, vmp2.getChildren().size());

    final TreeNodeData amp11 = vmp1.getChildren().get(0);
    assertEquals("Lekadol 500 mg oral", ((MedicationSimpleDto)amp11.getData()).getName());
  }

  @Test
  public void testFindMedicationsInpatientFormularyCareProviderFilter2()
  {
    Mockito
        .when(medicationsValueHolderProvider.getValidMedicationDataDtos(any(DateTime.class)))
        .thenReturn(new ArrayList<>(getMedicationsMap().values()));

    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications(
            "para",
            true,
            EnumSet.of(MedicationFinderFilterEnum.INPATIENT_PRESCRIPTION, MedicationFinderFilterEnum.FORMULARY),
            new DateTime(2018, 1, 1, 1, 0),
            new Locale("en"));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(2, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(1, vmp1.getChildren().size());
  }

  @Test
  public void testFindMedicationsInpatientMentalHealthFilter()
  {
    Mockito
        .when(medicationsValueHolderProvider.getValidMedicationDataDtos(any(DateTime.class)))
        .thenReturn(new ArrayList<>(getMedicationsMap().values()));

    final List<TreeNodeData> searchResult =
        medicationsFinder.findMedications(
            "para",
            true,
            EnumSet.of(MedicationFinderFilterEnum.MENTAL_HEALTH),
            new DateTime(2018, 1, 1, 1, 0),
            new Locale("en"));

    assertEquals(1, searchResult.size());
    final TreeNodeData vtm = searchResult.get(0);
    assertEquals("Paracetamol", ((MedicationSimpleDto)vtm.getData()).getName());
    assertEquals(1, vtm.getChildren().size());

    final TreeNodeData vmp1 = vtm.getChildren().get(0);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals(1, vmp1.getChildren().size());

    final TreeNodeData amp11 = vmp1.getChildren().get(0);
    assertEquals("Daleron 500 mg oral", ((MedicationSimpleDto)amp11.getData()).getName());
  }

  @Test
  public void testFindSimilarMedications()
  {
    final Set<Long> similarIds = Sets.newHashSet(11L, 12L, 111L, 112L, 121L, 122L);
    Mockito
        .when(medicationsValueHolderProvider.findSimilarMedicationDataDtos(anyLong(), any()))
        .thenReturn(getMedicationsMap().values()
                        .stream()
                        .filter(m -> similarIds.contains(m.getMedication().getId()))
                        .collect(Collectors.toList()));

    final List<TreeNodeData> searchResult = medicationsFinder.findSimilarMedications(
        11L,
        new DateTime(),
        new Locale("en"));

    assertEquals(2L, searchResult.size());
    final TreeNodeData vmp1 = searchResult.get(0);
    final TreeNodeData vmp2 = searchResult.get(1);
    assertEquals("Paracetamol 500 mg oral", ((MedicationSimpleDto)vmp1.getData()).getName());
    assertEquals("Paracetamol 250 mg oral", ((MedicationSimpleDto)vmp2.getData()).getName());

    assertEquals(2L, vmp1.getChildren().size());
    assertTrue(vmp1.getChildren().stream().anyMatch(m -> "Daleron 500 mg oral".equals(((MedicationSimpleDto)m.getData()).getName())));
    assertTrue(vmp1.getChildren().stream().anyMatch(m -> "Lekadol 500 mg oral".equals(((MedicationSimpleDto)m.getData()).getName())));

    assertEquals(2L, vmp2.getChildren().size());
    assertTrue(vmp2.getChildren().stream().anyMatch(m -> "Lekadol 250 mg oral".equals(((MedicationSimpleDto)m.getData()).getName())));
    assertTrue(vmp2.getChildren().stream().anyMatch(m -> "Daleron 250 mg oral".equals(((MedicationSimpleDto)m.getData()).getName())));
  }

  @Test
  public void testFindMedicationProductsProductBasedMedication()
  {
    final MedicationDto medication1 = new MedicationDto();
    medication1.setName("Medication1");
    final MedicationDto medication2 = new MedicationDto();
    medication2.setName("Medication2");

    Mockito
        .when(medicationsValueHolderProvider.isProductBasedMedication(anyLong()))
        .thenReturn(true);

    Mockito
        .when(medicationsValueHolderProvider.findSimilarMedications(anyLong(), any()))
        .thenReturn(Lists.newArrayList(medication1, medication2));

    final List<MedicationDto> searchResult =
        medicationsFinder.findMedicationProducts(111L, Collections.singletonList(10L), null, new DateTime());

    assertEquals(2L, searchResult.size());
    assertEquals("Medication1", searchResult.get(0).getName());
    assertEquals("Medication2", searchResult.get(1).getName());
  }

  @Test
  public void testFindMedicationProductsNonProductBasedMedication()
  {
    final MedicationDto medication1 = new MedicationDto();
    medication1.setName("Medication1");
    final MedicationDto medication2 = new MedicationDto();
    medication2.setName("Medication2");

    final DateTime when = new DateTime();
    Mockito
        .when(medicationsValueHolderProvider.isProductBasedMedication(anyLong()))
        .thenReturn(false);

    Mockito
        .when(medicationsValueHolderProvider.getMedicationChildProducts(111L, Collections.singletonList(10L), null, when))
        .thenReturn(Lists.newArrayList(medication1, medication2));

    final List<MedicationDto> searchResult =
        medicationsFinder.findMedicationProducts(111L, Collections.singletonList(10L), null, when);

    assertEquals(2L, searchResult.size());
    assertEquals("Medication1", searchResult.get(0).getName());
    assertEquals("Medication2", searchResult.get(1).getName());
  }

  private Map<Long, MedicationDataDto> getMedicationsMap()
  {
    final Map<Long, MedicationDataDto> medicationsMap = new LinkedHashMap<>();
    medicationsMap.put(
        1L,
        getMedicationDataDto(
            1L,
            "9",
            null,
            null,
            "Paracetamol",
            "generic 2",
            MedicationLevelEnum.VTM,
            true,
            true,
            true,
            true));
    medicationsMap.put(
        11L,
        getMedicationDataDto(
            11L,
            "9",
            "91",
            null,
            "Paracetamol 500 mg oral",
            "generic",
            MedicationLevelEnum.VMP,
            true,
            true,
            true,
            true));
    final MedicationDataDto lekadol500 = getMedicationDataDto(
        111L,
        "9",
        "91",
        "911",
        "Lekadol 500 mg oral",
        "generic",
        MedicationLevelEnum.AMP,
        true,
        false,
        true,
        false);
    medicationsMap.put(111L, lekadol500);
    medicationsMap.put(
        112L,
        getMedicationDataDto(
            112L,
            "9",
            "91",
            "912",
            "Daleron 500 mg oral",
            "generic",
            MedicationLevelEnum.AMP,
            false,
            true,
            true,
            true));
    medicationsMap.put(
        12L,
        getMedicationDataDto(
            12L,
            "9",
            "92",
            null,
            "Paracetamol 250 mg oral",
            "generic",
            MedicationLevelEnum.VMP,
            true,
            true,
            true,
            false));
    medicationsMap.put(
        121L,
        getMedicationDataDto(
            121L,
            "9",
            "92",
            "921",
            "Lekadol 250 mg oral",
            "generic",
            MedicationLevelEnum.AMP,
            true,
            false,
            true,
            false));
    medicationsMap.put(
        12123L,
        getMedicationDataDto(
            12123L,
            "9",
            "92",
            "123123",
            "Lekadol 250 mg oral not FORMULARY",
            "generic",
            MedicationLevelEnum.AMP,
            true,
            false,
            false,
            false));
    medicationsMap.put(
        122L,
        getMedicationDataDto(
            122L,
            "9",
            "92",
            "922",
            "Daleron 250 mg oral",
            "generic",
            MedicationLevelEnum.AMP,
            false,
            true,
            true,
            false));

    return medicationsMap;
  }

  private MedicationDataDto getMedicationDataDto(
      final long id,
      final String vtmId,
      final String vmpId,
      final String ampId,
      final String name,
      final String genericName,
      final MedicationLevelEnum vtm,
      final boolean inpatientMedication,
      final boolean outpatientMedication,
      final boolean formulary,
      final boolean mentalHealth)
  {
    final MedicationDataDto dataDto = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto();
    dataDto.setMedication(medication);
    medication.setId(id);
    dataDto.setVtmId(vtmId);
    dataDto.setVmpId(vmpId);
    dataDto.setAmpId(ampId);
    medication.setName(name);
    medication.setGenericName(genericName);
    dataDto.setMedicationLevel(vtm);
    dataDto.setInpatient(inpatientMedication);
    dataDto.setOutpatient(outpatientMedication);
    dataDto.setFormulary(formulary);

    if (mentalHealth)
    {
      dataDto.setProperty(new MedicationPropertyDto(id, MedicationPropertyType.MENTAL_HEALTH_DRUG, "mental health"));
    }
    dataDto.setValidFrom(new DateTime(2016, 1, 1, 1, 0));
    dataDto.setValidTo(new DateTime(2020, 1, 1, 1, 0));
    return dataDto;
  }

  private List<TreeNodeData> getMedicationTree()
  {
    //noinspection TooBroadScope
    final List<TreeNodeData> medications = new ArrayList<>();

    final TreeNodeData dto1 = new TreeNodeData();
    dto1.setTitle("Paracetamol");
    dto1.setKey("1");
    dto1.setData(new MedicationSimpleDto());
    medications.add(dto1);

    final TreeNodeData dto11 = new TreeNodeData();
    dto11.setTitle("Paracetamol 500 mg oral");
    dto11.setKey("11");
    dto11.setData(new MedicationSimpleDto());
    dto1.getChildren().add(dto11);

    final TreeNodeData dto111 = new TreeNodeData();
    dto111.setTitle("Lekadol 500 mg oral");
    dto111.setKey("111");
    dto111.setData(new MedicationSimpleDto());
    dto11.getChildren().add(dto111);

    final TreeNodeData dto112 = new TreeNodeData();
    dto112.setTitle("Daleron 500 mg oral");
    dto112.setKey("112");
    dto112.setData(new MedicationSimpleDto());
    dto11.getChildren().add(dto112);

    final TreeNodeData dto12 = new TreeNodeData();
    dto12.setTitle("Paracetamol 250 mg oral");
    dto12.setKey("12");
    dto12.setData(new MedicationSimpleDto());
    dto1.getChildren().add(dto12);

    final TreeNodeData dto121 = new TreeNodeData();
    dto121.setTitle("Lekadol 250 mg oral");
    dto121.setKey("121");
    dto121.setData(new MedicationSimpleDto());
    dto12.getChildren().add(dto121);

    final TreeNodeData dto122 = new TreeNodeData();
    dto122.setTitle("Daleron 250 mg oral");
    dto122.setKey("122");
    final MedicationSimpleDto data122 = new MedicationSimpleDto();
    data122.setTradeFamily("TradeA");
    dto122.setData(data122);
    dto12.getChildren().add(dto122);

    return medications;
  }
}
