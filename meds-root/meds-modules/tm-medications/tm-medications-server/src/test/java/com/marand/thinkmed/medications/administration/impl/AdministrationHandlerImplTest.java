package com.marand.thinkmed.medications.administration.impl;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.marand.thinkehr.util.ConversionUtils;
import com.marand.thinkmed.medications.administration.AdministrationSaver;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.administration.converter.AdministrationToEhrConverter;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.NotAdministeredReasonEnum;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationAdministration;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.therapy.converter.toehr.TherapyToEhrUtils;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.UserDto;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openehr.jaxb.rm.DvCodedText;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class AdministrationHandlerImplTest
{
  @InjectMocks
  private final AdministrationHandlerImpl administrationHandler = new AdministrationHandlerImpl();

  @InjectMocks
  private final AdministrationTaskConverter administrationTaskConverter = new AdministrationTaskConverterImpl();

  @InjectMocks
  private final AdministrationToEhrConverter administrationToEhrConverter = new AdministrationToEhrConverter();

  @InjectMocks
  private final TherapyToEhrUtils therapyToEhrUtils = new TherapyToEhrUtils();

  @InjectMocks
  private final AdministrationUtilsImpl administrationUtils = new AdministrationUtilsImpl();

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Mock
  private AdministrationSaver administrationSaver;

  @Mock
  private MedicationsTasksHandler medicationsTasksHandler;

  @Mock
  private ProcessService processService;

  @Mock
  private OverviewContentProvider overviewContentProvider;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private RequestDateTimeHolder requestDateTimeHolder;

  @Before
  public void setup()
  {
    administrationHandler.setAdministrationTaskConverter(administrationTaskConverter);
    administrationHandler.setAdministrationToEhrConverter(administrationToEhrConverter);
    administrationToEhrConverter.setTherapyToEhrUtils(therapyToEhrUtils);
    administrationToEhrConverter.setAdministrationUtils(administrationUtils);
    RequestUser.init(auth -> new UserDto("Test", null, "Test", Collections.emptyList()));
  }

  @Test
  public void testHandleGroupTasksOnAdministrationConfirm1()
  {
    // normal infusion
    // start and stop tasks in the past and unconfirmed
    // user confirms start task as GIVEN, stop task should also confirm as GIVEN

    final DateTime startAdministrationTime = new DateTime(2018, 11, 13, 9, 0);
    final DateTime stopDueTime = new DateTime(2018, 11, 13, 11, 0);
    mockMedicationsTasksProviderOneStopTask(stopDueTime, startAdministrationTime, null, false);

    final StartAdministrationDto administration = buildStartAdministrationDto(
        startAdministrationTime,
        AdministrationResultEnum.GIVEN);
    final InpatientPrescription inpatientPrescription = buildInpatientPrescription();
    final ConstantComplexTherapyDto therapy = buildConstantComplexTherapyDto();

    administrationHandler.handleGroupTasksOnAdministrationConfirm(
        "patient1",
        "user1",
        administration,
        inpatientPrescription,
        therapy,
        new DateTime(2018, 11, 13, 12, 0));

    Mockito.verify(processService, times(1)).completeTasks("task1");

    final ArgumentCaptor<MedicationAdministration> administrationCaptor = ArgumentCaptor.forClass(MedicationAdministration.class);
    Mockito.verify(administrationSaver, times(1)).save(eq("patient1"), administrationCaptor.capture(), eq(null));
    assertEquals(
        ConversionUtils.toDvDateTime(stopDueTime).getValue(),
        administrationCaptor.getValue().getMedicationManagement().getTime().getValue());
    assertEquals(
        MedicationActionEnum.ADMINISTER,
        MedicationActionEnum.getActionEnum(administrationCaptor.getValue().getMedicationManagement()));
  }

  @Test
  public void testHandleGroupTasksOnAdministrationConfirm2()
  {
    // normal infusion
    // start is in the past and unconfirmed, stop task is in the future and unconfirmed
    // user confirms start task as GIVEN, stop task should not be confirmed

    final DateTime startAdministrationTime = new DateTime(2018, 11, 13, 9, 0);
    final DateTime stopDueTime = new DateTime(2018, 11, 13, 11, 0);
    mockMedicationsTasksProviderOneStopTask(stopDueTime, startAdministrationTime, null, false);

    final StartAdministrationDto administration = buildStartAdministrationDto(
        startAdministrationTime,
        AdministrationResultEnum.GIVEN);
    final InpatientPrescription inpatientPrescription = buildInpatientPrescription();
    final ConstantComplexTherapyDto therapy = buildConstantComplexTherapyDto();

    administrationHandler.handleGroupTasksOnAdministrationConfirm(
        "patient1",
        "user1",
        administration,
        inpatientPrescription,
        therapy,
        new DateTime(2018, 11, 13, 10, 0));

    Mockito.verify(processService, never()).completeTasks(anyString());
    Mockito.verify(administrationSaver, never()).save(anyString(), anyObject(), anyObject());
  }

  @Test
  public void testHandleGroupTasksOnAdministrationConfirm3()
  {
    // normal infusion
    // start and stop tasks in the past and unconfirmed
    // user confirms start task as NOT_GIVEN, stop task should also confirm as NOT_GIVEN

    final DateTime startAdministrationTime = new DateTime(2018, 11, 13, 9, 0);
    final DateTime stopDueTime = new DateTime(2018, 11, 13, 11, 0);
    mockMedicationsTasksProviderOneStopTask(stopDueTime, startAdministrationTime, null, false);

    final StartAdministrationDto administration = buildStartAdministrationDto(
        startAdministrationTime,
        AdministrationResultEnum.NOT_GIVEN);
    final InpatientPrescription inpatientPrescription = buildInpatientPrescription();
    final ConstantComplexTherapyDto therapy = buildConstantComplexTherapyDto();

    administrationHandler.handleGroupTasksOnAdministrationConfirm(
        "patient1",
        "user1",
        administration,
        inpatientPrescription,
        therapy,
        new DateTime(2018, 11, 13, 12, 0));

    Mockito.verify(processService, times(1)).completeTasks("task1");

    final ArgumentCaptor<MedicationAdministration> administrationCaptor = ArgumentCaptor.forClass(MedicationAdministration.class);
    Mockito.verify(administrationSaver, times(1)).save(eq("patient1"), administrationCaptor.capture(), eq(null));
    assertEquals(
        ConversionUtils.toDvDateTime(stopDueTime).getValue(),
        administrationCaptor.getValue().getMedicationManagement().getTime().getValue());
    assertEquals(
        MedicationActionEnum.WITHHOLD,
        MedicationActionEnum.getActionEnum(administrationCaptor.getValue().getMedicationManagement()));
  }

  @Test
  public void testHandleGroupTasksOnAdministrationConfirm4()
  {
    // normal infusion
    // start is in the past and unconfirmed, stop task is in the future and unconfirmed
    // user confirms start task as NOT_GIVEN, stop task should also confirm as NOT_GIVEN

    final DateTime startAdministrationTime = new DateTime(2018, 11, 13, 9, 0);
    final DateTime stopDueTime = new DateTime(2018, 11, 13, 11, 0);
    mockMedicationsTasksProviderOneStopTask(stopDueTime, startAdministrationTime, null, false);

    final StartAdministrationDto administration = buildStartAdministrationDto(
        startAdministrationTime,
        AdministrationResultEnum.NOT_GIVEN);
    final InpatientPrescription inpatientPrescription = buildInpatientPrescription();
    final ConstantComplexTherapyDto therapy = buildConstantComplexTherapyDto();

    administrationHandler.handleGroupTasksOnAdministrationConfirm(
        "patient1",
        "user1",
        administration,
        inpatientPrescription,
        therapy,
        new DateTime(2018, 11, 13, 10, 0));

    Mockito.verify(processService, times(1)).completeTasks("task1");

    final ArgumentCaptor<MedicationAdministration> administrationCaptor = ArgumentCaptor.forClass(MedicationAdministration.class);
    Mockito.verify(administrationSaver, times(1)).save(eq("patient1"), administrationCaptor.capture(), eq(null));
    assertEquals(
        ConversionUtils.toDvDateTime(stopDueTime).getValue(),
        administrationCaptor.getValue().getMedicationManagement().getTime().getValue());
    assertEquals(
        MedicationActionEnum.WITHHOLD,
        MedicationActionEnum.getActionEnum(administrationCaptor.getValue().getMedicationManagement()));
  }

  @Test
  public void testHandleGroupTasksOnAdministrationConfirm5()
  {
    // normal infusion
    // start and stop tasks in the past and confirmed as GIVEN
    // user edits start task and changes it to NOT GIVEN, stop task should also be changed to NOT_GIVEN

    final DateTime startAdministrationTime = new DateTime(2018, 11, 13, 9, 0);
    final DateTime stopDueTime = new DateTime(2018, 11, 13, 11, 0);
    mockMedicationsTasksProviderOneStopTask(stopDueTime, startAdministrationTime, "administrationUid::1", true);
    mockLoadAdministration(stopDueTime, MedicationActionEnum.ADMINISTER);

    final StartAdministrationDto administration = buildStartAdministrationDto(
        startAdministrationTime,
        AdministrationResultEnum.NOT_GIVEN);
    final InpatientPrescription inpatientPrescription = buildInpatientPrescription();
    final ConstantComplexTherapyDto therapy = buildConstantComplexTherapyDto();

    administrationHandler.handleGroupTasksOnAdministrationConfirm(
        "patient1",
        "user1",
        administration,
        inpatientPrescription,
        therapy,
        new DateTime(2018, 11, 13, 12, 0));

    Mockito.verify(processService, never()).completeTasks(anyString());

    final ArgumentCaptor<MedicationAdministration> administrationCaptor = ArgumentCaptor.forClass(MedicationAdministration.class);
    Mockito.verify(medicationsOpenEhrDao, times(1))
        .saveComposition(eq("patient1"), administrationCaptor.capture(), eq("administrationUid"));
    assertEquals(
        ConversionUtils.toDvDateTime(stopDueTime).getValue(),
        administrationCaptor.getValue().getMedicationManagement().getTime().getValue());
    assertEquals(
        MedicationActionEnum.WITHHOLD,
        MedicationActionEnum.getActionEnum(administrationCaptor.getValue().getMedicationManagement()));
  }

  private void mockLoadAdministration(final DateTime time, final MedicationActionEnum action)
  {
    final MedicationAdministration ehrAdministration = new MedicationAdministration();
    final MedicationManagement medicationManagement = new MedicationManagement();
    medicationManagement.setIsmTransition(action.buildIsmTransition());
    medicationManagement.setTime(ConversionUtils.toDvDateTime(time));
    ehrAdministration.setMedicationManagement(medicationManagement);

    ehrAdministration.setUid("administrationUid");
    Mockito.when(medicationsOpenEhrDao.loadMedicationAdministration("patient1", "administrationUid"))
        .thenReturn(ehrAdministration);
  }

  @Test
  public void testHandleGroupTasksOnAdministrationConfirm6()
  {
    // normal infusion
    // start is in the past and confirmed as GIVEN, stop task is in the future and unconfirmed
    // user edits start task and changes it to NOT GIVEN, stop task should be confirmed as NOT_GIVEN

    final DateTime startAdministrationTime = new DateTime(2018, 11, 13, 9, 0);
    final DateTime stopDueTime = new DateTime(2018, 11, 13, 11, 0);
    mockMedicationsTasksProviderOneStopTask(stopDueTime, startAdministrationTime, "administrationUid::1", true);
    mockLoadAdministration(stopDueTime, MedicationActionEnum.ADMINISTER);

    final StartAdministrationDto administration = buildStartAdministrationDto(
        new DateTime(2018, 11, 13, 9, 0),
        AdministrationResultEnum.NOT_GIVEN);

    final InpatientPrescription inpatientPrescription = buildInpatientPrescription();

    final ConstantComplexTherapyDto therapy = buildConstantComplexTherapyDto();

    administrationHandler.handleGroupTasksOnAdministrationConfirm(
        "patient1",
        "user1",
        administration,
        inpatientPrescription,
        therapy,
        new DateTime(2018, 11, 13, 10, 0));

    Mockito.verify(processService, never()).completeTasks(anyString());

    final ArgumentCaptor<MedicationAdministration> administrationCaptor = ArgumentCaptor.forClass(MedicationAdministration.class);
    Mockito.verify(medicationsOpenEhrDao, times(1))
        .saveComposition(eq("patient1"), administrationCaptor.capture(), eq("administrationUid"));
    assertEquals(
        ConversionUtils.toDvDateTime(stopDueTime).getValue(),
        administrationCaptor.getValue().getMedicationManagement().getTime().getValue());
    assertEquals(
        MedicationActionEnum.WITHHOLD,
        MedicationActionEnum.getActionEnum(administrationCaptor.getValue().getMedicationManagement()));
  }

  @Test
  public void testHandleGroupTasksOnAdministrationConfirm7()
  {
    // normal infusion
    // start and stop tasks in the past and confirmed as NOT_GIVEN
    // user edits start task and changes it to GIVEN, stop task should also be changed to GIVEN
    final DateTime startAdministrationTime = new DateTime(2018, 11, 13, 9, 0);
    final DateTime stopDueTime = new DateTime(2018, 11, 13, 11, 0);
    mockMedicationsTasksProviderOneStopTask(stopDueTime, startAdministrationTime, "administrationUid::1", true);
    mockLoadAdministration(stopDueTime, MedicationActionEnum.ADMINISTER);

    final StartAdministrationDto administration = buildStartAdministrationDto(
        new DateTime(2018, 11, 13, 9, 0),
        AdministrationResultEnum.GIVEN);

    final InpatientPrescription inpatientPrescription = buildInpatientPrescription();

    final ConstantComplexTherapyDto therapy = buildConstantComplexTherapyDto();

    administrationHandler.handleGroupTasksOnAdministrationConfirm(
        "patient1",
        "user1",
        administration,
        inpatientPrescription,
        therapy,
        new DateTime(2018, 11, 13, 12, 0));

    Mockito.verify(processService, never()).completeTasks(anyString());

    final ArgumentCaptor<MedicationAdministration> administrationCaptor = ArgumentCaptor.forClass(MedicationAdministration.class);
    Mockito.verify(medicationsOpenEhrDao, times(1))
        .saveComposition(eq("patient1"), administrationCaptor.capture(), eq("administrationUid"));
    assertEquals(
        ConversionUtils.toDvDateTime(stopDueTime).getValue(),
        administrationCaptor.getValue().getMedicationManagement().getTime().getValue());
    assertEquals(
        MedicationActionEnum.ADMINISTER,
        MedicationActionEnum.getActionEnum(administrationCaptor.getValue().getMedicationManagement()));
  }

  @Test
  public void testHandleGroupTasksOnAdministrationConfirm8()
  {
    // normal infusion
    // start is in the past and confirmed as GIVEN, stop task is in the future confirmed as NOT_GIVEN
    // user edits start task and changes it to GIVEN, stop task should become unconfirmed
    final DateTime startAdministrationTime = new DateTime(2018, 11, 13, 9, 0);
    final DateTime stopDueTime = new DateTime(2018, 11, 13, 11, 0);
    mockMedicationsTasksProviderOneStopTask(stopDueTime, startAdministrationTime, "administrationUid::1", true);
    mockLoadAdministration(stopDueTime, MedicationActionEnum.ADMINISTER);

    final StartAdministrationDto administration = buildStartAdministrationDto(
        new DateTime(2018, 11, 13, 9, 0),
        AdministrationResultEnum.GIVEN);

    final InpatientPrescription inpatientPrescription = buildInpatientPrescription();

    final ConstantComplexTherapyDto therapy = buildConstantComplexTherapyDto();

    administrationHandler.handleGroupTasksOnAdministrationConfirm(
        "patient1",
        "user1",
        administration,
        inpatientPrescription,
        therapy,
        new DateTime(2018, 11, 13, 10, 0));

    Mockito.verify(processService, never()).completeTasks(anyString());
    Mockito.verify(medicationsTasksHandler, times(1)).undoCompleteTask("task1");

    Mockito.verify(medicationsOpenEhrDao, times(1))
        .deleteTherapyAdministration(eq("patient1"), eq("administrationUid"), eq(null));
  }

  private ConstantComplexTherapyDto buildConstantComplexTherapyDto()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setDoseType(TherapyDoseTypeEnum.RATE_QUANTITY);
    therapy.setCompositionUid("111");
    therapy.setEhrOrderName("Medication order");
    return therapy;
  }

  private InpatientPrescription buildInpatientPrescription()
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.setMedicationOrder(new MedicationOrder());
    return inpatientPrescription;
  }

  private StartAdministrationDto buildStartAdministrationDto(
      final DateTime administrationTime,
      final AdministrationResultEnum result)
  {
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setAdministrationTime(administrationTime);
    administration.setAdministrationResult(result);
    administration.setGroupUUId("groupUUId1");
    return administration;
  }

  private void mockMedicationsTasksProviderOneStopTask(
      final DateTime dueTime,
      final DateTime when,
      final String administrationUid,
      final boolean completed)
  {
    final TaskDto stopTask = new TaskDto();
    stopTask.setId("task1");
    stopTask.setDueTime(dueTime);
    stopTask.setCompleted(completed);
    stopTask.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "STOP");
    stopTask.getVariables().put(AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName(), administrationUid);

    Mockito.reset(medicationsTasksProvider);

    Mockito.when(medicationsTasksProvider.findAdministrationTasks(
        "patient1",
        Collections.singletonList("111|Medication order"),
        when,
        null,
        "groupUUId1",
        true))
        .thenReturn(Lists.newArrayList(stopTask));
  }

  @Test
  public void testCancelAdministrationTask()
  {
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setTherapyId("presc-uid1|Medication order");
    administration.setTaskId("t1");
    administration.setPlannedTime(new DateTime(2019, 5, 11, 12, 0));

    final InpatientPrescription prescription = new InpatientPrescription();
    prescription.setMedicationOrder(new MedicationOrder());

    Mockito
        .when(medicationsOpenEhrDao.loadInpatientPrescription("1", "presc-uid1"))
        .thenReturn(prescription);

    Mockito
        .when(requestDateTimeHolder.getRequestTimestamp())
        .thenReturn(new DateTime(2019, 5, 11, 10, 0));

    administrationHandler.cancelAdministrationTask("1", administration, NotAdministeredReasonEnum.CANCELLED, null);

    final ArgumentCaptor<String> patientIdCapture = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<MedicationAdministration> administrationCapture = ArgumentCaptor.forClass(MedicationAdministration.class);
    final ArgumentCaptor<String> administrationId = ArgumentCaptor.forClass(String.class);

    Mockito.verify(administrationSaver, times(1))
        .save(patientIdCapture.capture(), administrationCapture.capture(), administrationId.capture());

    assertEquals("1", patientIdCapture.getValue());
    assertNull(administrationId.getValue());

    final MedicationAdministration callAdministration = administrationCapture.getValue();
    assertEquals(
        "CANCELLED",
        ((DvCodedText)callAdministration.getMedicationManagement().getReason().get(0)).getDefiningCode().getCodeString());
    assertTrue(new DateTime(2019, 5, 11, 12, 0).isEqual(
        ConversionUtils.toDateTime(callAdministration.getMedicationManagement().getScheduledDateTime())));
    assertEquals(
        MedicationActionEnum.WITHHOLD,
        MedicationActionEnum.getActionEnum(callAdministration.getMedicationManagement()));

    Mockito.verify(processService, times(1)).completeTasks("t1");
  }

  @Test
  public void testCancelAdministrationTaskWithGroup()
  {
    final StartAdministrationDto administration = new StartAdministrationDto();
    final String therapyId = "presc-uid1|Medication order";
    administration.setTherapyId(therapyId);
    administration.setPlannedTime(new DateTime(2019, 5, 11, 12, 0));
    administration.setGroupUUId("group-uid");

    final InpatientPrescription prescription = new InpatientPrescription();
    prescription.setMedicationOrder(new MedicationOrder());

    Mockito
        .when(medicationsOpenEhrDao.loadInpatientPrescription("1", "presc-uid1"))
        .thenReturn(prescription);

    Mockito
        .when(requestDateTimeHolder.getRequestTimestamp())
        .thenReturn(new DateTime(2019, 5, 11, 10, 0));

    final TaskDto task1 = new TaskDto();
    task1.setId("t1");
    task1.setDueTime(new DateTime(2019, 5, 11, 12, 0));
    task1.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "START");
    task1.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), therapyId);

    final TaskDto task2 = new TaskDto();
    task2.setId("t2");
    task2.setDueTime(new DateTime(2019, 5, 11, 18, 0));
    task2.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "START");
    task2.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), therapyId);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks("1", null, null, null, "group-uid", false))
        .thenReturn(Lists.newArrayList(task1, task2));

    administrationHandler.cancelAdministrationTask("1", administration, NotAdministeredReasonEnum.CANCELLED, "c1");

    final ArgumentCaptor<String> patientIdCapture = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<MedicationAdministration> administrationCapture = ArgumentCaptor.forClass(MedicationAdministration.class);
    final ArgumentCaptor<String> administrationId = ArgumentCaptor.forClass(String.class);

    Mockito.verify(administrationSaver, times(2))
        .save(patientIdCapture.capture(), administrationCapture.capture(), administrationId.capture());

    final List<MedicationAdministration> callAdministration = administrationCapture.getAllValues();

    assertEquals(
        "CANCELLED",
        ((DvCodedText)callAdministration.get(0).getMedicationManagement().getReason().get(0)).getDefiningCode()
            .getCodeString());
    assertTrue(new DateTime(2019, 5, 11, 12, 0).isEqual(
        ConversionUtils.toDateTime(callAdministration.get(0).getMedicationManagement().getScheduledDateTime())));
    assertEquals(
        MedicationActionEnum.WITHHOLD,
        MedicationActionEnum.getActionEnum(callAdministration.get(0).getMedicationManagement()));
    assertEquals("c1", callAdministration.get(0).getMedicationManagement().getComment().getValue());

    assertEquals(
        "CANCELLED",
        ((DvCodedText)callAdministration.get(1).getMedicationManagement().getReason().get(0)).getDefiningCode()
            .getCodeString());
    assertTrue(new DateTime(2019, 5, 11, 18, 0).isEqual(
        ConversionUtils.toDateTime(callAdministration.get(1).getMedicationManagement().getScheduledDateTime())));
    assertEquals(
        MedicationActionEnum.WITHHOLD,
        MedicationActionEnum.getActionEnum(callAdministration.get(1).getMedicationManagement()));

    assertEquals("c1", callAdministration.get(1).getMedicationManagement().getComment().getValue());

    Mockito.verify(processService, times(1)).completeTasks("t1");
    Mockito.verify(processService, times(1)).completeTasks("t2");
  }

  @Test
  public void testUnCancelAdministrationTask()
  {
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setAdministrationId("a1");
    administration.setTaskId("t1");

    administrationHandler.uncancelAdministrationTask("1", administration);

    Mockito.verify(medicationsOpenEhrDao, times(1)).deleteTherapyAdministration("1", "a1", null);
    Mockito.verify(medicationsTasksHandler, times(1)).undoCompleteTask(administration.getTaskId());
  }

  @Test
  public void testUnCancelAdministrationTaskGroup()
  {
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setAdministrationId("a1");
    final String therapyId = "presc-uid1|Medication order";
    administration.setTherapyId(therapyId);
    administration.setTaskId("t1");
    administration.setGroupUUId("group-uid");

    final TaskDto task1 = new TaskDto();
    task1.setId("t1");
    task1.setCompleted(true);
    task1.setDueTime(new DateTime(2019, 5, 11, 12, 0));
    task1.getVariables().put(AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName(), "a1");
    task1.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "START");
    task1.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), therapyId);

    final TaskDto task2 = new TaskDto();
    task2.setId("t2");
    task2.setCompleted(true);
    task2.setDueTime(new DateTime(2019, 5, 11, 18, 0));
    task2.getVariables().put(AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName(), "a2");
    task2.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "STOP");
    task2.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), therapyId);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks("1", null, null, null, "group-uid", true))
        .thenReturn(Lists.newArrayList(task1, task2));

    administrationHandler.uncancelAdministrationTask("1", administration);

    Mockito.verify(medicationsOpenEhrDao, times(1)).deleteTherapyAdministration("1", "a1", null);
    Mockito.verify(medicationsTasksHandler, times(1)).undoCompleteTask("t1");

    Mockito.verify(medicationsOpenEhrDao, times(1)).deleteTherapyAdministration("1", "a2", null);
    Mockito.verify(medicationsTasksHandler, times(1)).undoCompleteTask("t2");
  }

  @Test
  public void testUnCancelAdministrationTaskGroupFirstConfirmed()
  {
    final AdjustInfusionAdministrationDto administration = new AdjustInfusionAdministrationDto();
    administration.setAdministrationId("a2");
    final String therapyId = "presc-uid1|Medication order";
    administration.setTherapyId(therapyId);
    administration.setTaskId("t2");
    administration.setGroupUUId("group-uid");

    final TaskDto task1 = new TaskDto();
    task1.setId("t1");
    task1.setCompleted(true);
    task1.setDueTime(new DateTime(2019, 5, 11, 12, 0));
    task1.getVariables().put(AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName(), "a1");
    task1.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "START");
    task1.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), therapyId);

    final TaskDto task2 = new TaskDto();
    task2.setId("t2");
    task2.setCompleted(false);
    task2.setDueTime(new DateTime(2019, 5, 11, 14, 0));
    task2.getVariables().put(AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName(), "a2");
    task2.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "ADJUST_INFUSION");
    task2.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), therapyId);

    final TaskDto task3 = new TaskDto();
    task3.setId("t3");
    task3.setCompleted(false);
    task3.setDueTime(new DateTime(2019, 5, 11, 18, 0));
    task3.getVariables().put(AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName(), "a3");
    task3.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "STOP");
    task3.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), therapyId);

    administrationHandler.uncancelAdministrationTask("1", administration);

    Mockito.verify(medicationsOpenEhrDao, times(1)).deleteTherapyAdministration("1", "a2", null);
    Mockito.verify(medicationsTasksHandler, times(1)).undoCompleteTask("t2");
  }
}
