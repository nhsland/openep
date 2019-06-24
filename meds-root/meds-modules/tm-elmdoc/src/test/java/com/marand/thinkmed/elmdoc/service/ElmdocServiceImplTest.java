package com.marand.thinkmed.elmdoc.service;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.elmdoc.data.AlertDo;
import com.marand.thinkmed.elmdoc.data.AlertsDo;
import com.marand.thinkmed.elmdoc.data.GenderEnum;
import com.marand.thinkmed.elmdoc.data.ScreenRequestDo;
import com.marand.thinkmed.elmdoc.data.ScreenableDrugDo;
import com.marand.thinkmed.elmdoc.data.ScreeningSummaryDo;
import com.marand.thinkmed.elmdoc.data.ScreeningTypesEnum;
import com.marand.thinkmed.elmdoc.data.SeverityDo;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.WarningScreenMedicationDto;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Vid Kumse
 */
public class ElmdocServiceImplTest
{
  private ElmdocServiceImpl elmdocService;

  @Before
  public void setup()
  {
    elmdocService = new ElmdocServiceImpl();
  }

  @Test
  public void testMapMedicationsWarning()
  {
    final List<ScreenableDrugDo> drugsAspirinWarfarin = Lists.newArrayList(
        new ScreenableDrugDo("1", "Aspirin", "100", "MyAspirin", true),
        new ScreenableDrugDo("2", "Warfarin", "200", "MyWarfarin", true));
    final List<ScreenableDrugDo> drugsAspirin = Lists.newArrayList(
        new ScreenableDrugDo("1", "Aspirin", "100", "MyAspirin", true));
    final List<ScreenableDrugDo> drugsAspirinAspirin = Lists.newArrayList(
        new ScreenableDrugDo("1", "Aspirin", "100", "MyAspirin", true),
        new ScreenableDrugDo("1", "Aspirin", "100", "MyAspirin", true));

    final ScreeningSummaryDo screeningSummaryDo = new ScreeningSummaryDo();

    final List<AlertDo> itemsDrugDrug = Lists.newArrayList(new AlertDo(
        new SeverityDo(30000, "Низкий"),
        "Medicines have interactions",
        "<HTML>Medicines hasve interactions</HTML>",
        drugsAspirinWarfarin));

    screeningSummaryDo.setDrugDrugInteractions(new AlertsDo(itemsDrugDrug));

    final List<AlertDo> itemsDrugFood = Lists.newArrayList(new AlertDo(
        new SeverityDo(20000, "Низкий"),
        "Taking medicine and eating meat is dangerous",
        "<HTML>Taking medicine and eating meat is dangerous</HTML>",
        drugsAspirin));
    screeningSummaryDo.setDrugFoodInteractions(new AlertsDo(itemsDrugFood));

    final List<AlertDo> itemsDrugAlcohol = Lists.newArrayList(new AlertDo(
        new SeverityDo(10000, "Низкий"),
        "Taking medicine and drinking alcohol is dangerous",
        "<HTML>Taking medicine and drinking alcohol is dangerous</HTML>",
        drugsAspirin));
    screeningSummaryDo.setDrugAlcoholInteractions(new AlertsDo(itemsDrugAlcohol));

    final List<AlertDo> itemsAllergicReactions = Lists.newArrayList(new AlertDo(
        "Patient is allergic to this medicine",
        "<HTML>Patient is allergic to this medicine</HTML>",
        drugsAspirin));
    screeningSummaryDo.setAllergicReactions(new AlertsDo(itemsAllergicReactions));

    final List<AlertDo> itemsAgeContraindications = Lists.newArrayList(new AlertDo(
        new SeverityDo(2, "Низкий"),
        "Patient is to old for this medicine",
        "<HTMl>Patient is to old for this medicine</HTML>",
        drugsAspirin));
    screeningSummaryDo.setAgeContraindications(new AlertsDo(itemsAgeContraindications));

    final List<AlertDo> itemsGenderContraindications = Lists.newArrayList(new AlertDo(
        new SeverityDo(1, "Низкий"),
        "Patient of this gender are not allowed taking this medicine",
        "<HTML>Patient of this gender are not allowed taking this medicine</HTML>",
        drugsAspirin));
    screeningSummaryDo.setGenderContraindications(new AlertsDo(itemsGenderContraindications));

    final List<AlertDo> itemsDiseaseContaindications = Lists.newArrayList(new AlertDo(
        new SeverityDo(1, "Низкий"),
        "Patient has disease and is not allowed to take this drug",
        "<HTML>Patient has disease and is not allowed to take this drug</HTML>",
        drugsAspirin));
    screeningSummaryDo.setDiseaseContraindications(new AlertsDo(itemsDiseaseContaindications));

    final List<AlertDo> itemsLactationContraindications = Lists.newArrayList(new AlertDo(
        new SeverityDo(3, "Низкий"),
        "Drug is dangerous for toddlers",
        "<HTML>Drug is dangerous for toddlers</HTML>",
        drugsAspirin));
    screeningSummaryDo.setLactationContraindications(new AlertsDo(itemsLactationContraindications));

    final List<AlertDo> itemsPregnancyContraindications = Lists.newArrayList(new AlertDo(
        new SeverityDo(3, "Низкий"),
        "Patient is pregnant and is not allowed to take this drug",
        "<HTML>Patient is pregnant and is not allowed to take this drug</HTML>",
        drugsAspirin));
    screeningSummaryDo.setPregnancyContraindications(new AlertsDo(itemsPregnancyContraindications));

    final List<AlertDo> itemsDuplicateTherapies = Lists.newArrayList(new AlertDo(
        "Patient already takes this drug",
        "<HTML>Patient already takes this drug</HTML>",
        drugsAspirinAspirin));
    screeningSummaryDo.setDuplicateTherapies(new AlertsDo(itemsDuplicateTherapies));

    final List<AlertDo> itemsGeneticTests = Lists.newArrayList(new AlertDo(
        "Patient has bad genetics",
        "<HTML>Patient has bad genetics</HTML>",
        drugsAspirin));
    screeningSummaryDo.setGeneticTests(new AlertsDo(itemsGeneticTests));

    final List<AlertDo> itemsDopingAlerts = Lists.newArrayList(new AlertDo(
        "Doping warnigns",
        "<HTML>Doping warnings</HTML>",
        drugsAspirin));
    screeningSummaryDo.setDopingAlerts(new AlertsDo(itemsDopingAlerts));

    final List<MedicationsWarningDto> medicationWarningDto = elmdocService.mapMedicationsWarnings(screeningSummaryDo);

    assertEquals("Medicines have interactions", medicationWarningDto.get(0).getDescription());
    assertEquals(WarningSeverity.OTHER, medicationWarningDto.get(0).getSeverity());
    assertEquals(WarningType.INTERACTION, medicationWarningDto.get(0).getType());
    assertEquals(2, medicationWarningDto.get(0).getMedications().size());
    assertEquals("Aspirin", medicationWarningDto.get(0).getMedications().get(0).getName());
    assertEquals("1", medicationWarningDto.get(0).getMedications().get(0).getId());
    assertEquals("Warfarin", medicationWarningDto.get(0).getMedications().get(1).getName());
    assertEquals("2", medicationWarningDto.get(0).getMedications().get(1).getId());

    assertEquals("Taking medicine and drinking alcohol is dangerous", medicationWarningDto.get(1).getDescription());
    assertEquals(WarningSeverity.HIGH_OVERRIDE, medicationWarningDto.get(1).getSeverity());
    assertEquals(WarningType.INTERACTION, medicationWarningDto.get(1).getType());
    assertEquals(1, medicationWarningDto.get(1).getMedications().size());
    assertEquals("Aspirin", medicationWarningDto.get(1).getMedications().get(0).getName());
    assertEquals("1", medicationWarningDto.get(1).getMedications().get(0).getId());

    assertEquals("Taking medicine and eating meat is dangerous", medicationWarningDto.get(2).getDescription());
    assertEquals(WarningSeverity.HIGH, medicationWarningDto.get(2).getSeverity());
    assertEquals(WarningType.INTERACTION, medicationWarningDto.get(2).getType());
    assertEquals(1, medicationWarningDto.get(2).getMedications().size());
    assertEquals("Aspirin", medicationWarningDto.get(2).getMedications().get(0).getName());
    assertEquals("1", medicationWarningDto.get(2).getMedications().get(0).getId());

    assertEquals("Patient is allergic to this medicine", medicationWarningDto.get(3).getDescription());
    assertEquals(WarningType.ALLERGY, medicationWarningDto.get(3).getType());
    assertEquals(1, medicationWarningDto.get(3).getMedications().size());
    assertEquals("Aspirin", medicationWarningDto.get(3).getMedications().get(0).getName());
    assertEquals("1", medicationWarningDto.get(3).getMedications().get(0).getId());

    assertEquals("Patient has disease and is not allowed to take this drug", medicationWarningDto.get(4).getDescription());
    assertEquals(WarningSeverity.HIGH_OVERRIDE, medicationWarningDto.get(4).getSeverity());
    assertEquals(WarningType.PATIENT_CHECK, medicationWarningDto.get(4).getType());
    assertEquals(1, medicationWarningDto.get(4).getMedications().size());
    assertEquals("Aspirin", medicationWarningDto.get(4).getMedications().get(0).getName());
    assertEquals("1", medicationWarningDto.get(4).getMedications().get(0).getId());

    assertEquals("Drug is dangerous for toddlers", medicationWarningDto.get(5).getDescription());
    assertEquals(WarningSeverity.OTHER, medicationWarningDto.get(5).getSeverity());
    assertEquals(WarningType.PATIENT_CHECK, medicationWarningDto.get(5).getType());
    assertEquals(1, medicationWarningDto.get(5).getMedications().size());
    assertEquals("Aspirin", medicationWarningDto.get(5).getMedications().get(0).getName());
    assertEquals("1", medicationWarningDto.get(5).getMedications().get(0).getId());

    assertEquals("Patient is pregnant and is not allowed to take this drug", medicationWarningDto.get(6).getDescription());
    assertEquals(WarningSeverity.OTHER, medicationWarningDto.get(6).getSeverity());
    assertEquals(WarningType.PATIENT_CHECK, medicationWarningDto.get(6).getType());
    assertEquals(1, medicationWarningDto.get(6).getMedications().size());
    assertEquals("Aspirin", medicationWarningDto.get(6).getMedications().get(0).getName());
    assertEquals("1", medicationWarningDto.get(6).getMedications().get(0).getId());

    assertEquals("Patient is to old for this medicine", medicationWarningDto.get(7).getDescription());
    assertEquals(WarningSeverity.HIGH, medicationWarningDto.get(7).getSeverity());
    assertEquals(WarningType.PATIENT_CHECK, medicationWarningDto.get(7).getType());
    assertEquals(1, medicationWarningDto.get(7).getMedications().size());
    assertEquals("Aspirin", medicationWarningDto.get(7).getMedications().get(0).getName());
    assertEquals("1", medicationWarningDto.get(7).getMedications().get(0).getId());

    assertEquals(
        "Patient of this gender are not allowed taking this medicine",
        medicationWarningDto.get(8).getDescription());
    assertEquals(WarningSeverity.HIGH_OVERRIDE, medicationWarningDto.get(8).getSeverity());
    assertEquals(WarningType.PATIENT_CHECK, medicationWarningDto.get(8).getType());
    assertEquals(1, medicationWarningDto.get(8).getMedications().size());
    assertEquals("Aspirin", medicationWarningDto.get(8).getMedications().get(0).getName());
    assertEquals("1", medicationWarningDto.get(8).getMedications().get(0).getId());

    assertEquals("Patient already takes this drug", medicationWarningDto.get(9).getDescription());
    assertEquals(WarningSeverity.HIGH, medicationWarningDto.get(9).getSeverity());
    assertEquals(WarningType.DUPLICATE, medicationWarningDto.get(9).getType());
    assertEquals(2, medicationWarningDto.get(9).getMedications().size());
    assertEquals("Aspirin", medicationWarningDto.get(9).getMedications().get(0).getName());
    assertEquals("1", medicationWarningDto.get(9).getMedications().get(0).getId());
    assertEquals("Aspirin", medicationWarningDto.get(9).getMedications().get(1).getName());
    assertEquals("1", medicationWarningDto.get(9).getMedications().get(1).getId());

    assertEquals("Patient has bad genetics", medicationWarningDto.get(10).getDescription());
    assertEquals("Aspirin", medicationWarningDto.get(10).getMedications().get(0).getName());
    assertEquals("1", medicationWarningDto.get(10).getMedications().get(0).getId());

    assertEquals("Doping warnigns", medicationWarningDto.get(11).getDescription());
    assertEquals("Aspirin", medicationWarningDto.get(11).getMedications().get(0).getName());
    assertEquals("1", medicationWarningDto.get(11).getMedications().get(0).getId());
  }

  @Test
  public void testMapMedicationsNoData()
  {
    final List<MedicationsWarningDto> medicationWarningDto = elmdocService.mapMedicationsWarnings(new ScreeningSummaryDo());

    assertTrue(medicationWarningDto.isEmpty());
  }

  @Test
  public void testBuildScreenRequestDo()
  {
    final DateTime dateOfBirth = new DateTime(2010, 4, 28, 7, 22);
    final Double patientWeightInKg = 66.0;
    final Double bsaInM2 = 45.0;
    final Gender gender = Gender.MALE;
    final List<IdNameDto> diseaseTypeValues = Lists.newArrayList(new IdNameDto("100", "Flu"));
    final List<IdNameDto> allergiesExternalValues = Lists.newArrayList(new IdNameDto("2", "Warfarin"));
    final List<WarningScreenMedicationDto> medicationSummaries = Lists.newArrayList(
        getMedicationForWarningsSearchDto("1", "Aspirin", 7947003, true),
        getMedicationForWarningsSearchDto("2", "Warfarin", 7947003, true)
    );

    final ScreenRequestDo screenRequestDoResult = elmdocService.buildScreenRequestDo(
        dateOfBirth,
        patientWeightInKg,
        bsaInM2,
        gender,
        diseaseTypeValues,
        allergiesExternalValues,
        medicationSummaries);

    assertEquals(ScreeningTypesEnum.getAllNames(), screenRequestDoResult.getScreeningTypes());
    assertEquals(new LocalDate(2010, 4, 28), screenRequestDoResult.getPatient().getBirthDate());
    assertEquals((Double)66.0, screenRequestDoResult.getPatient().getWeight());
    assertEquals((Double)45.0, screenRequestDoResult.getPatient().getBodySurfaceArea());
    assertEquals(GenderEnum.Male, screenRequestDoResult.getPatient().getGender());
    assertEquals("Flu", screenRequestDoResult.getDiseases().get(0).getName());
    assertEquals("100", screenRequestDoResult.getDiseases().get(0).getCode());
    assertEquals("Warfarin", screenRequestDoResult.getAllergies().get(0).getName());
    assertEquals("2", screenRequestDoResult.getAllergies().get(0).getCode());
    assertEquals("Aspirin", screenRequestDoResult.getDrugs().get(0).getName());
    assertEquals("1", screenRequestDoResult.getDrugs().get(0).getCode());
    assertEquals("Warfarin", screenRequestDoResult.getDrugs().get(1).getName());
    assertEquals("2", screenRequestDoResult.getDrugs().get(1).getCode());
  }

  @Test
  public void testBuildScreenRequestDoNoData()
  {
    final ScreenRequestDo screenRequestDoResult = elmdocService.buildScreenRequestDo(
        new DateTime(2010, 4, 28, 7, 22),
        66.0,
        45.0,
        Gender.MALE,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList());

    assertTrue(screenRequestDoResult.getAllergies().isEmpty());
    assertTrue(screenRequestDoResult.getDrugs().isEmpty());
    assertTrue(screenRequestDoResult.getDiseases().isEmpty());
  }

  @SuppressWarnings("Duplicates")
  private WarningScreenMedicationDto getMedicationForWarningsSearchDto(
      final String externalId,
      final String name,
      final int id,
      final boolean isProspective)
  {
    final WarningScreenMedicationDto medicationSummary = new WarningScreenMedicationDto();
    medicationSummary.setExternalId(externalId);
    medicationSummary.setName(name);
    medicationSummary.setId(id);
    medicationSummary.setProspective(isProspective);
    return medicationSummary;
  }
}