package com.marand.thinkmed.medications.administration.converter;

import java.util.Collections;

import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationStatusEnum;
import com.marand.thinkmed.medications.api.internal.dto.InfusionSetChangeEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicalDeviceEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.administration.impl.AdministrationUtilsImpl;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.BolusAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.DoseAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.PlannedDoseAdministration;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartOxygenAdministrationDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.Medication;
import com.marand.thinkmed.medications.ehr.model.MedicationAdministration;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import com.marand.thinkmed.medications.therapy.converter.fromehr.TherapyFromEhrUtils;
import com.marand.thinkmed.medications.therapy.converter.toehr.TherapyToEhrUtils;
import com.marand.thinkmed.medications.units.provider.TestUnitsProviderImpl;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.UserDto;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Nejc Korasa
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class AdministrationEhrConverterTest
{
  private final TherapyToEhrUtils therapyToEhrUtils = new TherapyToEhrUtils();

  @Mock
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  @InjectMocks
  private final AdministrationFromEhrConverter administrationFromEhrConverter = new AdministrationFromEhrConverter();

  @InjectMocks
  private final AdministrationToEhrConverter administrationToEhrConverter = new AdministrationToEhrConverter();

  @Before
  public void setUp()
  {
    administrationToEhrConverter.setAdministrationUtils(new AdministrationUtilsImpl());
    administrationToEhrConverter.setTherapyToEhrUtils(new TherapyToEhrUtils());

    administrationFromEhrConverter.setTherapyFromEhrUtils(new TherapyFromEhrUtils());
    administrationFromEhrConverter.setUnitsProvider(new TestUnitsProviderImpl());

    RequestUser.init(auth -> new UserDto("Test", null, "Test", Collections.emptyList()));
  }

  @Test
  public void convertAdministrationQuantitySubstitute()
  {
    // administration dto

    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    administrationDto.setAdministeredDose(buildTherapyDose(1000.0, "mg", null, null, TherapyDoseTypeEnum.QUANTITY));
    administrationDto.setPlannedDose(buildTherapyDose(2000.0, "mg", null, null, TherapyDoseTypeEnum.QUANTITY));
    administrationDto.setDifferentFromOrder(true);

    administrationDto.setRoute(MedicationsTestUtils.buildRoute(1L, "route name", null));
    administrationDto.setComposerName("composer");

    administrationDto.setDoctorConfirmation(true);
    administrationDto.setDoctorsComment("doctors comment");

    administrationDto.setComment("my comment");
    administrationDto.setAdministrationStatus(AdministrationStatusEnum.LATE);
    administrationDto.setAdministrationResult(AdministrationResultEnum.GIVEN);

    administrationDto.setAdministrationTime(new DateTime(2018, 5, 5, 12, 0));
    administrationDto.setPlannedTime(new DateTime(2018, 5, 5, 10, 0));

    administrationDto.setTaskId("3276628");
    administrationDto.setTherapyId("uid|Medication instruction");
    administrationDto.setWitness(new NamedExternalDto("witness id", "witness name"));

    final MedicationDto substituteMedication = MedicationsTestUtils.buildMedicationDto(
        222L,
        "Substitute",
        MedicationTypeEnum.MEDICATION);

    when(medicationsValueHolderProvider.getMedication(222L)).thenReturn(substituteMedication);
    administrationDto.setSubstituteMedication(substituteMedication);

    // inpatient prescription

    final InpatientPrescription inpatientPrescription = buildInpatientPrescription(MedicationsTestUtils.buildMedicationDto(111L, "Aspirin", MedicationTypeEnum.MEDICATION));

    final MedicationAdministration medicationAdministration = administrationToEhrConverter.convertAdministration(
        inpatientPrescription,
        administrationDto,
        AdministrationResultEnum.GIVEN,
        "Test",
        "Test",
        "central case",
        "care provider",
        new DateTime(2018, 2, 21, 8, 10, 0));

    assertNotNull(medicationAdministration);

    final StartAdministrationDto convertedAdministrationDto = (StartAdministrationDto)administrationFromEhrConverter.convertToAdministrationDto(
        medicationAdministration,
        inpatientPrescription);

    assertAdministeredDose(administrationDto, convertedAdministrationDto);
    assertPlannedDose(administrationDto, convertedAdministrationDto);

    assertEquals(administrationDto.getAdministrationTime(), convertedAdministrationDto.getAdministrationTime());
    assertEquals(administrationDto.getPlannedTime(), convertedAdministrationDto.getPlannedTime());

    assertEquals(administrationDto.getRoute().getId(), convertedAdministrationDto.getRoute().getId());
    assertEquals(administrationDto.getRoute().getName(), convertedAdministrationDto.getRoute().getName());

    assertEquals(administrationDto.getTaskId(), convertedAdministrationDto.getTaskId());
    assertEquals(administrationDto.getTherapyId(), convertedAdministrationDto.getTherapyId());

    assertEquals("Test", convertedAdministrationDto.getComposerName());

    assertEquals(administrationDto.getDoctorConfirmation(), convertedAdministrationDto.getDoctorConfirmation());
    assertEquals(administrationDto.getDoctorsComment(), convertedAdministrationDto.getDoctorsComment());

    assertEquals(AdministrationStatusEnum.COMPLETED_LATE, convertedAdministrationDto.getAdministrationStatus());
    assertEquals(administrationDto.getAdministrationResult(), convertedAdministrationDto.getAdministrationResult());

    assertEquals(administrationDto.getWitness().getName(), convertedAdministrationDto.getWitness().getName());
    assertEquals(administrationDto.getWitness().getId(), convertedAdministrationDto.getWitness().getId());

    assertEquals(administrationDto.isDifferentFromOrder(), convertedAdministrationDto.isDifferentFromOrder());

    assertEquals(administrationDto.getSubstituteMedication().getId(), convertedAdministrationDto.getSubstituteMedication().getId());
    assertEquals(administrationDto.getSubstituteMedication().getName(), convertedAdministrationDto.getSubstituteMedication().getName());
  }

  @Test
  public void convertAdministrationRateSelfAdministered()
  {
    // administration dto

    final AdjustInfusionAdministrationDto administrationDto = new AdjustInfusionAdministrationDto();

    administrationDto.setAdministeredDose(buildTherapyDose(90.0, "ml/h", null, null, TherapyDoseTypeEnum.RATE));
    administrationDto.setPlannedDose(buildTherapyDose(50.0, "ml/h", null, null, TherapyDoseTypeEnum.RATE));
    administrationDto.setDifferentFromOrder(true);

    administrationDto.setRoute(MedicationsTestUtils.buildRoute(1L, "route name", null));

    administrationDto.setComment("my comment");
    administrationDto.setAdministrationStatus(AdministrationStatusEnum.DUE);
    administrationDto.setAdministrationResult(AdministrationResultEnum.SELF_ADMINISTERED);
    administrationDto.setSelfAdministrationType(SelfAdministeringActionEnum.CHARTED_BY_NURSE);

    administrationDto.setAdministrationTime(new DateTime(2018, 5, 5, 12, 0));
    administrationDto.setPlannedTime(new DateTime(2018, 5, 5, 12, 0));

    administrationDto.setTaskId("3276628");
    administrationDto.setTherapyId("uid|Medication instruction");

    // inpatient prescription

    final InpatientPrescription inpatientPrescription = buildInpatientPrescription(MedicationsTestUtils.buildMedicationDto(111L, "Aspirin", MedicationTypeEnum.MEDICATION));

    final MedicationAdministration medicationAdministration = administrationToEhrConverter.convertAdministration(
        inpatientPrescription,
        administrationDto,
        AdministrationResultEnum.GIVEN,
        "Test",
        "Test",
        "central case",
        "care provider",
        new DateTime(2018, 2, 21, 8, 10, 0));

    final AdjustInfusionAdministrationDto convertedAdministrationDto = (AdjustInfusionAdministrationDto)administrationFromEhrConverter.convertToAdministrationDto(
        medicationAdministration,
        inpatientPrescription);

    assertAdministeredDose(administrationDto, convertedAdministrationDto);
    assertPlannedDose(administrationDto, convertedAdministrationDto);

    assertEquals(administrationDto.getAdministrationTime(), convertedAdministrationDto.getAdministrationTime());
    assertEquals(administrationDto.getPlannedTime(), convertedAdministrationDto.getPlannedTime());

    assertEquals(AdministrationStatusEnum.COMPLETED, convertedAdministrationDto.getAdministrationStatus());
    assertEquals(AdministrationResultEnum.SELF_ADMINISTERED, convertedAdministrationDto.getAdministrationResult());

    assertEquals(administrationDto.isDifferentFromOrder(), convertedAdministrationDto.isDifferentFromOrder());
  }

  @Test
  public void convertAdministrationBolus()
  {
    // administration dto

    final BolusAdministrationDto administrationDto = new BolusAdministrationDto();

    administrationDto.setAdministeredDose(buildTherapyDose(90.0, "mg", null, null, TherapyDoseTypeEnum.QUANTITY));
    administrationDto.setRoute(MedicationsTestUtils.buildRoute(1L, "route name", null));

    administrationDto.setComment("my comment");
    administrationDto.setAdministrationStatus(AdministrationStatusEnum.DUE);

    administrationDto.setAdministrationTime(new DateTime(2018, 5, 5, 12, 0));
    administrationDto.setPlannedTime(new DateTime(2018, 5, 5, 12, 0));

    administrationDto.setTaskId("3276628");
    administrationDto.setTherapyId("uid|Medication instruction");

    // inpatient prescription

    final InpatientPrescription inpatientPrescription = buildInpatientPrescription(MedicationsTestUtils.buildMedicationDto(111L, "Aspirin", MedicationTypeEnum.MEDICATION));

    final MedicationAdministration medicationAdministration = administrationToEhrConverter.convertAdministration(
        inpatientPrescription,
        administrationDto,
        AdministrationResultEnum.GIVEN,
        "Test",
        "Test",
        "central case",
        "care provider",
        new DateTime(2018, 2, 21, 8, 10, 0));

    final BolusAdministrationDto convertedAdministrationDto = (BolusAdministrationDto)administrationFromEhrConverter.convertToAdministrationDto(
        medicationAdministration,
        inpatientPrescription);

    assertAdministeredDose(administrationDto, convertedAdministrationDto);

    assertEquals(administrationDto.getAdministrationTime(), convertedAdministrationDto.getAdministrationTime());
    assertEquals(administrationDto.getPlannedTime(), convertedAdministrationDto.getPlannedTime());

    assertEquals(AdministrationStatusEnum.COMPLETED, convertedAdministrationDto.getAdministrationStatus());
  }

  @Test
  public void convertAdministrationNotGiven()
  {
    // administration dto

    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    administrationDto.setAdministeredDose(buildTherapyDose(90.0, "mg", null, null, TherapyDoseTypeEnum.QUANTITY));
    administrationDto.setAdministrationStatus(AdministrationStatusEnum.DUE);

    administrationDto.setAdministrationTime(new DateTime(2018, 5, 5, 12, 0));
    administrationDto.setPlannedTime(new DateTime(2018, 5, 5, 12, 0));

    administrationDto.setTaskId("3276628");
    administrationDto.setTherapyId("uid|Medication instruction");

    // inpatient prescription

    final InpatientPrescription inpatientPrescription = buildInpatientPrescription(MedicationsTestUtils.buildMedicationDto(111L, "Aspirin", MedicationTypeEnum.MEDICATION));

    final MedicationAdministration medicationAdministration = administrationToEhrConverter.convertAdministration(
        inpatientPrescription,
        administrationDto,
        AdministrationResultEnum.NOT_GIVEN,
        "Test",
        "Test",
        "central case",
        "care provider",
        new DateTime(2018, 2, 21, 8, 10, 0));

    final StartAdministrationDto convertedAdministrationDto = (StartAdministrationDto)administrationFromEhrConverter.convertToAdministrationDto(
        medicationAdministration,
        inpatientPrescription);

    assertAdministeredDose(administrationDto, convertedAdministrationDto);

    assertEquals(AdministrationStatusEnum.FAILED, convertedAdministrationDto.getAdministrationStatus());
    assertEquals(AdministrationResultEnum.NOT_GIVEN, convertedAdministrationDto.getAdministrationResult());
  }

  @Test
  public void convertAdministrationOxygen()
  {
    // administration dto

    final StartOxygenAdministrationDto administrationDto = new StartOxygenAdministrationDto();

    administrationDto.setPlannedStartingDevice(new OxygenStartingDevice(MedicalDeviceEnum.CPAP_MASK));
    administrationDto.setStartingDevice(new OxygenStartingDevice(MedicalDeviceEnum.FULL_FACE_MASK));

    administrationDto.setAdministeredDose(buildTherapyDose(90.0, "l/min", null, null, TherapyDoseTypeEnum.RATE));
    administrationDto.setAdministrationStatus(AdministrationStatusEnum.DUE);

    administrationDto.setAdministrationTime(new DateTime(2018, 5, 5, 12, 0));
    administrationDto.setPlannedTime(new DateTime(2018, 5, 5, 12, 0));

    administrationDto.setTaskId("3276628");
    administrationDto.setTherapyId("uid|Medication instruction");

    // inpatient prescription

    final InpatientPrescription inpatientPrescription = buildInpatientPrescription(MedicationsTestUtils.buildMedicationDto(111L, "Aspirin", MedicationTypeEnum.MEDICINAL_GAS));
    inpatientPrescription.getMedicationOrder().getAdditionalDetails().setPrescriptionType(MedicationOrderFormType.OXYGEN.getDvCodedText());

    final MedicationAdministration medicationAdministration = administrationToEhrConverter.convertAdministration(
        inpatientPrescription,
        administrationDto,
        AdministrationResultEnum.GIVEN,
        "Test",
        "Test",
        "central case",
        "care provider",
        new DateTime(2018, 2, 21, 8, 10, 0));

    final StartOxygenAdministrationDto convertedAdministrationDto = (StartOxygenAdministrationDto)administrationFromEhrConverter.convertToAdministrationDto(
        medicationAdministration,
        inpatientPrescription);

    assertAdministeredDose(administrationDto, convertedAdministrationDto);

    assertEquals(administrationDto.getStartingDevice().getRoute(), convertedAdministrationDto.getStartingDevice().getRoute());
    assertEquals(administrationDto.getPlannedStartingDevice().getRoute(), convertedAdministrationDto.getPlannedStartingDevice().getRoute());

    assertFalse(administrationDto.isDifferentFromOrder());

    assertEquals(AdministrationStatusEnum.COMPLETED, convertedAdministrationDto.getAdministrationStatus());
    assertEquals(AdministrationResultEnum.GIVEN, convertedAdministrationDto.getAdministrationResult());
  }

  @Test
  public void convertAdministrationRateInfusionBag()
  {
    // administration dto

    final StartAdministrationDto administrationDto = new StartAdministrationDto();

    administrationDto.setAdministeredDose(buildTherapyDose(90.0, "ml/h", null, null, TherapyDoseTypeEnum.RATE));
    administrationDto.setRoute(MedicationsTestUtils.buildRoute(1L, "route name", null));
    administrationDto.setAdministrationStatus(AdministrationStatusEnum.DUE);

    administrationDto.setAdministrationTime(new DateTime(2018, 5, 5, 12, 0));
    administrationDto.setPlannedTime(new DateTime(2018, 5, 5, 12, 0));

    administrationDto.setTaskId("3276628");
    administrationDto.setTherapyId("uid|Medication instruction");

    administrationDto.setAdditionalAdministration(true);
    administrationDto.setInfusionBag(new InfusionBagDto(200.0, "ml"));

    // inpatient prescription

    final InpatientPrescription inpatientPrescription = buildInpatientPrescription(MedicationsTestUtils.buildMedicationDto(111L, "Aspirin", MedicationTypeEnum.MEDICATION));
    inpatientPrescription.getMedicationOrder().getAdditionalDetails().setBaselineInfusion(DataValueUtils.getBoolean(true));

    final MedicationAdministration medicationAdministration = administrationToEhrConverter.convertAdministration(
        inpatientPrescription,
        administrationDto,
        AdministrationResultEnum.GIVEN,
        "Test",
        "Test",
        "central case",
        "care provider",
        new DateTime(2018, 2, 21, 8, 10, 0));

    assertTrue(medicationAdministration.getMedicationManagement().getAdditionalDetails().getBaselineInfusion().isValue());

    final StartAdministrationDto convertedAdministrationDto = (StartAdministrationDto)administrationFromEhrConverter.convertToAdministrationDto(
        medicationAdministration,
        inpatientPrescription);

    assertAdministeredDose(administrationDto, convertedAdministrationDto);

    assertEquals(administrationDto.getAdministrationTime(), convertedAdministrationDto.getAdministrationTime());
    assertEquals(administrationDto.getPlannedTime(), convertedAdministrationDto.getPlannedTime());

    assertEquals(administrationDto.getInfusionBag().getQuantity(), convertedAdministrationDto.getInfusionBag().getQuantity());
    assertEquals(administrationDto.getInfusionBag().getUnit(), convertedAdministrationDto.getInfusionBag().getUnit());

    assertEquals(AdministrationStatusEnum.COMPLETED, convertedAdministrationDto.getAdministrationStatus());

    assertEquals(administrationDto.isDifferentFromOrder(), convertedAdministrationDto.isDifferentFromOrder());
  }

  @Test
  public void convertAdministrationSetChange()
  {
    // administration dto

    final InfusionSetChangeDto administrationDto = new InfusionSetChangeDto();

    administrationDto.setAdministrationStatus(AdministrationStatusEnum.DUE);
    administrationDto.setAdministrationTime(new DateTime(2018, 5, 5, 12, 0));
    administrationDto.setTherapyId("uid|Medication instruction");
    administrationDto.setComment("comment");
    administrationDto.setInfusionBag(new InfusionBagDto(200.0, "ml"));
    administrationDto.setInfusionSetChangeEnum(InfusionSetChangeEnum.INFUSION_SYRINGE_CHANGE);

    // inpatient prescription

    final InpatientPrescription inpatientPrescription = buildInpatientPrescription(MedicationsTestUtils.buildMedicationDto(111L, "Aspirin", MedicationTypeEnum.MEDICATION));

    final MedicationAdministration medicationAdministration = administrationToEhrConverter.convertSetChangeAdministration(
        inpatientPrescription,
        administrationDto,
        "central case",
        "care provider",
        new DateTime(2018, 2, 21, 8, 10, 0));

    final InfusionSetChangeDto convertedAdministrationDto = (InfusionSetChangeDto)administrationFromEhrConverter.convertToAdministrationDto(
        medicationAdministration,
        inpatientPrescription);

    assertEquals(administrationDto.getAdministrationTime(), convertedAdministrationDto.getAdministrationTime());

    assertEquals(administrationDto.getInfusionBag().getQuantity(), convertedAdministrationDto.getInfusionBag().getQuantity());
    assertEquals(administrationDto.getInfusionBag().getUnit(), convertedAdministrationDto.getInfusionBag().getUnit());

    assertEquals(AdministrationStatusEnum.COMPLETED, convertedAdministrationDto.getAdministrationStatus());

    assertEquals(administrationDto.getComment(), convertedAdministrationDto.getComment());
  }

  private InpatientPrescription buildInpatientPrescription(final MedicationDto medicationDto)
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.setUid("uid");

    final MedicationOrder order = new MedicationOrder();
    inpatientPrescription.setMedicationOrder(order);

    order.setName(DataValueUtils.getText("Medication instruction"));

    final Medication preparationDetails = new Medication();
    order.setPreparationDetails(preparationDetails);
    preparationDetails.setForm(DataValueUtils.getLocalCodedText("dose form id", "dose form name"));
    preparationDetails.setComponentName(therapyToEhrUtils.extractMedication(medicationDto));
    order.setMedicationItem(therapyToEhrUtils.extractMedication(medicationDto));
    return inpatientPrescription;
  }

  private TherapyDoseDto buildTherapyDose(
      final Double numerator,
      final String numeratorUnit,
      final Double denominator,
      final String denominatorUnit,
      final TherapyDoseTypeEnum doseTypeEnum)
  {
    final TherapyDoseDto dose = new TherapyDoseDto();
    dose.setNumerator(numerator);
    dose.setNumeratorUnit(numeratorUnit);
    dose.setDenominator(denominator);
    dose.setDenominatorUnit(denominatorUnit);
    dose.setTherapyDoseTypeEnum(doseTypeEnum);
    return dose;
  }

  private void assertAdministeredDose(final DoseAdministration first, final DoseAdministration second)
  {
    assertEquals(first.getAdministeredDose().getNumerator(), second.getAdministeredDose().getNumerator());
    assertEquals(first.getAdministeredDose().getNumeratorUnit(), second.getAdministeredDose().getNumeratorUnit());
    assertEquals(first.getAdministeredDose().getDenominator(), second.getAdministeredDose().getDenominator());
    assertEquals(first.getAdministeredDose().getDenominatorUnit(), second.getAdministeredDose().getDenominatorUnit());
    assertEquals(first.getAdministeredDose().getTherapyDoseTypeEnum(), second.getAdministeredDose().getTherapyDoseTypeEnum());
  }

  private void assertPlannedDose(final PlannedDoseAdministration first, final PlannedDoseAdministration second)
  {
    assertEquals(first.getPlannedDose().getNumerator(), second.getPlannedDose().getNumerator());
    assertEquals(first.getPlannedDose().getNumeratorUnit(), second.getPlannedDose().getNumeratorUnit());
    assertEquals(first.getPlannedDose().getDenominator(), second.getPlannedDose().getDenominator());
    assertEquals(first.getPlannedDose().getDenominatorUnit(), second.getPlannedDose().getDenominatorUnit());
    assertEquals(first.getPlannedDose().getTherapyDoseTypeEnum(), second.getPlannedDose().getTherapyDoseTypeEnum());
  }
}
