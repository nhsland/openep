package com.marand.thinkmed.fdb.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.dictionary.MafDictionary;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.fdb.dto.FdbAlertRelevanceTypeEnum;
import com.marand.thinkmed.fdb.dto.FdbDrug;
import com.marand.thinkmed.fdb.dto.FdbDrugSensitivityWarningDto;
import com.marand.thinkmed.fdb.dto.FdbGenderEnum;
import com.marand.thinkmed.fdb.dto.FdbNameValue;
import com.marand.thinkmed.fdb.dto.FdbPatientCheckTriggersDto;
import com.marand.thinkmed.fdb.dto.FdbPatientChecksWarningDto;
import com.marand.thinkmed.fdb.dto.FdbScreeningDto;
import com.marand.thinkmed.fdb.dto.FdbScreeningResultDto;
import com.marand.thinkmed.fdb.dto.FdbTerminologyWithConceptDto;
import com.marand.thinkmed.fdb.dto.FdbWarningDto;
import com.marand.thinkmed.fdb.dto.FdbWarningDuplicateDto;
import com.marand.thinkmed.fdb.rest.FdbRestService;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.WarningScreenMedicationDto;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.assertj.core.api.Assertions.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Vid Kumše
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class FdbServiceImplTest
{
  @Mock
  private MedsProperties medsProperties;

  @Mock
  private FdbRestService fdbRestService;

  @InjectMocks
  private final FdbServiceImpl fdbService = new FdbServiceImpl();

  @Before
  public void setup()
  {
    Dictionary.setDelegate(new MafDictionary("Dictionary", "", new Locale("en")));
  }

  @Test
  public void testBuildFdbScreeningDto()
  {
    final List<WarningScreenMedicationDto> medicationSummaries = new ArrayList<>();
    medicationSummaries.add(getMedicationForWarningsSearchDto("Aspirin", 343070039, true, 12L, "3245667", true, null));
    medicationSummaries.add(getMedicationForWarningsSearchDto("Aspirin", 343070039, false, 13L, "3245667", true, null));
    medicationSummaries.add(getMedicationForWarningsSearchDto("Paracetamol", 400, false, 13L, "45559", false, null));
    medicationSummaries.add(getMedicationForWarningsSearchDto("Dopamine", 344490, false, 14L, "2345", false, "233"));
    medicationSummaries.add(getMedicationForWarningsSearchDto("Dopamine", 344490, false, 14L, "2345", false, "233"));
    medicationSummaries.add(getMedicationForWarningsSearchDto("Dopamine", 344490, true, 14L, "2345", false, "233"));

    Mockito
        .when(fdbRestService.getOrderableMedicine(Matchers.anyString(), Matchers.anyString(), Matchers.anyString()))
        .thenReturn("{\n" +
                        "  \"OrderableMed\": {\n" +
                        "   \"SingleId\": \"15535\"\n" +
                        "  }\n" +
                        "}");

    final List<IdNameDto> diseaseTypeCodes = Lists.newArrayList(new IdNameDto("1298", "Flu"));
    final List<IdNameDto> allergies = Lists.newArrayList(new IdNameDto("2389", "Bee sting allergy"));
    final Map<String, Long> orderableMedIdToInternalIdMap = Maps.newHashMap();

    final FdbScreeningDto fdbScreeningDto = fdbService.buildFdbScreeningDto(
        new DateTime(2010, 4, 28, 7 , 22),
        Gender.MALE,
        diseaseTypeCodes,
        medicationSummaries,
        allergies,
        new DateTime(2015, 10, 2, 3, 4),
        orderableMedIdToInternalIdMap);

    assertEquals(1983, (long) fdbScreeningDto.getPatientInformation().getAge());
    assertThat(fdbScreeningDto.getProspectiveDrugs()).hasSize(2);
    assertThat(fdbScreeningDto.getCurrentDrugs()).hasSize(4);

    assertThat(fdbScreeningDto.getProspectiveDrugs())
        .extracting("id","name", "terminology", "conceptType")
        .contains(tuple("3245667","Aspirin", "SNOMEDCT", "Product"),
                  tuple("15535", "Dopamine", "SNOMEDCT", "OrderableMed"));

    assertThat(fdbScreeningDto.getCurrentDrugs())
        .extracting("id","name", "terminology", "conceptType")
        .contains(tuple("3245667", "Aspirin", "SNOMEDCT", "Product"),
                  tuple("45559", "Paracetamol", "SNOMEDCT", "Drug"),
                  tuple("15535", "Dopamine", "SNOMEDCT", "OrderableMed"),
                  tuple("15535", "Dopamine", "SNOMEDCT", "OrderableMed"));

    assertEquals(1, orderableMedIdToInternalIdMap.size());
  }

  @Test
  public void testBuildFdbScreeningDtoNoData()
  {
    final FdbScreeningDto fdbScreeningDto = fdbService.buildFdbScreeningDto(
        new DateTime(),
        Gender.MALE,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        new DateTime(),
        Collections.emptyMap());

    assertTrue(fdbScreeningDto.getAllergens().isEmpty());
    assertTrue(fdbScreeningDto.getProspectiveDrugs().isEmpty());
    assertTrue(fdbScreeningDto.getCurrentDrugs().isEmpty());
    assertTrue(fdbScreeningDto.getConditions().isEmpty());
  }

  @Test
  public void testMapWarningsNoData()
  {
    final Map<String, String> idsMapping = new HashMap<>();
    idsMapping.put("1", "100");
    idsMapping.put("2", "200");
    idsMapping.put("3", "300");
    idsMapping.put("4", "400");
    idsMapping.put("5", "500");
    idsMapping.put("6", "600");
    idsMapping.put("7", "700");
    idsMapping.put("8", "800");
    idsMapping.put("9", "900");
    idsMapping.put("10", "1000");
    idsMapping.put("11", "1100");
    idsMapping.put("12", "1200");

    final List<MedicationsWarningDto> medicationsWarningDtos = fdbService.mapWarnings(
        new FdbScreeningResultDto(),
        idsMapping,
        Collections.emptyMap());

    assertTrue(medicationsWarningDtos.isEmpty());
  }

  @Test
  public void testMapMedicationWarnings()
  {
    final FdbWarningDuplicateDto drugDoubling = new FdbWarningDuplicateDto();
    drugDoubling.setFullAlertMessage("1");
    drugDoubling.setPrimaryDrug(new FdbDrug("Aspirin", 1L, new FdbNameValue("SNOMEDCT", 2L)));
    drugDoubling.setSecondaryDrug(new FdbDrug("Aspirin", 1L, new FdbNameValue("SNOMEDCT", 2L)));
    final List<FdbWarningDuplicateDto> drugDoublings = Lists.newArrayList(drugDoubling);

    final FdbDrugSensitivityWarningDto sensitivity = new FdbDrugSensitivityWarningDto();
    sensitivity.setFullAlertMessage("2");
    sensitivity.setDrug(new FdbDrug("paracetamol", 2L, new FdbNameValue("SNOMEDCT", 2L)));
    final List<FdbDrugSensitivityWarningDto> sensitivities = Lists.newArrayList(sensitivity);

    final FdbPatientChecksWarningDto precaution = new FdbPatientChecksWarningDto();
    precaution.setConditionAlertSeverity(new FdbNameValue("Precaution", 2L));
    precaution.setDrug(new FdbDrug("Dopamin", 3L, new FdbNameValue("SNOMEDCT", 2L)));
    precaution.setFullAlertMessage("3");
    precaution.setAlertRelevanceType(new FdbNameValue(
        FdbAlertRelevanceTypeEnum.RELATED.getName(),
        FdbAlertRelevanceTypeEnum.RELATED.getKey()));
    final List<FdbPatientChecksWarningDto> patientChecks = Lists.newArrayList(precaution);

    final FdbPatientChecksWarningDto diseaseRelatedContraindication = new FdbPatientChecksWarningDto();
    diseaseRelatedContraindication.setConditionAlertSeverity(new FdbNameValue("Contraindication", 1L));
    diseaseRelatedContraindication.setDrug(new FdbDrug("Glukozamin", 4L, new FdbNameValue("SNOMEDCT", 2L)));
    diseaseRelatedContraindication.setFullAlertMessage("4");
    diseaseRelatedContraindication.setAlertRelevanceType(new FdbNameValue(
        FdbAlertRelevanceTypeEnum.RELATED.getName(),
        FdbAlertRelevanceTypeEnum.RELATED.getKey()));
    patientChecks.add(diseaseRelatedContraindication);

    final FdbPatientChecksWarningDto genderContraindication = new FdbPatientChecksWarningDto();
    genderContraindication.setConditionAlertSeverity(new FdbNameValue("Contraindication", 1L));
    genderContraindication.setDrug(new FdbDrug("Glukozamin", 4L, new FdbNameValue("SNOMEDCT", 2L)));
    genderContraindication.setFullAlertMessage("genderContraindication");
    genderContraindication.setAlertRelevanceType(new FdbNameValue(
        FdbAlertRelevanceTypeEnum.UNRELATED.getName(),
        FdbAlertRelevanceTypeEnum.UNRELATED.getKey()));
    final FdbPatientCheckTriggersDto patientCheckTriggers = new FdbPatientCheckTriggersDto();
    patientCheckTriggers.setGender(new FdbNameValue(FdbGenderEnum.FEMALE.name(), FdbGenderEnum.FEMALE.getKey()));
    genderContraindication.setPatientCheckTriggers(patientCheckTriggers);
    patientChecks.add(genderContraindication);

    final FdbWarningDto lowRisk = new FdbWarningDto();
    lowRisk.setFullAlertMessage("5");
    lowRisk.setPrimaryDrug(new FdbDrug("Lekadol", 5L, new FdbNameValue("SNOMEDCT", 2L)));
    lowRisk.setSecondaryDrug(new FdbDrug("Antibiotik", 6L, new FdbNameValue("SNOMEDCT", 2L)));
    lowRisk.setAlertSeverity(new FdbNameValue("LowRisk", 1L));
    final FdbWarningDto moderateRisk = new FdbWarningDto();
    moderateRisk.setPrimaryDrug(new FdbDrug("Morphine", 7L, new FdbNameValue("SNOMEDCT", 2L)));
    moderateRisk.setSecondaryDrug(new FdbDrug("Claritine", 8L, new FdbNameValue("SNOMEDCT", 2L)));
    moderateRisk.setFullAlertMessage("6");
    moderateRisk.setAlertSeverity(new FdbNameValue("ModerateRisk", 2L));
    final FdbWarningDto significantRisk = new FdbWarningDto();
    significantRisk.setPrimaryDrug(new FdbDrug("Kerozine", 9L, new FdbNameValue("SNOMEDCT", 2L)));
    significantRisk.setSecondaryDrug(new FdbDrug("Roaccutane", 10L, new FdbNameValue("SNOMEDCT", 2L)));
    significantRisk.setAlertSeverity(new FdbNameValue("SignificantRisk", 3L));
    significantRisk.setFullAlertMessage("7");
    final FdbWarningDto highRisk = new FdbWarningDto();
    highRisk.setAlertSeverity(new FdbNameValue("HighRisk", 4L));
    highRisk.setPrimaryDrug(new FdbDrug("NaCl", 11L, new FdbNameValue("SNOMEDCT", 2L)));
    highRisk.setSecondaryDrug(new FdbDrug("Glukoza", 12L,new FdbNameValue("SNOMEDCT", 2L)));
    highRisk.setFullAlertMessage("8");
    final List<FdbWarningDto> interactions = Lists.newArrayList(lowRisk);
    interactions.add(moderateRisk);
    interactions.add(significantRisk);
    interactions.add(highRisk);

    final FdbScreeningResultDto expectation = new FdbScreeningResultDto();
    expectation.setDrugDoublings(drugDoublings);
    expectation.setDrugSensitivities(sensitivities);
    expectation.setPatientChecks(patientChecks);
    expectation.setDrugInteractions(interactions);

    final Map<String, String> idsMapping = new HashMap<>();
    idsMapping.put("1", "100");
    idsMapping.put("2", "200");
    idsMapping.put("3", "300");
    idsMapping.put("4", "400");
    idsMapping.put("5", "500");
    idsMapping.put("6", "600");
    idsMapping.put("7", "700");
    idsMapping.put("8", "800");
    idsMapping.put("9", "900");
    idsMapping.put("10", "1000");
    idsMapping.put("11", "1100");
    idsMapping.put("12", "1200");

    final List<MedicationsWarningDto> result = fdbService.mapWarnings(expectation, idsMapping, Collections.emptyMap());

    assertEquals("2", result.get(0).getDescription());
    assertEquals("4", result.get(1).getDescription());
    assertEquals("genderContraindication", result.get(2).getDescription());
    assertEquals("8", result.get(3).getDescription());
    assertEquals("1", result.get(4).getDescription());
    assertEquals("7", result.get(5).getDescription());
    assertEquals("3", result.get(6).getDescription());
    assertEquals("6", result.get(7).getDescription());
    assertEquals("5", result.get(8).getDescription());

    assertEquals(WarningType.ALLERGY, result.get(0).getType());
    assertEquals(WarningType.PATIENT_CHECK, result.get(1).getType());
    assertEquals(WarningType.PATIENT_CHECK, result.get(2).getType());
    assertEquals(WarningType.INTERACTION, result.get(3).getType());
    assertEquals(WarningType.DUPLICATE, result.get(4).getType());
    assertEquals(WarningType.INTERACTION, result.get(5).getType());
    assertEquals(WarningType.PATIENT_CHECK, result.get(6).getType());
    assertEquals(WarningType.INTERACTION, result.get(7).getType());
    assertEquals(WarningType.INTERACTION, result.get(8).getType());

    assertEquals(WarningSeverity.HIGH_OVERRIDE, result.get(0).getSeverity());
    assertEquals(WarningSeverity.HIGH_OVERRIDE, result.get(1).getSeverity());
    assertEquals(WarningSeverity.OTHER, result.get(2).getSeverity());
    assertEquals(WarningSeverity.HIGH, result.get(3).getSeverity());
    assertEquals(WarningSeverity.HIGH, result.get(4).getSeverity());
    assertEquals(WarningSeverity.OTHER, result.get(5).getSeverity());
    assertEquals(WarningSeverity.OTHER, result.get(6).getSeverity());
    assertEquals(WarningSeverity.OTHER, result.get(7).getSeverity());
    assertEquals(WarningSeverity.OTHER, result.get(8).getSeverity());
  }

  @Test
  public void createExternalToInternalMedicationMappingTest()
  {
    final WarningScreenMedicationDto aspirin = getMedicationForWarningsSearchDto(
        "Aspirin",
        1,
        true,
        2L,
        "ex100",
        true,
        null
    );

    final WarningScreenMedicationDto paracetamol = getMedicationForWarningsSearchDto(
        "Paracetamol",
        2,
        true,
        3L,
        "ex200",
        true,
        null
    );

    final WarningScreenMedicationDto linex = getMedicationForWarningsSearchDto(
        "linex",
        3,
        false,
        4L,
        "ex300",
        true,
        null
    );

    final List<WarningScreenMedicationDto> listOfmedications = new ArrayList<>();
    listOfmedications.add(aspirin);
    listOfmedications.add(paracetamol);
    listOfmedications.add(linex);

    final Map<String, String> externalToInternalmedicatioNMappinResult =
        fdbService.createExternalToInternalMedicationMapping(listOfmedications);

    assertEquals("1", externalToInternalmedicatioNMappinResult.get("ex100"));
    assertEquals("2", externalToInternalmedicatioNMappinResult.get("ex200"));
    assertEquals("3", externalToInternalmedicatioNMappinResult.get("ex300"));
  }

  @Test
  public void mapMedicationsTest()
  {
    final List<FdbDrug> warnings = Lists.newArrayList();
    final FdbDrug warningForAspirin = new FdbDrug("Aspirin + oral", 500L, new FdbNameValue("OrderableMed", 3L));
    warnings.add(warningForAspirin);

    final Map<String, String> externalToInternalMedicationIdMap = new HashMap<>();
    externalToInternalMedicationIdMap.put("100", "1");
    externalToInternalMedicationIdMap.put("400", "5");

    final Map<String, Long> orderableMedIdToInternalId = new HashMap<>();
    orderableMedIdToInternalId.put("500", 5L);

    final List<NamedExternalDto> result = fdbService.mapMedications(
        warnings,
        externalToInternalMedicationIdMap,
        orderableMedIdToInternalId);

    assertEquals("Aspirin + oral", result.get(0).getName());
    assertEquals("5", result.get(0).getId());
  }

  @Test
  public void mapPatientCheckWarningsTest()
  {
    final List<FdbPatientChecksWarningDto> fdbPatientCheckWarnings = Lists.newArrayList();

    final FdbPatientChecksWarningDto aspirinWarning = new FdbPatientChecksWarningDto();
    aspirinWarning.setAlertRelevanceType(new FdbNameValue("UnRelated", 3L));
    aspirinWarning.setConditionAlertSeverity(new FdbNameValue("Precaution", 2L));
    aspirinWarning.setDrug(new FdbDrug("Aspirin + oral", 500L, new FdbNameValue("OrderableMed", 3L)));
    aspirinWarning.setFullAlertMessage("aspirin warning");
    final FdbPatientCheckTriggersDto patientCheckTriggers1 = new FdbPatientCheckTriggersDto();
    patientCheckTriggers1.setAge(10L);
    aspirinWarning.setPatientCheckTriggers(patientCheckTriggers1);

    final FdbPatientChecksWarningDto furosemideWarning = new FdbPatientChecksWarningDto();
    furosemideWarning.setAlertRelevanceType(new FdbNameValue("unrelated", 3L));
    furosemideWarning.setConditionAlertSeverity(new FdbNameValue("Contraindication", 3L));
    furosemideWarning.setDrug(new FdbDrug("Furosemide", 3000L, new FdbNameValue("SNOMEDCT", 2L)));
    furosemideWarning.setFullAlertMessage("furosemide warning");
    final FdbPatientCheckTriggersDto patientCheckTriggers2 = new FdbPatientCheckTriggersDto();
    patientCheckTriggers2.setGender(new FdbNameValue("Unknown", 3L));
    furosemideWarning.setPatientCheckTriggers(patientCheckTriggers2);

    final FdbPatientChecksWarningDto paracetamolWarning = new FdbPatientChecksWarningDto();
    paracetamolWarning.setAlertRelevanceType(new FdbNameValue("unrelated", 3L));
    paracetamolWarning.setConditionAlertSeverity(new FdbNameValue("Precaution", 2L));
    paracetamolWarning.setDrug(new FdbDrug("Paracetamol", 200L, new FdbNameValue("OrderableMed", 3L)));
    paracetamolWarning.setFullAlertMessage("paracetamol warning");

    final FdbPatientChecksWarningDto aspirinIvWarning = new FdbPatientChecksWarningDto();
    aspirinIvWarning.setAlertRelevanceType(new FdbNameValue("UnRelated", 3L));
    aspirinIvWarning.setConditionAlertSeverity(new FdbNameValue("Precaution", 2L));
    aspirinIvWarning.setDrug(new FdbDrug("Aspirin + IV", 700L, new FdbNameValue("OrderableMed", 3L)));
    aspirinIvWarning.setFullAlertMessage("aspirin iv warning");

    fdbPatientCheckWarnings.add(aspirinWarning);
    fdbPatientCheckWarnings.add(furosemideWarning);
    fdbPatientCheckWarnings.add(paracetamolWarning);
    fdbPatientCheckWarnings.add(aspirinIvWarning);

    final Map<String, String> externalToInternalMedicationIdMap = new HashMap<>();
    externalToInternalMedicationIdMap.put("4000", "5");
    externalToInternalMedicationIdMap.put("3000", "3");
    externalToInternalMedicationIdMap.put("6000", "2");

    final Map<String, Long> orderableMedIdToInternalId = new HashMap<>();
    orderableMedIdToInternalId.put("500", 5L);
    orderableMedIdToInternalId.put("700", 5L);
    orderableMedIdToInternalId.put("200", 2L);

    final List<MedicationsWarningDto> result = fdbService.mapPatientCheckWarnings(
        fdbPatientCheckWarnings,
        externalToInternalMedicationIdMap,
        orderableMedIdToInternalId);

    assertEquals("aspirin warning", result.get(0).getDescription());
    assertEquals("Aspirin + oral", result.get(0).getMedications().get(0).getName());
    assertEquals("5", result.get(0).getMedications().get(0).getId());
    assertEquals("furosemide warning", result.get(1).getDescription());
    assertEquals("Furosemide", result.get(1).getMedications().get(0).getName());
    assertEquals("3", result.get(1).getMedications().get(0).getId());
    assertEquals("paracetamol warning", result.get(2).getDescription());
    assertEquals("Paracetamol", result.get(2).getMedications().get(0).getName());
    assertEquals("2", result.get(2).getMedications().get(0).getId());
  }

  @Test
  public void mapDrugForVMPTest()
  {
    final WarningScreenMedicationDto aspirinForWarning = getMedicationForWarningsSearchDto(
        "aspirin",
        1,
        true,
        1L,
        "100",
        true,
        null);

    final FdbTerminologyWithConceptDto result = fdbService.mapDrug(
        aspirinForWarning);

    assertEquals("Product", result.getConceptType());
    assertEquals("100", result.getId());
    assertEquals("aspirin", result.getName());
    assertEquals("SNOMEDCT", result.getTerminology());
  }

  @Test
  public void mapDrugForVTMTest()
  {
    final WarningScreenMedicationDto aspirinForWarning = getMedicationForWarningsSearchDto(
        "aspirin",
        1,
        true,
        1L,
        "100".toUpperCase(),
        false,
        null);
    aspirinForWarning.setRouteExternalId("11");

    Mockito
        .when(fdbRestService.getOrderableMedicine(Matchers.anyString(), Matchers.anyString(), Matchers.anyString()))
        .thenReturn("{\n" +
                        "  \"OrderableMed\": {\n" +
                        "   \"SingleId\": \"15535\"\n" +
                        "  }\n" +
                        "}");

    final FdbTerminologyWithConceptDto result = fdbService.mapDrug(
        aspirinForWarning);

    assertEquals("OrderableMed", result.getConceptType());
    assertEquals("15535", result.getId());
    assertEquals("aspirin", result.getName());
    assertEquals("SNOMEDCT", result.getTerminology());
  }

  @Test
  public void mapDrugForVTMNullableRouteExternalIdTest()
  {
    final WarningScreenMedicationDto aspirinForWarning = getMedicationForWarningsSearchDto(
        "aspirin",
        1,
        true,
        1L,
        "100".toUpperCase(),
        false,
        null);

    Mockito
        .when(fdbRestService.getOrderableMedicine(Matchers.anyString(), Matchers.anyString(), Matchers.anyString()))
        .thenReturn("{\n" +
                        "  \"OrderableMed\": {\n" +
                        "   \"SingleId\": \"15535\"\n" +
                        "  }\n" +
                        "}");

    final FdbTerminologyWithConceptDto result = fdbService.mapDrug(
        aspirinForWarning);

    assertEquals("Drug", result.getConceptType());
    assertEquals("100", result.getId());
    assertEquals("aspirin", result.getName());
    assertEquals("SNOMEDCT", result.getTerminology());
  }

  @Test
  public void mapDrugForVTMOrderableMedNotFoundTest()
  {
    final WarningScreenMedicationDto aspirinForWarning = getMedicationForWarningsSearchDto(
        "aspirin",
        1,
        true,
        1L,
        "100".toUpperCase(),
        false,
        null);
    aspirinForWarning.setRouteExternalId("11");

    Mockito
        .when(fdbRestService.getOrderableMedicine(Matchers.anyString(), Matchers.anyString(), Matchers.anyString()))
        .thenReturn("{\n" +
                        "    \"ApiError\": {\n" +
                        "        \"Message\": \"The method LoadAllAttributes is not available for bespoke OrderableMeds.\"\n" +
                        "    }\n" +
                        "}");

    final FdbTerminologyWithConceptDto result = fdbService.mapDrug(
        aspirinForWarning);

    assertEquals("Drug", result.getConceptType());
    assertEquals("100", result.getId());
    assertEquals("aspirin", result.getName());
    assertEquals("SNOMEDCT", result.getTerminology());
  }

  @Test
  public void createUnmatchedOrderableMedsWarningsTest()
  {
    final List<FdbTerminologyWithConceptDto> unmatchedOrderableMeds = Lists.newArrayList();
    final FdbTerminologyWithConceptDto aspirin =
        new FdbTerminologyWithConceptDto("1", "Aspirin", "SNOMEDCT", "Drug");
    final FdbTerminologyWithConceptDto paracetamol =
        new FdbTerminologyWithConceptDto("2", "Paracetamol", "SNOMEDCT", "Drug");
    final FdbTerminologyWithConceptDto dopamine =
        new FdbTerminologyWithConceptDto("3", "Dopamine", "OrderableMed", "OrderableMed");
    unmatchedOrderableMeds.add(aspirin);
    unmatchedOrderableMeds.add(paracetamol);
    unmatchedOrderableMeds.add(dopamine);

    final MedicationsWarningDto result = fdbService.createUnmatchedOrderableMedsWarning(unmatchedOrderableMeds);

    assertEquals(
        "Following medications don't work in PatientCheck module: Aspirin, Paracetamol!",
        result.getDescription());
    assertEquals(WarningSeverity.HIGH, result.getSeverity());
    assertEquals(WarningType.UNMATCHED, result.getType());
  }

  @Test
  public void createUnmatchedOrderableMedsWarningsEmptyTest()
  {
    final MedicationsWarningDto result = fdbService.createUnmatchedOrderableMedsWarning(Collections.emptyList());

    assertNull(result);
  }

  private WarningScreenMedicationDto getMedicationForWarningsSearchDto(
      final String name,
      final int id,
      final boolean isProspective,
      final Long routeId,
      final String externalId,
      final boolean isProduct,
      final String routeExternalId)
  {
    final WarningScreenMedicationDto medicationSummary = new WarningScreenMedicationDto();
    medicationSummary.setName(name);
    medicationSummary.setId(id);
    medicationSummary.setRouteId(routeId);
    medicationSummary.setProspective(isProspective);
    medicationSummary.setExternalId(externalId);
    medicationSummary.setProduct(isProduct);
    medicationSummary.setRouteExternalId(routeExternalId);
    return medicationSummary;
  }
}
