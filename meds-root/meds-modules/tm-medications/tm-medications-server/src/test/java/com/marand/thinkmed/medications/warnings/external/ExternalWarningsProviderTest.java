package com.marand.thinkmed.medications.warnings.external;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.service.dto.WarningScreenMedicationDto;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import com.marand.thinkmed.medications.warnings.TherapyWarningsUtils;
import com.marand.thinkmed.medications.warnings.WarningTestUtils;
import com.marand.thinkmed.medications.warnings.WarningsPlugin;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
public class ExternalWarningsProviderTest
{
  @InjectMocks
  private final ExternalWarningsProvider externalWarningsProvider = new ExternalWarningsProvider();

  @InjectMocks
  private final TherapyWarningsUtils therapyWarningsUtils = new TherapyWarningsUtils();

  @Mock
  private MedicationsDao medicationsDao;

  @Mock
  private WarningsPlugin warningsPlugin;

  @Mock
  private MedsProperties medsProperties;

  @Mock
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  @Before
  public void init()
  {
    externalWarningsProvider.setTherapyWarningsUtils(therapyWarningsUtils);
    Mockito.when(medsProperties.getAllergyCodeTranslationRequired()).thenReturn(true);
    Mockito.when(medsProperties.getDiseaseCodeTranslationRequired()).thenReturn(true);

    final MedicationDataDto medication = new MedicationDataDto();
    medication.setMedicationLevel(MedicationLevelEnum.VTM);
    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(anyLong()))
        .thenReturn(medication);
  }

  @Test
  public void getExternalWarnings()
  {
    Mockito
        .when(warningsPlugin.getExternalSystemName())
        .thenReturn("FDB");

    final List<MedicationsWarningDto> warningsFromExternalSource = Arrays.asList(
        new MedicationsWarningDto("a interaction high override", WarningSeverity.HIGH_OVERRIDE, WarningType.INTERACTION, null),
        new MedicationsWarningDto("b interaction high override", WarningSeverity.HIGH_OVERRIDE, WarningType.INTERACTION, null),
        new MedicationsWarningDto("a interaction high", WarningSeverity.HIGH, WarningType.INTERACTION, null),
        new MedicationsWarningDto("b interaction high", WarningSeverity.HIGH, WarningType.INTERACTION, null),
        new MedicationsWarningDto("patient other", WarningSeverity.OTHER, WarningType.PATIENT_CHECK, null),
        new MedicationsWarningDto("duplicate other", WarningSeverity.OTHER, WarningType.DUPLICATE, null));

    Mockito
        .when(warningsPlugin.findMedicationWarnings(any(), anyDouble(), anyDouble(), any(), anyList(), anyList(), anyList(), any()))
        .thenReturn(warningsFromExternalSource);


    final Map<Long, String> idToExternalIdMap = new HashMap<>();
    idToExternalIdMap.put(1L, "ex100");
    idToExternalIdMap.put(2L, "ex200");
    idToExternalIdMap.put(3L, "ex300");
    idToExternalIdMap.put(4L, "ex400");

    Mockito
        .when(medicationsDao.getMedicationsExternalIds(anyString(), anySet()))
        .thenReturn(idToExternalIdMap);

    final Map<String, String> conditionsExternalIdsMap = new HashMap<>();
    conditionsExternalIdsMap.put("d100", "exd100");
    conditionsExternalIdsMap.put("d200", "exd200");
    conditionsExternalIdsMap.put("a100", "exa200");
    conditionsExternalIdsMap.put("a200", "exa200");

    Mockito
        .when(medicationsDao.getMedicationExternalValues(anyString(), any(), anySet()))
        .thenReturn(conditionsExternalIdsMap);

    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(1L))
        .thenReturn(new MedicationDataDto());

    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(2L))
        .thenReturn(new MedicationDataDto());

    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(3L))
        .thenReturn(new MedicationDataDto());

    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(4L))
        .thenReturn(new MedicationDataDto());

    final List<TherapyDto> therapies = Arrays.asList(
        WarningTestUtils.buildTherapy(1L, "Aspirin", 1L),
        WarningTestUtils.buildTherapy(2L, "Lekadol", 2L),
        WarningTestUtils.buildTherapy(3L, "Coldrex", 3L),
        WarningTestUtils.buildTherapy(4L, "nalgesin", 4L));

    final List<IdNameDto> diseases = Arrays.asList(
        new IdNameDto("d100", "measels"),
        new IdNameDto("d200", "flu"));

    final List<IdNameDto> allergies = Arrays.asList(
        new IdNameDto("a100", "peanuts"),
        new IdNameDto("a200", "aspirin"));

    final List<MedicationsWarningDto> warningsResult = externalWarningsProvider.getExternalWarnings(
        new DateTime(2017, 1, 1, 1, 0, 1),
        2.0,
        1.0,
        Gender.MALE,
        diseases,
        allergies,
        therapies,
        Collections.emptyList(),
        new DateTime(2017, 2, 4, 0, 0));

    assertThat(warningsResult).hasSize(6);
    assertThat(warningsResult.get(0).getSeverity()).isEqualTo(WarningSeverity.HIGH_OVERRIDE);
    assertThat(warningsResult.get(0).getType()).isEqualTo(WarningType.INTERACTION);
    assertThat(warningsResult.get(0).getDescription()).isEqualTo("a interaction high override");

    assertThat(warningsResult.get(1).getSeverity()).isEqualTo(WarningSeverity.HIGH_OVERRIDE);
    assertThat(warningsResult.get(1).getType()).isEqualTo(WarningType.INTERACTION);
    assertThat(warningsResult.get(1).getDescription()).isEqualTo("b interaction high override");

    assertThat(warningsResult.get(2).getSeverity()).isEqualTo(WarningSeverity.HIGH);
    assertThat(warningsResult.get(2).getType()).isEqualTo(WarningType.INTERACTION);
    assertThat(warningsResult.get(2).getDescription()).isEqualTo("a interaction high");

    assertThat(warningsResult.get(3).getSeverity()).isEqualTo(WarningSeverity.HIGH);
    assertThat(warningsResult.get(3).getType()).isEqualTo(WarningType.INTERACTION);
    assertThat(warningsResult.get(3).getDescription()).isEqualTo("b interaction high");

    assertThat(warningsResult.get(4).getSeverity()).isEqualTo(WarningSeverity.OTHER);
    assertThat(warningsResult.get(4).getType()).isEqualTo(WarningType.PATIENT_CHECK);

    assertThat(warningsResult.get(5).getSeverity()).isEqualTo(WarningSeverity.OTHER);
    assertThat(warningsResult.get(5).getType()).isEqualTo(WarningType.DUPLICATE);
  }

  @Test
  public void createWarningsForUnmatchedConditionsTest()
  {
    final Opt<MedicationsWarningDto> warnings = externalWarningsProvider.createWarningForUnmatchedConditions(
        true,
        Arrays.asList(new IdNameDto("1", "Aspirin"), new IdNameDto("2", "Warfarin")),
        "Allergies unmatched:");

    assertEquals(Dictionary.getEntry("Allergies unmatched:") + ": " + "Aspirin, Warfarin!", warnings.get().getDescription());
  }

  @Test
  public void getMappedAndUnmatchedConditionsTest()
  {
    final Map<String, String> mappedIds = new HashMap<>();
    mappedIds.put("1", "1000");
    mappedIds.put("2", "2000");

    Mockito
        .when(medicationsDao.getMedicationExternalValues(anyString(), any(MedicationsExternalValueType.class), anySet()))
        .thenReturn(mappedIds);

    final List<IdNameDto> inputConditionsList = Lists.newArrayList(
        new IdNameDto("1", "Aspirin"),
        new IdNameDto("2", "Warfarin"),
        new IdNameDto("3", "Paracetamol"));

    final ExternalWarningsProvider.MappedAndUnmatchedConditionLists result = externalWarningsProvider.getMappedAndUnmatchedConditions(
        "FDB",
        MedicationsExternalValueType.ALLERGY,
        inputConditionsList);

    final Set<IdNameDto> expectedMappedConditions = Sets.newSet(
        new IdNameDto("1000", "Aspirin"),
        new IdNameDto("2000", "Warfarin")
    );

    final Set<IdNameDto> expectedUnmatchedConditions = Sets.newSet(
        new IdNameDto("3", "Paracetamol")
    );

    assertEquals(expectedMappedConditions.size(), result.getMappedConditions().size());

    expectedMappedConditions.forEach(m -> assertTrue(
        result.getMappedConditions()
            .stream()
            .anyMatch(c -> c.getId().equals(m.getId()) && c.getName().equals(m.getName()))));

    expectedUnmatchedConditions.forEach(m -> assertTrue(
        result.getUnmatchedConditions()
            .stream()
            .anyMatch(c -> c.getId().equals(m.getId()) && c.getName().equals(m.getName()))));
  }

  @Test
  public void getUnmatchedMedicationsWarnings()
  {
    final List<WarningScreenMedicationDto> screenMedications = Arrays.asList(
        new WarningScreenMedicationDto(1L, "name 1", "1", false, 1L, "1E"),
        new WarningScreenMedicationDto(2L, "name 2", null, true, 1L, "1E"),
        new WarningScreenMedicationDto(3L, "name 3", null, true, 1L, "1E"),
        new WarningScreenMedicationDto(4L, "name 4", "1", false, 1L, "1E")
    );

    final List<MedicationsWarningDto> unmatchedWarnings = externalWarningsProvider.getUnmatchedMedicationsWarnings(screenMedications);

    assertEquals(1, unmatchedWarnings.size());
    assertTrue(unmatchedWarnings.get(0).getDescription().contains("name 2, name 3"));
  }

  @Test
  public void getNoUnmatchedMedicationsWarnings()
  {
    final List<WarningScreenMedicationDto> screenMedications = Arrays.asList(
        new WarningScreenMedicationDto(1L, "name 1", "1", false, 1L, "1E"),
        new WarningScreenMedicationDto(2L, "name 2", "1", true, 1L, "1E"),
        new WarningScreenMedicationDto(3L, "name 3", "1", true, 1L, "1E"),
        new WarningScreenMedicationDto(4L, "name 4", "1", false, 1L, "1E")
    );

    final List<MedicationsWarningDto> unmatchedWarnings = externalWarningsProvider.getUnmatchedMedicationsWarnings(screenMedications);

    assertEquals(0, unmatchedWarnings.size());
  }

  @Test
  public void testIgnoreWarningDuplicateIgnore()
  {
    final MedicationsWarningDto warning = new MedicationsWarningDto();
    warning.setType(WarningType.DUPLICATE);
    warning.getMedications().add(new NamedExternalDto("1", "Paracetamol 100mg tablet"));
    warning.getMedications().add(new NamedExternalDto("2", "Paracetamol 500mg capsule"));

    final MedicationDataDto medication1 = new MedicationDataDto();
    medication1.getProperties()
        .add(new MedicationPropertyDto(
            10L,
            MedicationPropertyType.IGNORE_DUPLICATION_WARNINGS,
            "Ignore duplicatiom warnings"));
    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(1L))
        .thenReturn(medication1);
    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(2L))
        .thenReturn(new MedicationDataDto());

    final boolean ignoreWarning = externalWarningsProvider.ignoreWarning(warning);
    assertTrue(ignoreWarning);
  }

  @Test
  public void testIgnoreWarningDuplicateDontIgnore()
  {
    final MedicationsWarningDto warning = new MedicationsWarningDto();
    warning.setType(WarningType.DUPLICATE);
    warning.getMedications().add(new NamedExternalDto("1", "Paracetamol 100mg tablet"));
    warning.getMedications().add(new NamedExternalDto("2", "Paracetamol 500mg capsule"));

    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(1L))
        .thenReturn(new MedicationDataDto());
    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(2L))
        .thenReturn(new MedicationDataDto());

    final boolean ignoreWarning = externalWarningsProvider.ignoreWarning(warning);
    assertFalse(ignoreWarning);
  }

  @Test
  public void testIgnoreWarningInteraction()
  {
    final MedicationsWarningDto warning = new MedicationsWarningDto();
    warning.setType(WarningType.INTERACTION);
    warning.getMedications().add(new NamedExternalDto("1", "Paracetamol 100mg tablet"));
    warning.getMedications().add(new NamedExternalDto("2", "Paracetamol 500mg capsule"));

    final MedicationDataDto medication1 = new MedicationDataDto();
    medication1.getProperties()
        .add(new MedicationPropertyDto(
            10L,
            MedicationPropertyType.IGNORE_DUPLICATION_WARNINGS,
            "Ignore duplicatiom warnings"));
    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(1L))
        .thenReturn(medication1);
    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(2L))
        .thenReturn(new MedicationDataDto());

    final boolean ignoreWarning = externalWarningsProvider.ignoreWarning(warning);
    assertFalse(ignoreWarning);
  }
}
