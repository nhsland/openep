package com.marand.thinkmed.medications.overview.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.daterule.service.MafDateRuleService;
import com.marand.maf.core.time.DayType;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.PrescriptionGroupEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.administration.impl.AdministrationHandlerImpl;
import com.marand.thinkmed.medications.administration.impl.AdministrationTaskConverterImpl;
import com.marand.thinkmed.medications.administration.impl.AdministrationUtilsImpl;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.business.impl.DefaultMedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.business.mapper.MedicationDataDtoMapper;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.connector.data.object.AllergiesStatus;
import com.marand.thinkmed.medications.connector.data.object.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationStatusEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.overview.ContinuousInfusionTherapyRowDtoDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.IsmTransition;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import com.marand.thinkmed.medications.witnessing.WitnessingHandler;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

/**
 * @author Nejc Korasa
 */
@SuppressWarnings("TooBroadScope")
@RunWith(SpringJUnit4ClassRunner.class)
public class OverviewContentProviderTest
{
  @InjectMocks
  private final OverviewContentProviderImpl overviewContentProvider = new OverviewContentProviderImpl();

  @InjectMocks
  private final DefaultMedicationsBo medicationsBo = new DefaultMedicationsBo();

  @Mock
  private MafDateRuleService mafDateRuleService;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private TherapyEhrHandler therapyEhrHandler;

  @Spy
  private final AdministrationUtils administrationUtils = new AdministrationUtilsImpl();

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Spy
  private final AdministrationHandler administrationHandler = new AdministrationHandlerImpl();

  @Mock
  private TherapyDisplayProvider therapyDisplayProvider;

  @Spy
  private final AdministrationTaskConverter administrationTaskConverter = new AdministrationTaskConverterImpl();

  @Spy
  private final MedicationDataDtoMapper medicationDataDtoMapper = new MedicationDataDtoMapper();

  @Mock
  private TherapyConverter therapyConverter;

  @Mock
  private WitnessingHandler witnessingHandler;

  @Mock
  private MedsProperties medsProperties;

  @Mock
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  @Before
  public void setUp()
  {
    Mockito.when(medsProperties.getDoctorReviewEnabled()).thenReturn(true);
    overviewContentProvider.setMedicationsBo(medicationsBo);

    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2016, 7, 4, 0, 0), DayType.WORKING_DAY)).thenReturn(true);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2016, 7, 5, 0, 0), DayType.WORKING_DAY)).thenReturn(false);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2016, 7, 6, 0, 0), DayType.WORKING_DAY)).thenReturn(false);
    Mockito.when(mafDateRuleService.isDateOfType(new DateTime(2016, 7, 7, 0, 0), DayType.WORKING_DAY)).thenReturn(true);

    Mockito.when(witnessingHandler.isTherapyWitnessingRequired(any())).thenReturn(false);
  }

  private StartAdministrationDto buildAdministration(
      final String uidValue,
      final DateTime actionTime,
      final String therapyId,
      final String composer,
      final Double doseNumerator,
      final String doseNumeratorUnit,
      final String comment,
      final String taskId,
      final DateTime taskDueTime)
  {
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setAdministrationId(uidValue);
    administration.setAdministrationTime(actionTime);
    administration.setTherapyId(therapyId);
    administration.setComposerName(composer);
    administration.setAdministrationResult(AdministrationResultEnum.GIVEN);
    administration.setComment(comment);
    administration.setTaskId(taskId);
    administration.setPlannedTime(taskDueTime);

    final TherapyDoseDto administeredDose = new TherapyDoseDto();
    administeredDose.setNumerator(doseNumerator);
    administeredDose.setNumeratorUnit(doseNumeratorUnit);
    administration.setAdministeredDose(administeredDose);
    administration.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);
    return administration;
  }

  @Test
  public void testBuildTherapyTimeline1()
  {
    final List<InpatientPrescription> inpatientPrescriptions = new ArrayList<>();

    final InpatientPrescription inpatientPrescription = MedicationsTestUtils.buildTestInpatientPrescription(
        "uid1::1",
        new DateTime(2014, 2, 20, 12, 0, 0),
        "1",
        "Medication order");
    inpatientPrescriptions.add(inpatientPrescription);

    final ConstantSimpleTherapyDto constantSimpleTherapyDto = new ConstantSimpleTherapyDto();
    constantSimpleTherapyDto.setCompositionUid(inpatientPrescription.getUid());
    constantSimpleTherapyDto.setEhrOrderName(inpatientPrescription.getMedicationOrder().getName().getValue());
    final MedicationDto medication = new MedicationDto(1L, "Aspirin");
    constantSimpleTherapyDto.setMedication(medication);

    //MOCKS
    Mockito
        .when(therapyConverter.convertToTherapyDto(
            inpatientPrescription.getMedicationOrder(),
            inpatientPrescription.getUid(),
            DataValueUtils.getDateTime(inpatientPrescription.getContext().getStartTime())))
        .thenReturn(constantSimpleTherapyDto);

    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    Mockito
        .when(therapyEhrHandler.getOriginalTherapyStart(any(), any()))
        .thenReturn(new DateTime(2014, 2, 20, 12, 0, 0));

    final MedicationDataDto medicationData = new MedicationDataDto();
    medicationData.setMedication(medication);
    Mockito.when(medicationsValueHolderProvider.getMedicationData(1L))
        .thenReturn(medicationData);

    final String therapyId = TherapyIdUtils.createTherapyId(inpatientPrescription);

    final List<AdministrationDto> administrations = new ArrayList<>();

    //administered on time
    administrations.add(
        buildAdministration(
            "adm0",
            new DateTime(2014, 2, 21, 8, 10, 0),
            therapyId,
            "1",
            500.0,
            "mg",
            "comment123",
            "t0",
            new DateTime(2014, 2, 21, 8, 0, 0)));

    //administered on time
    administrations.add(
        buildAdministration(
            "adm1",
            new DateTime(2014, 2, 21, 10, 40, 0),
            therapyId,
            "1",
            500.0,
            "mg",
            null,
            "t1",
            new DateTime(2014, 2, 21, 11, 0, 0)));

    //administered late
    administrations.add(
        buildAdministration(
            "adm2",
            new DateTime(2014, 2, 21, 14, 50, 0),
            therapyId,
            "1",
            500.0,
            "ug",
            null,
            "t2",
            new DateTime(2014, 2, 21, 14, 0, 0)));

    //administered early
    administrations.add(
        buildAdministration(
            "adm3",
            new DateTime(2014, 2, 21, 15, 15, 0),
            therapyId,
            "1",
            450.0,
            "mg",
            null,
            "t3",
            new DateTime(2014, 2, 21, 16, 0, 0)));

    //unplanned
    administrations.add(
        buildAdministration(
            "adm4",
            new DateTime(2014, 2, 21, 16, 30, 0),
            therapyId,
            "1",
            600.0,
            "mg",
            null,
            null,
            null));

    //past - should be filtered out
    administrations.add(
        buildAdministration(
            "adm5",
            new DateTime(2013, 1, 1, 10, 30, 0),
            therapyId,
            "1",
            600.0,
            "mg",
            null,
            null,
            null));

    final List<AdministrationTaskDto> tasks = new ArrayList<>();
    tasks.add(buildAdministrationTaskDto("t4", null, therapyId, new DateTime(2014, 2, 21, 18, 0, 0), 500.0, "mg")); //late
    tasks.add(buildAdministrationTaskDto("t5", null, therapyId, new DateTime(2014, 2, 21, 18, 30, 0), 500.0, "mg")); //due
    tasks.add(buildAdministrationTaskDto("t6", null, therapyId, new DateTime(2014, 2, 21, 19, 0, 0), 500.0, "mg")); //due
    tasks.add(buildAdministrationTaskDto("t7", null, therapyId, new DateTime(2014, 2, 21, 21, 0, 0), 500.0, "mg")); //planned

    final MedicationsCentralCaseDto centralCase = new MedicationsCentralCaseDto();
    centralCase.setCentralCaseId("1");
    final PatientDataForMedicationsDto patientData = new PatientDataForMedicationsDto(
        null,
        "James Smith",
        null,
        null,
        Gender.FEMALE,
        null,
        AllergiesStatus.NOT_CHECKED,
        null,
        centralCase);

    final List<TherapyRowDto> therapyTimelineRows =
        overviewContentProvider.buildTherapyRows(
            "1",
            inpatientPrescriptions,
            administrations,
            tasks,
            TherapySortTypeEnum.DESCRIPTION_ASC,
            false,
            Collections.emptyList(),
            patientData,
            Intervals.infiniteFrom(new DateTime(2014, 2, 10, 0, 0, 0)),
            roundsIntervalDto,
            null,
            new DateTime(2014, 2, 21, 18, 40, 0));

    assertEquals(1L, therapyTimelineRows.size());
    assertEquals(9L, therapyTimelineRows.get(0).getAdministrations().size());

    final StartAdministrationDto administration0 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(0);
    assertEquals("adm0", administration0.getAdministrationId());
    assertEquals("t0", administration0.getTaskId());
    assertEquals(new DateTime(2014, 2, 21, 8, 10, 0), administration0.getAdministrationTime());
    assertEquals(500.0, administration0.getAdministeredDose().getNumerator(), 0);
    assertEquals("mg", administration0.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration0.isDifferentFromOrder());
    assertEquals("comment123", administration0.getComment());

    final StartAdministrationDto administration1 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(1);
    assertEquals("adm1", administration1.getAdministrationId());
    assertEquals("t1", administration1.getTaskId());
    assertEquals(new DateTime(2014, 2, 21, 10, 40, 0), administration1.getAdministrationTime());
    assertEquals(500.0, administration1.getAdministeredDose().getNumerator(), 0);
    assertEquals("mg", administration1.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration1.isDifferentFromOrder());
    assertNull(administration1.getComment());

    final StartAdministrationDto administration2 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(2);
    assertEquals("adm2", administration2.getAdministrationId());
    assertEquals("t2", administration2.getTaskId());
    assertEquals(new DateTime(2014, 2, 21, 14, 50, 0), administration2.getAdministrationTime());
    assertEquals(500.0, administration2.getAdministeredDose().getNumerator(), 0);
    assertEquals("ug", administration2.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration2.isDifferentFromOrder());

    final StartAdministrationDto administration3 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(3);
    assertEquals("adm3", administration3.getAdministrationId());
    assertEquals("t3", administration3.getTaskId());
    assertEquals(new DateTime(2014, 2, 21, 15, 15, 0), administration3.getAdministrationTime());
    assertEquals(450.0, administration3.getAdministeredDose().getNumerator(), 0);
    assertEquals("mg", administration3.getAdministeredDose().getNumeratorUnit());

    final StartAdministrationDto administration4 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(4);
    assertEquals("adm4", administration4.getAdministrationId());
    assertNull(administration4.getTaskId());
    assertEquals(new DateTime(2014, 2, 21, 16, 30, 0), administration4.getAdministrationTime());
    assertEquals(600.0, administration4.getAdministeredDose().getNumerator(), 0);
    assertEquals("mg", administration4.getAdministeredDose().getNumeratorUnit());

    final StartAdministrationDto administration5 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(5);
    assertNull(administration5.getAdministrationId());
    assertEquals("t4", administration5.getTaskId());
    assertEquals(AdministrationStatusEnum.LATE, administration5.getAdministrationStatus());
    assertNull(administration5.getAdministrationTime());
    assertEquals(new DateTime(2014, 2, 21, 18, 0, 0), administration5.getPlannedTime());
    assertEquals(500.0, administration5.getPlannedDose().getNumerator(), 0);
    assertEquals("mg", administration5.getPlannedDose().getNumeratorUnit());

    final StartAdministrationDto administration6 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(6);
    assertNull(administration6.getAdministrationId());
    assertEquals("t5", administration6.getTaskId());
    assertEquals(AdministrationStatusEnum.DUE, administration6.getAdministrationStatus());
    assertNull(administration6.getAdministrationTime());
    assertEquals(new DateTime(2014, 2, 21, 18, 30, 0), administration6.getPlannedTime());
    assertEquals(500.0, administration6.getPlannedDose().getNumerator(), 0);
    assertEquals("mg", administration6.getPlannedDose().getNumeratorUnit());

    final StartAdministrationDto administration7 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(7);
    assertNull(administration7.getAdministrationId());
    assertEquals("t6", administration7.getTaskId());
    assertEquals(AdministrationStatusEnum.DUE, administration7.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 19, 0, 0), administration7.getPlannedTime());

    final StartAdministrationDto administration8 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(8);
    assertNull(administration8.getAdministrationId());
    assertEquals("t7", administration8.getTaskId());
    assertEquals(AdministrationStatusEnum.PLANNED, administration8.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 21, 0, 0), administration8.getPlannedTime());

    assertEquals(new DateTime(2014, 2, 20, 12, 0, 0), therapyTimelineRows.get(0).getOriginalTherapyStart());
  }

  @Test
  public void testBuildTherapyTimeline2()
  {
    final List<InpatientPrescription> inpatientPrescriptions = new ArrayList<>();
    final List<AdministrationTaskDto> tasks = new ArrayList<>();

    //original therapy
    final InpatientPrescription inpatientPrescription1 = MedicationsTestUtils.buildTestInpatientPrescription(
        "uid1::1",
        new DateTime(2014, 2, 20, 12, 0, 0),
        "1",
        "Medication order");
    inpatientPrescription1.getMedicationOrder()
        .getOrderDetails()
        .setOrderStartDateTime(DataValueUtils.getDateTime(new DateTime(2014, 2, 20, 12, 10, 0)));
    inpatientPrescriptions.add(inpatientPrescription1);

    final String therapyId1 = TherapyIdUtils.createTherapyId(inpatientPrescription1);

    final List<AdministrationDto> administrations = new ArrayList<>();
    administrations.add(
        buildAdministration(
            "adm0",
            new DateTime(2014, 2, 21, 8, 10, 0),
            therapyId1,
            "1",
            500.0,
            "mg",
            "comment123",
            "t0",
            new DateTime(2014, 2, 21, 8, 0, 0)));

    //modified therapy
    final InpatientPrescription inpatientPrescription2 = MedicationsTestUtils.buildTestInpatientPrescription(
        "uid2::1",
        new DateTime(2014, 2, 21, 12, 0, 0),
        "1",
        "Medication order");
    inpatientPrescription2.getMedicationOrder().getOrderDetails().setOrderStartDateTime(
        DataValueUtils.getDateTime(new DateTime(2014, 2, 21, 12, 10, 0)));
    inpatientPrescription2.getLinks().add(
        LinksEhrUtils.createLink(inpatientPrescription1.getUid(), "update", EhrLinkType.UPDATE));
    inpatientPrescription2.getLinks().add(
        LinksEhrUtils.createLink(inpatientPrescription1.getUid(), "origin", EhrLinkType.ORIGIN));
    inpatientPrescriptions.add(inpatientPrescription2);

    final String therapyId2 = TherapyIdUtils.createTherapyId(inpatientPrescription2);

    administrations.add(
        buildAdministration(
            "adm1",
            new DateTime(2014, 2, 21, 12, 40, 0),
            therapyId2,
            "1",
            1000.0,
            "mg",
            null,
            "t1",
            new DateTime(2014, 2, 21, 13, 0, 0)));

    Mockito
        .when(therapyEhrHandler.getOriginalTherapyStart(any(), any()))
        .thenReturn(new DateTime(2014, 2, 20, 12, 0, 0));

    final ConstantSimpleTherapyDto constantSimpleTherapyDto1 = new ConstantSimpleTherapyDto();
    constantSimpleTherapyDto1.setCompositionUid(inpatientPrescription1.getUid());
    constantSimpleTherapyDto1.setEhrOrderName(inpatientPrescription1.getMedicationOrder().getName().getValue());
    constantSimpleTherapyDto1.setTherapyDescription("description1");
    final MedicationDto medication1 = new MedicationDto();
    medication1.setId(1L);
    medication1.setName("Aspirin");
    constantSimpleTherapyDto1.setMedication(medication1);

    final ConstantSimpleTherapyDto constantSimpleTherapyDto2 = new ConstantSimpleTherapyDto();
    constantSimpleTherapyDto2.setCompositionUid(inpatientPrescription2.getUid());
    constantSimpleTherapyDto2.setEhrOrderName(inpatientPrescription2.getMedicationOrder().getName().getValue());
    constantSimpleTherapyDto2.setTherapyDescription("description2");
    final MedicationDto medication2 = new MedicationDto(2L, "Paracetamol");
    constantSimpleTherapyDto2.setMedication(medication2);

    Mockito
        .when(therapyConverter.convertToTherapyDto(
            inpatientPrescription2.getMedicationOrder(),
            inpatientPrescription2.getUid(),
            DataValueUtils.getDateTime(inpatientPrescription2.getContext().getStartTime())))
        .thenReturn(constantSimpleTherapyDto2);

    final MedicationDataDto medicationData1 = new MedicationDataDto();
    medicationData1.setMedication(medication1);
    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(1L))
        .thenReturn(medicationData1);

    final MedicationDataDto medicationData2 = new MedicationDataDto();
    medicationData2.setMedication(medication2);
    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(2L))
        .thenReturn(medicationData2);

    final MedicationsCentralCaseDto centralCase = new MedicationsCentralCaseDto();
    centralCase.setCentralCaseId("1");
    final PatientDataForMedicationsDto patientData = new PatientDataForMedicationsDto(
        null,
        "",
        null,
        null,
        Gender.FEMALE,
        null,
        AllergiesStatus.NOT_CHECKED,
        null,
        centralCase);

    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00
    final List<TherapyRowDto> therapyTimelineRows =
        overviewContentProvider.buildTherapyRows(
            "1",
            inpatientPrescriptions,
            administrations,
            tasks,
            TherapySortTypeEnum.DESCRIPTION_ASC,
            false,
            Collections.emptyList(),
            patientData,
            Intervals.INFINITE,
            roundsIntervalDto,
            null,
            new DateTime(2014, 2, 21, 18, 40, 0));

    assertEquals(1L, therapyTimelineRows.size());
    assertEquals(2L, therapyTimelineRows.get(0).getAdministrations().size());

    final StartAdministrationDto administration0 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(0);
    assertEquals("adm0", administration0.getAdministrationId());
    assertEquals("t0", administration0.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration0.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 8, 10, 0), administration0.getAdministrationTime());
    assertEquals(500.0, administration0.getAdministeredDose().getNumerator(), 0);
    assertEquals("mg", administration0.getAdministeredDose().getNumeratorUnit());
    assertEquals("comment123", administration0.getComment());

    final StartAdministrationDto administration1 =
        (StartAdministrationDto)therapyTimelineRows.get(0).getAdministrations().get(1);
    assertEquals("adm1", administration1.getAdministrationId());
    assertEquals("t1", administration1.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration1.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 12, 40, 0), administration1.getAdministrationTime());
    assertEquals(1000.0, administration1.getAdministeredDose().getNumerator(), 0);
    assertEquals("mg", administration1.getAdministeredDose().getNumeratorUnit());
    assertNull(administration1.getComment());

    assertEquals("uid2::1", therapyTimelineRows.get(0).getTherapy().getCompositionUid());
    assertEquals(new DateTime(2014, 2, 20, 12, 0, 0), therapyTimelineRows.get(0).getOriginalTherapyStart());
  }

  @Test
  public void testBuildTherapyTimeline3() //continuous infusion
  {
    final List<InpatientPrescription> inpatientPrescriptions = new ArrayList<>();
    final List<AdministrationTaskDto> tasks = new ArrayList<>();

    //original therapy
    final InpatientPrescription inpatientPrescription1 = MedicationsTestUtils.buildTestInpatientPrescription(
        "uid1::1",
        new DateTime(),
        "1",
        "Medication order");
    inpatientPrescription1.getMedicationOrder().getOrderDetails().setOrderStartDateTime(
        DataValueUtils.getDateTime(new DateTime(2014, 2, 20, 12, 10, 0)));
    inpatientPrescription1.getMedicationOrder().setAdministrationMethod(
        MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION.getDvCodedText());
    inpatientPrescriptions.add(inpatientPrescription1);

    final String therapyId1 = TherapyIdUtils.createTherapyId(inpatientPrescription1);

    final List<AdministrationDto> administrations = new ArrayList<>();
    administrations.add(
        buildAdministration(
            "adm0",
            new DateTime(2014, 2, 20, 12, 0, 0),
            therapyId1,
            "1",
            21.0,
            "mL/h",
            "comment123",
            null,
            null));

    //modified therapy
    final InpatientPrescription inpatientPrescription2 = MedicationsTestUtils.buildTestInpatientPrescription(
        "uid2::1",
        new DateTime(2014, 2, 21, 12, 0, 0),
        "1",
        "Medication order");
    inpatientPrescription2.getMedicationOrder().getOrderDetails().setOrderStartDateTime(
        DataValueUtils.getDateTime(new DateTime(2014, 2, 21, 12, 10, 0)));
    inpatientPrescription2.getMedicationOrder().setAdministrationMethod(
        MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION.getDvCodedText());
    inpatientPrescription2.getLinks().add(
        LinksEhrUtils.createLink(inpatientPrescription1.getUid(), "update", EhrLinkType.UPDATE));
    inpatientPrescription2.getLinks().add(
        LinksEhrUtils.createLink(inpatientPrescription1.getUid(), "origin", EhrLinkType.ORIGIN));
    inpatientPrescriptions.add(inpatientPrescription2);

    final String therapyId2 = TherapyIdUtils.createTherapyId(inpatientPrescription2);
    inpatientPrescription2.getLinks().add(
        LinksEhrUtils.createLink(inpatientPrescription2.getUid(), "update", EhrLinkType.UPDATE));
    inpatientPrescription2.getLinks().add(
        LinksEhrUtils.createLink(inpatientPrescription2.getUid(), "origin", EhrLinkType.ORIGIN));

    administrations.add(
        buildAdministration(
            "adm1",
            new DateTime(2014, 2, 21, 8, 10, 0),
            therapyId2,
            "1",
            25.0,
            "mL/h",
            null,
            null,
            null));

    Mockito
        .when(therapyEhrHandler.getOriginalTherapyStart(any(), any()))
        .thenReturn(new DateTime(2014, 2, 20, 12, 0, 0));

    final ConstantComplexTherapyDto constantComplexTherapyDto1 = new ConstantComplexTherapyDto();
    constantComplexTherapyDto1.setCompositionUid(inpatientPrescription1.getUid());
    constantComplexTherapyDto1.setEhrOrderName(inpatientPrescription1.getMedicationOrder().getName().getValue());
    constantComplexTherapyDto1.setTherapyDescription("description1");
    constantComplexTherapyDto1.setContinuousInfusion(true);

    final ConstantComplexTherapyDto constantComplexTherapyDto2 = new ConstantComplexTherapyDto();
    constantComplexTherapyDto2.setCompositionUid(inpatientPrescription2.getUid());
    constantComplexTherapyDto2.setEhrOrderName(inpatientPrescription2.getMedicationOrder().getName().getValue());
    constantComplexTherapyDto2.setTherapyDescription("description2");
    constantComplexTherapyDto2.setContinuousInfusion(true);

    Mockito
        .when(therapyConverter.convertToTherapyDto(
            inpatientPrescription2.getMedicationOrder(),
            inpatientPrescription2.getUid(),
            DataValueUtils.getDateTime(inpatientPrescription2.getContext().getStartTime())))
        .thenReturn(constantComplexTherapyDto2);

    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final MedicationsCentralCaseDto centralCase = new MedicationsCentralCaseDto();
    centralCase.setCentralCaseId("1");
    final PatientDataForMedicationsDto patientData = new PatientDataForMedicationsDto(
        null,
        "Janez Novak",
        null,
        null,
        Gender.FEMALE,
        null,
        AllergiesStatus.NOT_CHECKED,
        null,
        centralCase);

    final List<TherapyRowDto> therapyTimelineRows =
        overviewContentProvider.buildTherapyRows(
            "1",
            inpatientPrescriptions,
            administrations,
            tasks,
            TherapySortTypeEnum.DESCRIPTION_ASC,
            false,
            Collections.emptyList(),
            patientData,
            Intervals.INFINITE,
            roundsIntervalDto,
            new Locale("en"),
            new DateTime(2014, 2, 21, 18, 40, 0));

    assertEquals(1L, therapyTimelineRows.size());
    final TherapyRowDto timeline = therapyTimelineRows.get(0);
    assertEquals(2L, timeline.getAdministrations().size());

    final StartAdministrationDto administration0 = (StartAdministrationDto)timeline.getAdministrations().get(0);
    assertEquals("adm0", administration0.getAdministrationId());
    assertNull(administration0.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration0.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 20, 12, 0, 0), administration0.getAdministrationTime());
    assertEquals(21.0, administration0.getAdministeredDose().getNumerator(), 0);
    assertEquals("mL/h", administration0.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration0.isDifferentFromOrder());
    assertEquals("comment123", administration0.getComment());

    final StartAdministrationDto administration1 = (StartAdministrationDto)timeline.getAdministrations().get(1);
    assertEquals("adm1", administration1.getAdministrationId());
    assertNull(administration0.getTaskId());
    assertEquals(AdministrationStatusEnum.COMPLETED, administration1.getAdministrationStatus());
    assertEquals(new DateTime(2014, 2, 21, 8, 10, 0), administration1.getAdministrationTime());
    assertEquals(25, administration1.getAdministeredDose().getNumerator(), 0);
    assertEquals("mL/h", administration1.getAdministeredDose().getNumeratorUnit());
    assertFalse(administration1.isDifferentFromOrder());
    assertNull(administration1.getComment());

    assertEquals("uid2::1", timeline.getTherapy().getCompositionUid());
    assertTrue(timeline instanceof ContinuousInfusionTherapyRowDtoDto);
    assertEquals(25, ((ContinuousInfusionTherapyRowDtoDto)timeline).getCurrentInfusionRate(), 0);
    assertEquals(new DateTime(2014, 2, 20, 12, 0, 0), timeline.getOriginalTherapyStart());
  }

  @Test
  public void testRemoveOldCompletedTherapies1()
  {
    Mockito.when(medsProperties.getCompletedTherapiesShownMinutes()).thenReturn(5);

    final TherapyRowDto row1 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy1 = new ConstantSimpleTherapyDto();
    therapy1.setEnd(new DateTime(2019, 5, 3, 12, 0, 0));
    row1.setTherapy(therapy1);

    final TherapyRowDto row2 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy2 = new ConstantSimpleTherapyDto();
    therapy2.setEnd(new DateTime(2019, 5, 4, 20, 0, 0));
    row2.setTherapy(therapy2);

    final TherapyRowDto row3 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy3 = new ConstantSimpleTherapyDto();
    therapy3.setEnd(new DateTime(2019, 5, 5, 11, 57, 0));
    row3.setTherapy(therapy3);

    final TherapyRowDto row4 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy4 = new ConstantSimpleTherapyDto();
    row4.setTherapy(therapy4);

    final TherapyRowDto row5 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy5 = new ConstantSimpleTherapyDto();
    therapy5.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX));
    therapy5.setEnd(new DateTime(2019, 5, 4, 20, 0, 0));
    row5.setTherapy(therapy5);

    final List<TherapyRowDto> filteredRows = overviewContentProvider.removeOldCompletedTherapies(
        Lists.newArrayList(row1, row2, row3, row4, row5),
        new DateTime(2019, 5, 5, 12, 0, 0));
    assertEquals(3, filteredRows.size());
    assertTrue(filteredRows.contains(row3));
    assertTrue(filteredRows.contains(row4));
    assertTrue(filteredRows.contains(row5));
  }

  @Test
  public void testRemoveOldCompletedTherapies2()
  {
    Mockito.when(medsProperties.getCompletedTherapiesShownMinutes()).thenReturn(24*60);

    final TherapyRowDto row1 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy1 = new ConstantSimpleTherapyDto();
    therapy1.setEnd(new DateTime(2019, 5, 3, 12, 0, 0));
    row1.setTherapy(therapy1);

    final TherapyRowDto row2 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy2 = new ConstantSimpleTherapyDto();
    therapy2.setEnd(new DateTime(2019, 5, 4, 20, 0, 0));
    row2.setTherapy(therapy2);

    final TherapyRowDto row3 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy3 = new ConstantSimpleTherapyDto();
    therapy3.setEnd(new DateTime(2019, 5, 5, 11, 57, 0));
    row3.setTherapy(therapy3);

    final TherapyRowDto row4 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy4 = new ConstantSimpleTherapyDto();
    row4.setTherapy(therapy4);

    final TherapyRowDto row5 = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy5 = new ConstantSimpleTherapyDto();
    therapy5.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX));
    therapy5.setEnd(new DateTime(2019, 5, 4, 20, 0, 0));
    row5.setTherapy(therapy5);

    final List<TherapyRowDto> filteredRows = overviewContentProvider.removeOldCompletedTherapies(
        Lists.newArrayList(row1, row2, row3, row4, row5),
        new DateTime(2019, 5, 5, 12, 0, 0));
    assertEquals(4, filteredRows.size());
    assertTrue(filteredRows.contains(row2));
    assertTrue(filteredRows.contains(row3));
    assertTrue(filteredRows.contains(row4));
    assertTrue(filteredRows.contains(row5));
  }

  @Test
  public void testRemoveOldCompletedTherapies3()
  {
    Mockito.when(medsProperties.getCompletedTherapiesShownMinutes()).thenReturn(5);

    final TherapyRowDto row = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setEnd(new DateTime(2019, 5, 5, 10, 0, 0));
    row.setTherapy(therapy);
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setAdministrationStatus(AdministrationStatusEnum.PLANNED);
    administration.setPlannedTime(new DateTime(2019, 5, 5, 10, 0, 0));
    row.setAdministrations(Lists.newArrayList(administration));

    final List<TherapyRowDto> filteredRows = overviewContentProvider.removeOldCompletedTherapies(
        Lists.newArrayList(row),
        new DateTime(2019, 5, 5, 12, 0, 0));
    assertTrue(filteredRows.contains(row));
  }

  @Test
  public void testRemoveOldCompletedTherapies4()
  {
    Mockito.when(medsProperties.getCompletedTherapiesShownMinutes()).thenReturn(5);

    final TherapyRowDto row = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setEnd(new DateTime(2019, 5, 5, 10, 0, 0));
    row.setTherapy(therapy);
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);
    administration.setPlannedTime(new DateTime(2019, 5, 5, 10, 0, 0));
    row.setAdministrations(Lists.newArrayList(administration));

    final List<TherapyRowDto> filteredRows = overviewContentProvider.removeOldCompletedTherapies(
        Lists.newArrayList(row),
        new DateTime(2019, 5, 5, 12, 0, 0));
    assertTrue(filteredRows.isEmpty());
  }

  @Test
  public void testRemoveOldCompletedTherapies5()
  {
    Mockito.when(medsProperties.getCompletedTherapiesShownMinutes()).thenReturn(5);

    final TherapyRowDto row = new TherapyRowDto();
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setEnd(new DateTime(2019, 5, 5, 10, 0, 0));
    row.setTherapy(therapy);
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setAdministrationStatus(AdministrationStatusEnum.PLANNED);
    administration.setPlannedTime(new DateTime(2019, 5, 5, 2, 0, 0));
    row.setAdministrations(Lists.newArrayList(administration));

    final List<TherapyRowDto> filteredRows = overviewContentProvider.removeOldCompletedTherapies(
        Lists.newArrayList(row),
        new DateTime(2019, 5, 5, 12, 0, 0));
    assertTrue(filteredRows.isEmpty());
  }

  @Test
  public void testSortTherapies()
  {
    final List<TherapyRowDto> timelineRows = new ArrayList<>();

    final TherapyRowDto row = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setTherapyDescription("ZZAspirin");
    therapy.setLinkName("A1");
    therapy.setCreatedTimestamp(new DateTime(2015, 11, 5, 0, 0));
    row.setTherapy(therapy);
    timelineRows.add(row);

    final TherapyRowDto row2 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy2 = new ConstantComplexTherapyDto();
    therapy2.setTherapyDescription("Cspirin");
    therapy2.setLinkName("B1");
    therapy2.setCreatedTimestamp(new DateTime(2015, 11, 4, 0, 0));
    row2.setTherapy(therapy2);
    timelineRows.add(row2);

    final TherapyRowDto row3 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy3 = new ConstantComplexTherapyDto();
    therapy3.setTherapyDescription("Aspirin");
    therapy3.setLinkName("B2");
    therapy3.setCreatedTimestamp(new DateTime(2015, 11, 3, 0, 0));
    row3.setTherapy(therapy3);
    timelineRows.add(row3);

    final TherapyRowDto row4 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy4 = new ConstantComplexTherapyDto();
    therapy4.setTherapyDescription("AAspirin");
    therapy4.setCreatedTimestamp(new DateTime(2015, 11, 11, 0, 0));
    row4.setTherapy(therapy4);
    timelineRows.add(row4);

    final TherapyRowDto row5 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy5 = new ConstantComplexTherapyDto();
    therapy5.setTherapyDescription("Bspirin");
    therapy5.setCreatedTimestamp(new DateTime(2015, 11, 5, 0, 0));
    row5.setTherapy(therapy5);
    timelineRows.add(row5);

    final TherapyRowDto row6 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy6 = new ConstantComplexTherapyDto();
    therapy6.setTherapyDescription("Azlekadol");
    therapy6.setLinkName("C1");
    therapy6.setCreatedTimestamp(new DateTime(2015, 11, 1, 0, 0));
    row6.setTherapy(therapy6);
    timelineRows.add(row6);

    final TherapyRowDto row7 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy7 = new ConstantComplexTherapyDto();
    therapy7.setTherapyDescription("Bzlekadol");
    therapy7.setCreatedTimestamp(new DateTime(2015, 11, 6, 0, 0));
    row7.setTherapy(therapy7);
    timelineRows.add(row7);

    final TherapyRowDto row8 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy8 = new ConstantComplexTherapyDto();
    therapy8.setTherapyDescription("Czlekadol");
    therapy8.setLinkName("C2");
    therapy8.setCreatedTimestamp(new DateTime(2015, 11, 1, 0, 0));
    row8.setTherapy(therapy8);
    timelineRows.add(row8);

    final TherapyRowDto row10 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy10 = new ConstantComplexTherapyDto();
    therapy10.setTherapyDescription("BAlekadol");
    therapy10.setLinkName("C3");
    therapy10.setCreatedTimestamp(new DateTime(2015, 11, 1, 0, 0));
    row10.setTherapy(therapy10);
    timelineRows.add(row10);

    final TherapyRowDto row9 = new TherapyRowDto();
    final ConstantComplexTherapyDto therapy9 = new ConstantComplexTherapyDto();
    therapy9.setTherapyDescription("ZZZlekadol");
    therapy9.setLinkName("D2");
    therapy9.setCreatedTimestamp(new DateTime(2015, 10, 5, 0, 0));
    row9.setTherapy(therapy9);
    timelineRows.add(row9);

    final TherapySortTypeEnum therapySortTypeEnumTimeDesc = TherapySortTypeEnum.CREATED_TIME_DESC;
    overviewContentProvider.sortTherapyRowsAndAdministrations(timelineRows, therapySortTypeEnumTimeDesc);

    assertEquals("AAspirin", timelineRows.get(0).getTherapy().getTherapyDescription());
    assertEquals("Azlekadol", timelineRows.get(6).getTherapy().getTherapyDescription());
    assertEquals("C1", timelineRows.get(6).getTherapy().getLinkName());
    assertEquals("Czlekadol", timelineRows.get(7).getTherapy().getTherapyDescription());
    assertEquals("C2", timelineRows.get(7).getTherapy().getLinkName());
    assertEquals("BAlekadol", timelineRows.get(8).getTherapy().getTherapyDescription());
    assertEquals("C3", timelineRows.get(8).getTherapy().getLinkName());
    assertEquals("Bspirin", timelineRows.get(2).getTherapy().getTherapyDescription());
    assertEquals("Bzlekadol", timelineRows.get(1).getTherapy().getTherapyDescription());
    assertEquals("Cspirin", timelineRows.get(4).getTherapy().getTherapyDescription());
    assertEquals("B1", timelineRows.get(4).getTherapy().getLinkName());
    assertEquals("Aspirin", timelineRows.get(5).getTherapy().getTherapyDescription());
    assertEquals("B2", timelineRows.get(5).getTherapy().getLinkName());
    assertEquals("ZZAspirin", timelineRows.get(3).getTherapy().getTherapyDescription());
    assertEquals("A1", timelineRows.get(3).getTherapy().getLinkName());
    assertEquals("ZZZlekadol", timelineRows.get(9).getTherapy().getTherapyDescription());
    assertEquals("D2", timelineRows.get(9).getTherapy().getLinkName());

    final TherapySortTypeEnum therapySortTypeEnumTimeAsc = TherapySortTypeEnum.CREATED_TIME_ASC;
    overviewContentProvider.sortTherapyRowsAndAdministrations(timelineRows, therapySortTypeEnumTimeAsc);

    assertEquals("AAspirin", timelineRows.get(9).getTherapy().getTherapyDescription());
    assertEquals("Azlekadol", timelineRows.get(1).getTherapy().getTherapyDescription());
    assertEquals("C1", timelineRows.get(1).getTherapy().getLinkName());
    assertEquals("Czlekadol", timelineRows.get(2).getTherapy().getTherapyDescription());
    assertEquals("C2", timelineRows.get(2).getTherapy().getLinkName());
    assertEquals("BAlekadol", timelineRows.get(3).getTherapy().getTherapyDescription());
    assertEquals("C3", timelineRows.get(3).getTherapy().getLinkName());
    assertEquals("Bspirin", timelineRows.get(6).getTherapy().getTherapyDescription());
    assertEquals("Bzlekadol", timelineRows.get(8).getTherapy().getTherapyDescription());
    assertEquals("Cspirin", timelineRows.get(4).getTherapy().getTherapyDescription());
    assertEquals("B1", timelineRows.get(4).getTherapy().getLinkName());
    assertEquals("Aspirin", timelineRows.get(5).getTherapy().getTherapyDescription());
    assertEquals("B2", timelineRows.get(5).getTherapy().getLinkName());
    assertEquals("ZZAspirin", timelineRows.get(7).getTherapy().getTherapyDescription());
    assertEquals("A1", timelineRows.get(7).getTherapy().getLinkName());
    assertEquals("ZZZlekadol", timelineRows.get(0).getTherapy().getTherapyDescription());
    assertEquals("D2", timelineRows.get(0).getTherapy().getLinkName());

    final TherapySortTypeEnum therapySortTypeEnumDescAsc = TherapySortTypeEnum.DESCRIPTION_ASC;
    overviewContentProvider.sortTherapyRowsAndAdministrations(timelineRows, therapySortTypeEnumDescAsc);

    assertEquals("AAspirin", timelineRows.get(0).getTherapy().getTherapyDescription());
    assertEquals("Azlekadol", timelineRows.get(1).getTherapy().getTherapyDescription());
    assertEquals("C1", timelineRows.get(1).getTherapy().getLinkName());
    assertEquals("Czlekadol", timelineRows.get(2).getTherapy().getTherapyDescription());
    assertEquals("C2", timelineRows.get(2).getTherapy().getLinkName());
    assertEquals("BAlekadol", timelineRows.get(3).getTherapy().getTherapyDescription());
    assertEquals("C3", timelineRows.get(3).getTherapy().getLinkName());
    assertEquals("Bspirin", timelineRows.get(4).getTherapy().getTherapyDescription());
    assertEquals("Bzlekadol", timelineRows.get(5).getTherapy().getTherapyDescription());
    assertEquals("Cspirin", timelineRows.get(6).getTherapy().getTherapyDescription());
    assertEquals("B1", timelineRows.get(6).getTherapy().getLinkName());
    assertEquals("Aspirin", timelineRows.get(7).getTherapy().getTherapyDescription());
    assertEquals("B2", timelineRows.get(7).getTherapy().getLinkName());
    assertEquals("ZZAspirin", timelineRows.get(8).getTherapy().getTherapyDescription());
    assertEquals("A1", timelineRows.get(8).getTherapy().getLinkName());
    assertEquals("ZZZlekadol", timelineRows.get(9).getTherapy().getTherapyDescription());
    assertEquals("D2", timelineRows.get(9).getTherapy().getLinkName());

    final TherapySortTypeEnum therapySortTypeEnumDescDes = TherapySortTypeEnum.DESCRIPTION_DESC;
    overviewContentProvider.sortTherapyRowsAndAdministrations(timelineRows, therapySortTypeEnumDescDes);

    assertEquals("AAspirin", timelineRows.get(9).getTherapy().getTherapyDescription());
    assertEquals("Azlekadol", timelineRows.get(6).getTherapy().getTherapyDescription());
    assertEquals("C1", timelineRows.get(6).getTherapy().getLinkName());
    assertEquals("Czlekadol", timelineRows.get(7).getTherapy().getTherapyDescription());
    assertEquals("C2", timelineRows.get(7).getTherapy().getLinkName());
    assertEquals("BAlekadol", timelineRows.get(8).getTherapy().getTherapyDescription());
    assertEquals("C3", timelineRows.get(8).getTherapy().getLinkName());
    assertEquals("Bspirin", timelineRows.get(5).getTherapy().getTherapyDescription());
    assertEquals("Bzlekadol", timelineRows.get(4).getTherapy().getTherapyDescription());
    assertEquals("Cspirin", timelineRows.get(2).getTherapy().getTherapyDescription());
    assertEquals("B1", timelineRows.get(2).getTherapy().getLinkName());
    assertEquals("Aspirin", timelineRows.get(3).getTherapy().getTherapyDescription());
    assertEquals("B2", timelineRows.get(3).getTherapy().getLinkName());
    assertEquals("ZZAspirin", timelineRows.get(1).getTherapy().getTherapyDescription());
    assertEquals("A1", timelineRows.get(1).getTherapy().getLinkName());
    assertEquals("ZZZlekadol", timelineRows.get(0).getTherapy().getTherapyDescription());
    assertEquals("D2", timelineRows.get(0).getTherapy().getLinkName());
  }

  @Test
  public void testGetTherapyReviewedUntilReviewedInRounds()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationManagement> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.START, new DateTime(2016, 7, 1, 11, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2016, 7, 1, 11, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2016, 7, 2, 12, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2016, 7, 3, 13, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.MODIFY_EXISTING, new DateTime(2016, 7, 3, 22, 0)));

    final DateTime therapyReviewedUntil = overviewContentProvider.getTherapyReviewedUntil(
        null,
        actions,
        roundsIntervalDto);
    assertEquals(new DateTime(2016, 7, 4, 17, 0), therapyReviewedUntil);
  }

  @Test
  public void testGetTherapyReviewedUntilReviewedInRoundsNextTwoDaysWorkFree()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationManagement> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2016, 7, 4, 13, 0)));

    final DateTime therapyReviewedUntil = overviewContentProvider.getTherapyReviewedUntil(
        null,
        actions,
        roundsIntervalDto);
    assertEquals(new DateTime(2016, 7, 7, 17, 0), therapyReviewedUntil);
  }

  @Test
  public void testGetTherapyReviewedBeforeRounds()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationManagement> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2016, 7, 4, 5, 0)));

    final DateTime therapyReviewedUntil = overviewContentProvider.getTherapyReviewedUntil(
        null,
        actions,
        roundsIntervalDto);
    assertEquals(new DateTime(2016, 7, 4, 17, 0), therapyReviewedUntil);
  }

  @Test
  public void testGetTherapyReviewedBeforeRoundsNextTwoDaysWorkFree()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationManagement> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2016, 7, 5, 5, 0)));

    final DateTime therapyReviewedUntil = overviewContentProvider.getTherapyReviewedUntil(
        null,
        actions,
        roundsIntervalDto);
    assertEquals(new DateTime(2016, 7, 7, 17, 0), therapyReviewedUntil);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionCancelled()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationManagement> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 15, 10, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.CANCEL, new DateTime(2017, 1, 15, 12, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 13, 0),
        new DateTime(2017, 1, 16, 17, 0));
    assertEquals(TherapyStatusEnum.CANCELLED, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionAborted()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationManagement> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 15, 10, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.ABORT, new DateTime(2017, 1, 15, 12, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 13, 0),
        new DateTime(2017, 1, 16, 17, 0));
    assertEquals(TherapyStatusEnum.ABORTED, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionSuspended()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationManagement> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 15, 10, 0)));
    actions.add(buildMedicationAction(MedicationActionEnum.SUSPEND, new DateTime(2017, 1, 15, 12, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 13, 0),
        new DateTime(2017, 1, 16, 17, 0));
    assertEquals(TherapyStatusEnum.SUSPENDED, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionFuture()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationManagement> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 15, 10, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 16, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 13, 0),
        new DateTime(2017, 1, 16, 17, 0));
    assertEquals(TherapyStatusEnum.FUTURE, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionVeryLateSameDay()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationManagement> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 14, 10, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 18, 0),
        new DateTime(2017, 1, 15, 17, 0));
    assertEquals(TherapyStatusEnum.VERY_LATE, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionVeryLatePastDay()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationManagement> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 12, 10, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 12, 0),
        new DateTime(2017, 1, 13, 17, 0));
    assertEquals(TherapyStatusEnum.VERY_LATE, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionLate()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationManagement> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 14, 10, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 16, 30),
        new DateTime(2017, 1, 15, 17, 0));
    assertEquals(TherapyStatusEnum.LATE, status);
  }

  @Test
  public void testGetTherapyStatusFromMedicationActionNormal()
  {
    final RoundsIntervalDto roundsIntervalDto = MedicationsTestUtils.getTestRoundsIntervalDto(); //7:00 - 17:00

    final List<MedicationManagement> actions = new ArrayList<>();
    actions.add(buildMedicationAction(MedicationActionEnum.REVIEW, new DateTime(2017, 1, 14, 10, 0)));

    final TherapyStatusEnum status = overviewContentProvider.getTherapyStatusFromMedicationAction(
        actions,
        new DateTime(2017, 1, 14, 12, 0),
        roundsIntervalDto,
        new DateTime(2017, 1, 15, 10, 30),
        new DateTime(2017, 1, 15, 17, 0));
    assertEquals(TherapyStatusEnum.NORMAL, status);
  }

  @Test
  public void testExtractStatusReasonWithAllFields()
  {
    final TherapyChangeReasonDto therapyChangeReasonDto = new TherapyChangeReasonDto();
    therapyChangeReasonDto.setChangeReason(new CodedNameDto("9", "Suspended"));
    therapyChangeReasonDto.setComment("Suspended because of disease");

    final String result = overviewContentProvider.extractStatusReason(therapyChangeReasonDto);
    assertThat(result).isEqualTo("Suspended because of disease");
  }

  @Test
  public void testExtractStatusReasonWithOnlyCodeName()
  {
    final TherapyChangeReasonDto therapyChangeReasonDto = new TherapyChangeReasonDto();
    therapyChangeReasonDto.setChangeReason(new CodedNameDto("9", "Suspended"));

    final String result = overviewContentProvider.extractStatusReason(therapyChangeReasonDto);
    assertThat(result).isEqualTo("Suspended");
  }

  @Test
  public void testExtractStatusReasonEmpty()
  {
    final TherapyChangeReasonDto therapyChangeReasonDto = new TherapyChangeReasonDto();

    final String result = overviewContentProvider.extractStatusReason(therapyChangeReasonDto);
    assertThat(result).isEqualTo(null);
  }

  @Test
  public void testGetTherapyConsecutiveDay0()
  {
    //active therapy, today view, start with 0
    final DateTime pastTherapyStart = null;
    final DateTime therapyStart = new DateTime(2019, 2, 1, 8, 0);
    final DateTime therapyEnd = null;
    final Interval therapyDay = Intervals.wholeDay(new DateTime(2019, 2, 4, 0, 0));
    final DateTime currentTime = new DateTime(2019, 2, 4, 6, 0);
    Mockito.when(medsProperties.getAntimicrobialDaysCountStartsWithOne()).thenReturn(false);

    final int therapyConsecutiveDay = overviewContentProvider.getTherapyConsecutiveDay(
        pastTherapyStart,
        therapyStart,
        therapyEnd,
        therapyDay,
        currentTime);
    assertEquals(2, therapyConsecutiveDay);
  }

  @Test
  public void testGetTherapyConsecutiveDay1()
  {
    //active therapy, today view, start with 0
    final DateTime pastTherapyStart = null;
    final DateTime therapyStart = new DateTime(2019, 2, 1, 8, 0);
    final DateTime therapyEnd = null;
    final Interval therapyDay = Intervals.wholeDay(new DateTime(2019, 2, 4, 0, 0));
    final DateTime currentTime = new DateTime(2019, 2, 4, 12, 0);
    Mockito.when(medsProperties.getAntimicrobialDaysCountStartsWithOne()).thenReturn(false);

    final int therapyConsecutiveDay = overviewContentProvider.getTherapyConsecutiveDay(
        pastTherapyStart,
        therapyStart,
        therapyEnd,
        therapyDay,
        currentTime);
    assertEquals(3, therapyConsecutiveDay);
  }

  @Test
  public void testGetTherapyConsecutiveDay2()
  {
    //active therapy, today view, start with 1
    final DateTime pastTherapyStart = null;
    final DateTime therapyStart = new DateTime(2019, 2, 1, 8, 0);
    final DateTime therapyEnd = null;
    final Interval therapyDay = Intervals.wholeDay(new DateTime(2019, 2, 4, 0, 0));
    final DateTime currentTime = new DateTime(2019, 2, 4, 12, 0);
    Mockito.when(medsProperties.getAntimicrobialDaysCountStartsWithOne()).thenReturn(true);

    final int therapyConsecutiveDay = overviewContentProvider.getTherapyConsecutiveDay(
        pastTherapyStart,
        therapyStart,
        therapyEnd,
        therapyDay,
        currentTime);
    assertEquals(4, therapyConsecutiveDay);
  }

  @Test
  public void testGetTherapyConsecutiveDay3()
  {
    //active therapy, past view, start with 0
    final DateTime pastTherapyStart = null;
    final DateTime therapyStart = new DateTime(2019, 2, 1, 8, 0);
    final DateTime therapyEnd = null;
    final Interval therapyDay = Intervals.wholeDay(new DateTime(2019, 2, 3, 0, 0));
    final DateTime currentTime = new DateTime(2019, 2, 4, 12, 0);
    medsProperties.setAntimicrobialDaysCountStartsWithOne(false);

    final int therapyConsecutiveDay = overviewContentProvider.getTherapyConsecutiveDay(
        pastTherapyStart,
        therapyStart,
        therapyEnd,
        therapyDay,
        currentTime);
    assertEquals(2, therapyConsecutiveDay);
  }

  @Test
  public void testGetTherapyConsecutiveDay4()
  {
    //active therapy, today view, with pastTherapyStart, start with 0
    final DateTime pastTherapyStart = new DateTime(2019, 1, 28, 8, 0);
    final DateTime therapyStart = new DateTime(2019, 2, 1, 8, 0);
    final DateTime therapyEnd = null;
    final Interval therapyDay = Intervals.wholeDay(new DateTime(2019, 2, 4, 0, 0));
    final DateTime currentTime = new DateTime(2019, 2, 4, 12, 0);
    Mockito.when(medsProperties.getAntimicrobialDaysCountStartsWithOne()).thenReturn(false);

    final int therapyConsecutiveDay = overviewContentProvider.getTherapyConsecutiveDay(
        pastTherapyStart,
        therapyStart,
        therapyEnd,
        therapyDay,
        currentTime);
    assertEquals(7, therapyConsecutiveDay);
  }

  @Test
  public void testGetTherapyConsecutiveDay5()
  {
    //active therapy, past view, with pastTherapyStart, start with 0
    final DateTime pastTherapyStart = new DateTime(2019, 1, 28, 8, 0);
    final DateTime therapyStart = new DateTime(2019, 2, 1, 8, 0);
    final DateTime therapyEnd = null;
    final Interval therapyDay = Intervals.wholeDay(new DateTime(2019, 2, 3, 0, 0));
    final DateTime currentTime = new DateTime(2019, 2, 4, 12, 0);
    medsProperties.setAntimicrobialDaysCountStartsWithOne(false);

    final int therapyConsecutiveDay = overviewContentProvider.getTherapyConsecutiveDay(
        pastTherapyStart,
        therapyStart,
        therapyEnd,
        therapyDay,
        currentTime);
    assertEquals(6, therapyConsecutiveDay);
  }

  @Test
  public void testGetTherapyConsecutiveDay6()
  {
    //stopped therapy, today view, start with 0
    final DateTime pastTherapyStart = null;
    final DateTime therapyStart = new DateTime(2019, 2, 1, 8, 0);
    final DateTime therapyEnd = new DateTime(2019, 2, 3, 12, 0);
    final Interval therapyDay = Intervals.wholeDay(new DateTime(2019, 2, 4, 0, 0));
    final DateTime currentTime = new DateTime(2019, 2, 4, 12, 0);
    Mockito.when(medsProperties.getAntimicrobialDaysCountStartsWithOne()).thenReturn(false);

    final int therapyConsecutiveDay = overviewContentProvider.getTherapyConsecutiveDay(
        pastTherapyStart,
        therapyStart,
        therapyEnd,
        therapyDay,
        currentTime);
    assertEquals(2, therapyConsecutiveDay);
  }

  @Test
  public void testGetTherapyConsecutiveDay7()
  {
    //stopped therapy, past view, start with 0
    final DateTime pastTherapyStart = null;
    final DateTime therapyStart = new DateTime(2019, 2, 1, 8, 0);
    final DateTime therapyEnd = new DateTime(2019, 2, 3, 12, 0);
    final Interval therapyDay = Intervals.wholeDay(new DateTime(2019, 2, 4, 0, 0));
    final DateTime currentTime = new DateTime(2019, 2, 5, 12, 0);
    Mockito.when(medsProperties.getAntimicrobialDaysCountStartsWithOne()).thenReturn(false);

    final int therapyConsecutiveDay = overviewContentProvider.getTherapyConsecutiveDay(
        pastTherapyStart,
        therapyStart,
        therapyEnd,
        therapyDay,
        currentTime);
    assertEquals(2, therapyConsecutiveDay);
  }

  @Test
  public void testGetTherapyConsecutiveDay8()
  {
    //stopped therapy, past view, start with 0, therapy end in the future
    final DateTime pastTherapyStart = null;
    final DateTime therapyStart = new DateTime(2019, 2, 1, 8, 0);
    final DateTime therapyEnd = new DateTime(2019, 2, 5, 10, 0);
    final Interval therapyDay = Intervals.wholeDay(new DateTime(2019, 2, 3, 0, 0));
    final DateTime currentTime = new DateTime(2019, 2, 5, 12, 0);
    Mockito.when(medsProperties.getAntimicrobialDaysCountStartsWithOne()).thenReturn(false);

    final int therapyConsecutiveDay = overviewContentProvider.getTherapyConsecutiveDay(
        pastTherapyStart,
        therapyStart,
        therapyEnd,
        therapyDay,
        currentTime);
    assertEquals(2, therapyConsecutiveDay);
  }

  @Test
  public void testGetTherapyConsecutiveDay9()
  {
    //active therapy, today view, start with 0, therapy end in the future
    final DateTime pastTherapyStart = null;
    final DateTime therapyStart = new DateTime(2019, 2, 4, 20, 0);
    final DateTime therapyEnd = new DateTime(2019, 2, 6, 22, 0);
    final Interval therapyDay = Intervals.wholeDay(new DateTime(2019, 2, 4, 0, 0));
    final DateTime currentTime = new DateTime(2019, 2, 4, 12, 0);
    Mockito.when(medsProperties.getAntimicrobialDaysCountStartsWithOne()).thenReturn(false);

    final int therapyConsecutiveDay = overviewContentProvider.getTherapyConsecutiveDay(
        pastTherapyStart,
        therapyStart,
        therapyEnd,
        therapyDay,
        currentTime);
    assertEquals(0, therapyConsecutiveDay);
  }

  @Test
  public void testGetPrescriptionGroupAntimicrobial()
  {
    final MedicationDataDto medication = new MedicationDataDto();
    medication.getProperties().add(new MedicationPropertyDto(5L, MedicationPropertyType.ANTIBIOTIC, "Antibiotic"));
    Mockito.when(medicationsValueHolderProvider.getMedicationData(10L))
        .thenReturn(medication);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setMedication(new MedicationDto(10L, "Medication"));
    assertEquals(PrescriptionGroupEnum.ANTIMICROBIALS, overviewContentProvider.getPrescriptionGroup(therapy));
  }

  @Test
  public void testGetPrescriptionGroupAntimicrobial2()
  {
    final MedicationDataDto medication = new MedicationDataDto();
    medication.getProperties().add(new MedicationPropertyDto(5L, MedicationPropertyType.ANTIBIOTIC, "Antibiotic"));
    medication.getProperties().add(new MedicationPropertyDto(5L, MedicationPropertyType.ANTICOAGULANT, "Anticoagulant"));
    Mockito.when(medicationsValueHolderProvider.getMedicationData(10L))
        .thenReturn(medication);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setMedication(new MedicationDto(10L, "Medication"));
    therapy.setWhenNeeded(true);
    assertEquals(PrescriptionGroupEnum.ANTIMICROBIALS, overviewContentProvider.getPrescriptionGroup(therapy));
  }

  @Test
  public void testGetPrescriptionGroupAnticoagulant()
  {
    final MedicationDataDto medication = new MedicationDataDto();
    medication.getProperties().add(new MedicationPropertyDto(5L, MedicationPropertyType.ANTICOAGULANT, "Anticoagulant"));
    Mockito.when(medicationsValueHolderProvider.getMedicationData(10L))
        .thenReturn(medication);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setMedication(new MedicationDto(10L, "Medication"));
    assertEquals(PrescriptionGroupEnum.ANTICOAGULANTS, overviewContentProvider.getPrescriptionGroup(therapy));
  }

  @Test
  public void testGetPrescriptionGroupAnticoagulant2()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    medicationData.getProperties().add(new MedicationPropertyDto(5L, MedicationPropertyType.ANTICOAGULANT, "Anticoagulant"));
    Mockito.when(medicationsValueHolderProvider.getMedicationData(10L))
        .thenReturn(medicationData);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setMedication(new MedicationDto(10L, "Medication"));
    therapy.setWhenNeeded(true);
    assertEquals(PrescriptionGroupEnum.ANTICOAGULANTS, overviewContentProvider.getPrescriptionGroup(therapy));
  }

  @Test
  public void testGetPrescriptionGroupInsulins()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    medicationData.getProperties().add(new MedicationPropertyDto(5L, MedicationPropertyType.INSULIN, "Insulin"));
    Mockito.when(medicationsValueHolderProvider.getMedicationData(10L))
        .thenReturn(medicationData);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setMedication(new MedicationDto(10L, "Medication"));
    assertEquals(PrescriptionGroupEnum.INSULINS, overviewContentProvider.getPrescriptionGroup(therapy));
  }

  @Test
  public void testGetPrescriptionGroupFluids()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    medicationData.getProperties().add(new MedicationPropertyDto(5L, MedicationPropertyType.FLUID, "Fluids"));
    Mockito.when(medicationsValueHolderProvider.getMedicationData(10L))
        .thenReturn(medicationData);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setMedication(new MedicationDto(10L, "Medication"));
    assertEquals(PrescriptionGroupEnum.FLUIDS, overviewContentProvider.getPrescriptionGroup(therapy));
  }

  @Test
  public void testGetPrescriptionGroupBloodProduct()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto(10L, "Medication");
    medication.setMedicationType(MedicationTypeEnum.BLOOD_PRODUCT);
    medicationData.setMedication(medication);
    Mockito.when(medicationsValueHolderProvider.getMedicationData(10L))
        .thenReturn(medicationData);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setMedication(medication);
    assertEquals(PrescriptionGroupEnum.BLOOD_PRODUCTS, overviewContentProvider.getPrescriptionGroup(therapy));
  }

  @Test
  public void testGetPrescriptionGroupMedicinalGas()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto(10L, "Medication");
    medication.setMedicationType(MedicationTypeEnum.MEDICINAL_GAS);
    medicationData.setMedication(medication);
    Mockito.when(medicationsValueHolderProvider.getMedicationData(10L))
        .thenReturn(medicationData);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setMedication(medication);
    assertEquals(PrescriptionGroupEnum.MEDICINAL_GASES, overviewContentProvider.getPrescriptionGroup(therapy));
  }

  @Test
  public void testGetPrescriptionGroupStatDoses()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto(10L, "Medication");
    medicationData.setMedication(medication);

    Mockito.when(medicationsValueHolderProvider.getMedicationData(10L))
        .thenReturn(medicationData);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX));
    therapy.setMedication(medication);
    assertEquals(PrescriptionGroupEnum.STAT_DOSES, overviewContentProvider.getPrescriptionGroup(therapy));
  }

  @Test
  public void testGetPrescriptionGroupPrn()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto(10L, "Medication");
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);
    medicationData.setMedication(medication);
    Mockito.when(medicationsValueHolderProvider.getMedicationData(10L))
        .thenReturn(medicationData);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setMedication(new MedicationDto(10L, "Medication"));
    therapy.setWhenNeeded(true);
    assertEquals(PrescriptionGroupEnum.PRN, overviewContentProvider.getPrescriptionGroup(therapy));
  }

  @Test
  public void testGetPrescriptionGroupRegular()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto(10L, "Medication");
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);
    medicationData.setMedication(medication);
    Mockito.when(medicationsValueHolderProvider.getMedicationData(10L))
        .thenReturn(medicationData);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setMedication(new MedicationDto(10L, "Medication"));
    assertEquals(PrescriptionGroupEnum.REGULAR, overviewContentProvider.getPrescriptionGroup(therapy));
  }

  @Test
  public void testGetPrescriptionGroupAll()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto(10L, "Medication");
    medication.setMedicationType(MedicationTypeEnum.MEDICINAL_GAS);
    medicationData.setMedication(medication);
    medicationData.getProperties().add(new MedicationPropertyDto(4L, MedicationPropertyType.ANTIBIOTIC, "Antibiotic"));
    medicationData.getProperties().add(new MedicationPropertyDto(5L, MedicationPropertyType.ANTICOAGULANT, "Anticoagulant"));
    medicationData.getProperties().add(new MedicationPropertyDto(6L, MedicationPropertyType.INSULIN, "Insulin"));
    medicationData.getProperties().add(new MedicationPropertyDto(7L, MedicationPropertyType.FLUID, "Fluids"));

    Mockito.when(medicationsValueHolderProvider.getMedicationData(10L))
        .thenReturn(medicationData);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();

    therapy.setMedication(new MedicationDto(10L, "Medication"));
    therapy.setWhenNeeded(true);
    therapy.setDosingFrequency(new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX));

    assertEquals(PrescriptionGroupEnum.ANTIMICROBIALS, overviewContentProvider.getPrescriptionGroup(therapy));
  }

  private MedicationManagement buildMedicationAction(final MedicationActionEnum actionEnum, final DateTime actionTime)
  {
    final MedicationManagement action = new MedicationManagement();
    final IsmTransition ismTransition = new IsmTransition();
    ismTransition.setCareflowStep(actionEnum.getCareflowStep());
    ismTransition.setCurrentState(actionEnum.getCurrentState());
    action.setIsmTransition(ismTransition);
    action.setTime(DataValueUtils.getDateTime(actionTime));
    return action;
  }

  private AdministrationTaskDto buildAdministrationTaskDto(
      final String taskId,
      final String administrationId,
      final String therapyId,
      final DateTime timestamp,
      final Double doseNumerator,
      final String doseNumeratorUnit)
  {
    final AdministrationTaskDto taskDto = new AdministrationTaskDto();
    taskDto.setTaskId(taskId);
    taskDto.setAdministrationId(administrationId);
    taskDto.setTherapyId(therapyId);
    taskDto.setPlannedAdministrationTime(timestamp);
    taskDto.setAdministrationTypeEnum(AdministrationTypeEnum.START);
    taskDto.setTherapyDoseDto(new TherapyDoseDto());
    taskDto.getTherapyDoseDto().setNumerator(doseNumerator);
    taskDto.getTherapyDoseDto().setNumeratorUnit(doseNumeratorUnit);
    taskDto.getTherapyDoseDto().setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    return taskDto;
  }
}
