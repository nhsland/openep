package com.marand.thinkmed.medications.automatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.marand.maf.core.server.util.ProxyUtils;
import com.marand.thinkmed.medications.administration.impl.AdministrationTaskConverterImpl;
import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.automatic.confirm.AdministrationAutoTaskConfirmer;
import com.marand.thinkmed.medications.automatic.confirm.AdministrationAutoTaskConfirmerHandler;
import com.marand.thinkmed.medications.charting.AutomaticChartingType;
import com.marand.thinkmed.medications.charting.NormalInfusionAutomaticChartingDto;
import com.marand.thinkmed.medications.charting.SelfAdminAutomaticChartingDto;
import com.marand.thinkmed.medications.charting.TherapyAutomaticChartingDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.event.MedsEventProducer;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;

/**
 * @author Nejc Korasa
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class AdministrationAutoTaskConfirmerTest
{
  @InjectMocks
  private AdministrationAutoTaskConfirmer administrationAutoTaskConfirmer;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Mock
  private AdministrationAutoTaskConfirmerHandler administrationAutoTaskConfirmerHandler;

  @Mock
  private MedsEventProducer medsEventProducer;

  @Mock
  private AdministrationTaskConverterImpl administrationTaskConverter;

  @Test
  public void testAutoConfirmTasks()
  {
    final List<TherapyAutomaticChartingDto> therapyAutomaticChartingDtos = new ArrayList<>();
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c1",
        "i1",
        "p1",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c2",
        "i2",
        "p1",
        new DateTime(2015, 11, 20, 9, 55),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c3",
        "i3",
        "p2",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c4",
        "i4",
        "p2",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c5",
        "i5",
        "p3",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c6",
        "i6",
        "p4",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c7",
        "i7",
        "p5",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c8",
        "i8",
        "p6",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c9",
        "i9",
        "p7",
        new DateTime(2015, 11, 20, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));
    therapyAutomaticChartingDtos.add(new SelfAdminAutomaticChartingDto(
        "c10",
        "i10",
        "p8",
        new DateTime(2015, 12, 14, 10, 0),
        SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED));

    therapyAutomaticChartingDtos.add(new NormalInfusionAutomaticChartingDto("c11", "i11", "p8"));
    therapyAutomaticChartingDtos.add(new NormalInfusionAutomaticChartingDto("c12", "i12", "p8"));

    Mockito
        .when(
            medicationsOpenEhrDao.getAutoChartingTherapyDtos(any(DateTime.class)))
        .thenReturn(therapyAutomaticChartingDtos);

    final List<TaskDto> taskDtos = new ArrayList<>();

    // now -> 2015, 11, 20, 22, 5

    //composition 1 2015, 11, 20, 10, 0
    taskDtos.add(createTestingTaskDto("c1", "i1", new DateTime(2015, 11, 20, 10, 5))); // Y
    taskDtos.add(createTestingTaskDto("c1", "i1", new DateTime(2015, 11, 20, 9, 55))); // n
    taskDtos.add(createTestingTaskDto("c1", "i1", new DateTime(2015, 11, 20, 9, 50))); // n
    taskDtos.add(createTestingTaskDto("c1", "i1", new DateTime(2015, 11, 20, 11, 5))); // Y
    taskDtos.add(createTestingTaskDto("c1", "i1", new DateTime(2015, 11, 20, 12, 5))); // Y

    //composition 2 2015, 11, 20, 9, 55
    taskDtos.add(createTestingTaskDto("c2", "i2", new DateTime(2015, 11, 20, 9, 55))); // Y
    taskDtos.add(createTestingTaskDto("c2", "i2", new DateTime(2015, 11, 20, 9, 50))); // n
    taskDtos.add(createTestingTaskDto("c2", "i2", new DateTime(2015, 11, 20, 8, 50))); // n
    taskDtos.add(createTestingTaskDto("c2", "i2", new DateTime(2015, 11, 20, 7, 5))); // n
    taskDtos.add(createTestingTaskDto("c2", "i2", new DateTime(2015, 11, 20, 12, 5))); // Y

    //composition 3 2015, 11, 20, 10, 0
    taskDtos.add(createTestingTaskDto("c3", "i3", new DateTime(2015, 11, 20, 12, 0))); // Y

    //composition 5 2015, 11, 20, 10, 0
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 11, 20, 12, 0))); // Y
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 11, 20, 12, 0))); // Y
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 11, 20, 12, 0))); // Y
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 11, 20, 12, 0))); // Y
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 11, 20, 12, 0))); // Y
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 12, 20, 12, 0))); // Y
    taskDtos.add(createTestingTaskDto("c5", "i5", new DateTime(2015, 12, 20, 7, 0))); // Y

    //composition 6 2015, 11, 20, 10, 0
    taskDtos.add(createTestingTaskDto("c7", "i5", new DateTime(2015, 11, 20, 8, 0))); // n
    taskDtos.add(createTestingTaskDto("c7", "i5", new DateTime(2015, 11, 20, 8, 0))); // n
    taskDtos.add(createTestingTaskDto("c7", "i5", new DateTime(2015, 11, 20, 8, 0))); // n
    taskDtos.add(createTestingTaskDto("c7", "i5", new DateTime(2015, 12, 20, 8, 0))); // n
    taskDtos.add(createTestingTaskDto("c7", "i5", new DateTime(2015, 12, 20, 7, 0))); // n

    //composition 11 2015, 11, 20, 10, 0
    taskDtos.add(createLinkedStopTask("c11", "i11", new DateTime(2015, 12, 20, 12, 0))); // Y
    taskDtos.add(createLinkedStopTask("c11", "i11", new DateTime(2015, 12, 20, 7, 0))); // Y

    //composition 12 2015, 11, 20, 10, 0
    taskDtos.add(createTestingTaskDto("c12", "i12", new DateTime(2015, 12, 20, 12, 0))); // n
    taskDtos.add(createTestingTaskDto("c12", "i12", new DateTime(2015, 12, 20, 7, 0))); // n

    Mockito
        .when(
            medicationsTasksProvider.findAdministrationTasks(
                anySet(),
                any(DateTime.class),
                any(DateTime.class)))
        .thenReturn(taskDtos);

    final InpatientPrescription inpatientPrescription = new InpatientPrescription();

    Mockito
        .when(
            medicationsOpenEhrDao.loadInpatientPrescription(
                anyString(),
                anyString()))
        .thenReturn(inpatientPrescription);

    administrationAutoTaskConfirmer.run();

    Mockito.verify(
        ProxyUtils.unwrapAdvisedProxy(administrationAutoTaskConfirmerHandler),
        Mockito.times(30)).autoConfirmAdministrationTask(
        any(AutomaticChartingType.class),
        anyString(),
        any(InpatientPrescription.class),
        any(TaskDto.class),
        any(DateTime.class));

    Mockito.verify(ProxyUtils.unwrapAdvisedProxy(medicationsOpenEhrDao), Mockito.times(5))
        .loadInpatientPrescription(anyString(), anyString());
  }

  private TaskDto createLinkedStopTask(final String compositionId, final String instructionName, final DateTime dueDate)
  {
    final TaskDto task = createTestingTaskDto(compositionId, instructionName, dueDate);
    task.getVariables()
        .put(AdministrationTaskDef.GROUP_UUID.getName(), "uuid");
    task.getVariables()
        .put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), AdministrationTypeEnum.STOP.name());
    return task;
  }

  private TaskDto createTestingTaskDto(final String compositionId, final String instructionName, final DateTime dueDate)
  {
    final TaskDto taskDto = new TaskDto();
    taskDto.setVariables(new HashMap<>());
    taskDto.getVariables()
        .put(AdministrationTaskDef.THERAPY_ID.getName(), TherapyIdUtils.createTherapyId(compositionId, instructionName));
    taskDto.setDueTime(dueDate);
    taskDto.getVariables()
        .put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), AdministrationTypeEnum.START.name());
    return taskDto;
  }
}
