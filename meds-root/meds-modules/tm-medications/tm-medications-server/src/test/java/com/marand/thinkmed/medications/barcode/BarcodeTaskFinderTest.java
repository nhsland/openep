package com.marand.thinkmed.medications.barcode;

import java.util.Collections;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.business.MedicationsFinder;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto.BarcodeSearchResult;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.Medication;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class BarcodeTaskFinderTest
{
  @InjectMocks
  private final BarcodeTaskFinder barcodeTaskFinder = new BarcodeTaskFinder();

  @Mock
  private MedicationsDao medicationsDao;

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Mock
  private TherapyEhrHandler therapyEhrHandler;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsFinder medicationsFinder;

  private final DateTime testTime = new DateTime(2017, 8, 23, 10, 0);

  @Test
  public void testGetAdministrationTaskForBarcodeNoMedication()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("222"))
        .thenReturn(null);

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "222", testTime);
    assertEquals(BarcodeSearchResult.NO_MEDICATION, dto.getBarcodeSearchResult());
    assertNull(dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeNoDueTasks()
  {
    Mockito.reset(medicationsDao);
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("222"))
        .thenReturn(222L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Collections.emptyList());

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "222", testTime);
    assertEquals(BarcodeSearchResult.NO_TASK, dto.getBarcodeSearchResult());
    assertNull(dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeNoMatchingTherapy()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("222"))
        .thenReturn(222L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(buildTaskDto("333", "uid1|Medication order")));

    Mockito
        .when(medicationsOpenEhrDao.loadInpatientPrescriptions(Sets.newHashSet("uid1")))
        .thenReturn(Lists.newArrayList(buildTestInpatientPrescription("uid1::1", "1")));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "222", testTime);
    assertEquals(BarcodeSearchResult.NO_TASK, dto.getBarcodeSearchResult());
    assertNull(dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeDirectMedicationMatch()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("999"))
        .thenReturn(999L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(buildTaskDto("333", "uid1|Medication order")));

    final InpatientPrescription inpatientPrescription = buildTestInpatientPrescription("uid1::1", "999");

    Mockito
        .when(medicationsOpenEhrDao.loadInpatientPrescriptions(Sets.newHashSet("uid1")))
        .thenReturn(Lists.newArrayList(inpatientPrescription));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "999", testTime);
    assertEquals(BarcodeSearchResult.TASK_FOUND, dto.getBarcodeSearchResult());
    assertEquals("333", dto.getTaskId());
    assertEquals((Long)999L, dto.getMedicationId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeSimilarMedicationMatch()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("999"))
        .thenReturn(999L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(buildTaskDto("333", "uid1|Medication order")));

    final InpatientPrescription inpatientPrescription = buildTestInpatientPrescription("uid1::1", "888");

    Mockito
        .when(medicationsOpenEhrDao.loadInpatientPrescriptions(Sets.newHashSet("uid1")))
        .thenReturn(Lists.newArrayList(inpatientPrescription));

    Mockito
        .when(medicationsFinder.findMedicationProducts(888L, Lists.newArrayList(444L), null, testTime))
        .thenReturn(Lists.newArrayList(buildMedicationDto(888L), buildMedicationDto(999L)));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "999", testTime);
    assertEquals(BarcodeSearchResult.TASK_FOUND, dto.getBarcodeSearchResult());
    assertEquals("333", dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeNoSimilarMedicationMatch()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("999"))
        .thenReturn(999L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(buildTaskDto("333", "uid1|Medication order")));

    final InpatientPrescription inpatientPrescription = buildTestInpatientPrescription("uid1::1", "888");
    Mockito
        .when(medicationsOpenEhrDao.loadInpatientPrescriptions(Sets.newHashSet("uid1")))
        .thenReturn(Lists.newArrayList(inpatientPrescription));

    Mockito
        .when(medicationsFinder.findMedicationProducts(888L, Lists.newArrayList(444L), null, testTime))
        .thenReturn(Lists.newArrayList(buildMedicationDto(888L), buildMedicationDto(777L)));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "999", testTime);
    assertEquals(BarcodeSearchResult.NO_TASK, dto.getBarcodeSearchResult());
    assertNull(dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeMultipleTasksSingleTherapy()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("999"))
        .thenReturn(999L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(
            buildTaskDto("333", "uid1|Medication order"),
            buildTaskDto("334", "uid1|Medication order")));

    final InpatientPrescription inpatientPrescription = buildTestInpatientPrescription("uid1::1", "888");

    Mockito
        .when(medicationsOpenEhrDao.loadInpatientPrescriptions(Sets.newHashSet("uid1")))
        .thenReturn(Lists.newArrayList(inpatientPrescription));

    Mockito
        .when(medicationsFinder.findMedicationProducts(888L, Lists.newArrayList(444L), null, testTime))
        .thenReturn(Lists.newArrayList(buildMedicationDto(888L), buildMedicationDto(999L)));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "999", testTime);
    assertEquals(BarcodeSearchResult.MULTIPLE_TASKS, dto.getBarcodeSearchResult());
    assertNull(dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeMultipleTasksMultipleTherapies()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode("999"))
        .thenReturn(999L);

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(
            buildTaskDto("333", "uid1|Medication order"),
            buildTaskDto("334", "uid2|Medication order")));

    final InpatientPrescription inpatientPrescription1 = buildTestInpatientPrescription("uid1::1", "999");
    final InpatientPrescription inpatientPrescription2 = buildTestInpatientPrescription("uid2::1", "888");

    Mockito
        .when(medicationsOpenEhrDao.loadInpatientPrescriptions(Sets.newHashSet("uid1", "uid2")))
        .thenReturn(Lists.newArrayList(inpatientPrescription1, inpatientPrescription2));

    Mockito
        .when(medicationsFinder.findMedicationProducts(888L, Lists.newArrayList(444L), null, testTime))
        .thenReturn(Lists.newArrayList(buildMedicationDto(888L), buildMedicationDto(999L)));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode("1", "999", testTime);
    assertEquals(BarcodeSearchResult.MULTIPLE_TASKS, dto.getBarcodeSearchResult());
    assertNull(dto.getTaskId());
  }

  @Test
  public void testGetAdministrationTaskForBarcodeByTherapyId()
  {
    Mockito
        .when(medicationsDao.getMedicationIdForBarcode(
            "8325471586523539866160971934389845020457248789196712414436040558608958584240906267148183311097017355769778070776194094002890098"))
        .thenReturn(null);

    Mockito
        .when(therapyEhrHandler.getOriginalTherapyId(
            "1",
            "123e4567-e89b-12d3-a456-426655440000"))
        .thenReturn("123e4567-e89b-12d3-a456-426655440001|Medication order");

    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            Collections.singleton("1"),
            testTime.minusHours(1),
            testTime.plusHours(1)))
        .thenReturn(Lists.newArrayList(buildTaskDto("333", "123e4567-e89b-12d3-a456-426655440000|Medication order")));

    final BarcodeTaskSearchDto dto = barcodeTaskFinder.getAdministrationTaskForBarcode(
        "1",
        "8325471586523539866160971934389845020457248789196712414436040558608958584240906267148183311097017355769778070776194094002890098",
        testTime);
    assertEquals(BarcodeSearchResult.TASK_FOUND, dto.getBarcodeSearchResult());
    assertEquals("333", dto.getTaskId());
    assertNull(dto.getMedicationId());
  }

  private InpatientPrescription buildTestInpatientPrescription(final String compositionUid, final String medicationId)
  {
    final InpatientPrescription inpatientPrescription = new InpatientPrescription();
    inpatientPrescription.setUid(compositionUid);
    final MedicationOrder medicationOrder = new MedicationOrder();
    medicationOrder.setName(DataValueUtils.getText("Medication order"));
    medicationOrder.getRoute().add(DataValueUtils.getLocalCodedText("444", "Oral"));

    final Medication preparationDetails = new Medication();
    preparationDetails.setComponentName(DataValueUtils.getLocalCodedText(medicationId, medicationId));
    medicationOrder.setPreparationDetails(preparationDetails);

    inpatientPrescription.setMedicationOrder(medicationOrder);
    return inpatientPrescription;
  }

  private MedicationDto buildMedicationDto(final Long medicationId)
  {
    final MedicationDto medicationDto = new MedicationDto();
    medicationDto.setId(medicationId);
    return medicationDto;
  }

  private TaskDto buildTaskDto(final String taskId, final String therapyId)
  {
    final TaskDto taskDto = new TaskDto();
    taskDto.setId(taskId);
    taskDto.getVariables().put(AdministrationTaskDef.THERAPY_ID.getName(), therapyId);
    taskDto.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), AdministrationTypeEnum.START.name());
    return taskDto;
  }
}
