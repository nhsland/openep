package com.marand.thinkmed.medications.task.impl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.marand.maf.core.PartialList;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.TherapyTaskDef;
import com.marand.thinkmed.patient.PatientDataProvider;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
public class MedicationsTasksProviderTest
{
  @InjectMocks
  private final MedicationsTasksProvider medicationsTasksProvider = new MedicationsTasksProviderImpl();

  @Mock
  private AdministrationTaskConverter administrationTaskConverter;

  @Mock
  private PatientDataProvider patientDataProvider;

  @Mock
  private ProcessService processService;

  @Test
  public void testFindSimpleTasksForTherapies()
  {
    Mockito.reset(processService);

    final TaskDto task1 = new TaskDto();
    task1.setId("task1");
    task1.setDueTime(new DateTime(2015, 9, 14, 0, 0));
    task1.setTaskExecutionStrategyId(TaskTypeEnum.DOCTOR_REVIEW.getName());
    task1.setVariables(new HashMap<>());
    task1.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");

    final TaskDto task2 = new TaskDto();
    task2.setId("task2");
    task2.setDueTime(new DateTime(2015, 9, 15, 0, 0));
    task2.setTaskExecutionStrategyId(TaskTypeEnum.DOCTOR_REVIEW.getName());
    task2.setVariables(new HashMap<>());
    task2.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");

    final TaskDto task3 = new TaskDto();
    task3.setId("task3");
    task3.setDueTime(new DateTime(2015, 9, 13, 0, 0));
    task3.setTaskExecutionStrategyId(TaskTypeEnum.SWITCH_TO_ORAL.getName());
    task3.setVariables(new HashMap<>());
    task3.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy2");

    final TaskDto task4 = new TaskDto();
    task4.setId("task4");
    task4.setDueTime(new DateTime(2015, 9, 15, 0, 0));
    task4.setTaskExecutionStrategyId(TaskTypeEnum.SWITCH_TO_ORAL.getName());
    task4.setVariables(new HashMap<>());
    task4.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy2");

    final TaskDto task5 = new TaskDto();
    task5.setId("task5");
    task5.setDueTime(new DateTime(2015, 9, 14, 0, 0));
    task5.setTaskExecutionStrategyId(TaskTypeEnum.SUPPLY_REMINDER.getName());
    task5.setVariables(new HashMap<>());
    task5.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");

    final TaskDto task6 = new TaskDto();
    task6.setId("task6");
    task6.setDueTime(new DateTime(2015, 9, 15, 0, 0));
    task6.setTaskExecutionStrategyId(TaskTypeEnum.SUPPLY_REMINDER.getName());
    task6.setVariables(new HashMap<>());
    task6.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy2");

    final TaskDto task7 = new TaskDto();
    task7.setId("task7");
    task7.setDueTime(new DateTime(2015, 9, 14, 0, 0));
    task7.setTaskExecutionStrategyId(TaskTypeEnum.SUPPLY_REVIEW.getName());
    task7.setVariables(new HashMap<>());
    task7.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");

    final TaskDto task8 = new TaskDto();
    task8.setId("task8");
    task8.setDueTime(new DateTime(2015, 9, 20, 0, 0));
    task8.setTaskExecutionStrategyId(TaskTypeEnum.SUPPLY_REVIEW.getName());
    task8.setVariables(new HashMap<>());
    task8.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy3");

    final TaskDto task9 = new TaskDto();
    task9.setId("task9");
    task9.setDueTime(new DateTime(2015, 9, 21, 0, 0));
    task9.setTaskExecutionStrategyId(TaskTypeEnum.PERFUSION_SYRINGE_START.getName());
    task9.setVariables(new HashMap<>());
    task9.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy3");

    final List<TaskDto> tasksList = Lists.newArrayList(task1, task2, task3, task4, task5, task6, task7, task8, task9);

    Mockito
        .when(
            processService.findTasks(
                anyString(),
                anyString(),
                anyString(),
                anyBoolean(),
                any(DateTime.class),
                any(DateTime.class),
                anyListOf(String.class),
                anySetOf(TaskDetailsEnum.class)))
        .thenReturn(new PartialList<>(tasksList, tasksList.size()));

    final Set<String> therapyIds = new HashSet<>();
    therapyIds.add("therapy1");
    therapyIds.add("therapy2");
    therapyIds.add("therapy3");
    therapyIds.add("therapy4");

    final Map<String, List<TherapyTaskSimpleDto>> tasksMap = medicationsTasksProvider.findSimpleTasksForTherapies(
        "1",
        therapyIds,
        new DateTime(
            2015,
            9,
            14,
            12,
            0));

    final List<TherapyTaskSimpleDto> tasksList1 = tasksMap.get("therapy1");
    assertEquals(3, tasksList1.size());
    assertEquals("task1", tasksList1.get(0).getId());
    assertEquals(TaskTypeEnum.DOCTOR_REVIEW, tasksList1.get(0).getTaskType());
    assertEquals(new DateTime(2015, 9, 14, 0, 0), tasksList1.get(0).getDueTime());
    assertEquals("task7", tasksList1.get(1).getId());
    assertEquals(TaskTypeEnum.SUPPLY_REVIEW, tasksList1.get(1).getTaskType());
    assertEquals(new DateTime(2015, 9, 14, 0, 0), tasksList1.get(1).getDueTime());
    assertEquals("task5", tasksList1.get(2).getId());
    assertEquals(TaskTypeEnum.SUPPLY_REMINDER, tasksList1.get(2).getTaskType());
    assertEquals(new DateTime(2015, 9, 14, 0, 0), tasksList1.get(2).getDueTime());

    final List<TherapyTaskSimpleDto> tasksList2 = tasksMap.get("therapy2");
    assertEquals(2, tasksList2.size());
    assertEquals("task3", tasksList2.get(0).getId());
    assertEquals(TaskTypeEnum.SWITCH_TO_ORAL, tasksList2.get(0).getTaskType());
    assertEquals(new DateTime(2015, 9, 13, 0, 0), tasksList2.get(0).getDueTime());
    assertEquals("task6", tasksList2.get(1).getId());
    assertEquals(TaskTypeEnum.SUPPLY_REMINDER, tasksList2.get(1).getTaskType());
    assertEquals(new DateTime(2015, 9, 15, 0, 0), tasksList2.get(1).getDueTime());

    final List<TherapyTaskSimpleDto> tasksList3 = tasksMap.get("therapy3");
    assertEquals(2, tasksList3.size());
    assertEquals("task8", tasksList3.get(0).getId());
    assertEquals(TaskTypeEnum.SUPPLY_REVIEW, tasksList3.get(0).getTaskType());
    assertEquals(new DateTime(2015, 9, 20, 0, 0), tasksList3.get(0).getDueTime());
    assertEquals("task9", tasksList3.get(1).getId());
    assertEquals(TaskTypeEnum.PERFUSION_SYRINGE_START, tasksList3.get(1).getTaskType());
    assertEquals(new DateTime(2015, 9, 21, 0, 0), tasksList3.get(1).getDueTime());

  }

  @Test
  public void testGetAdministrationTasks()
  {
    //MOCK

    Mockito.reset(processService);

    final Map<String, PatientDisplayWithLocationDto> patientWithLocationMap = new HashMap<>();
    patientWithLocationMap.put(
        "patient1",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto(
                "patient1", "patient1", new DateTime(2015, 1, 1, 0, 0), Gender.MALE, null), "cp1", "R1B1"));
    Mockito
        .when(patientDataProvider.getPatientDisplayWithLocationMap(patientWithLocationMap.keySet()))
        .thenReturn(patientWithLocationMap);


    final TaskDto task1 = new TaskDto();
    task1.setId("t1");
    task1.setDueTime(new DateTime(2015, 9, 14, 0, 0));
    task1.setTaskExecutionStrategyId(TaskTypeEnum.ADMINISTRATION_TASK.getName());
    task1.setVariables(new HashMap<>());
    task1.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");
    task1.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "START");
    task1.getVariables().put(AdministrationTaskDef.PATIENT_ID.getName(), "patient1");

    final TaskDto task2 = new TaskDto();
    task2.setId("t1");
    task2.setDueTime(new DateTime(2015, 9, 15, 0, 0));
    task2.setTaskExecutionStrategyId(TaskTypeEnum.ADMINISTRATION_TASK.getName());
    task2.setVariables(new HashMap<>());
    task2.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");
    task2.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "STOP");
    task2.getVariables().put(AdministrationTaskDef.PATIENT_ID.getName(), "patient2");

    final List<TaskDto> tasksList = Lists.newArrayList(task1, task2);

    Mockito
        .when(
            processService.findTasks(
                anyString(),
                anyString(),
                anyString(),
                anyBoolean(),
                any(DateTime.class),
                any(DateTime.class),
                anyListOf(String.class),
                anySetOf(TaskDetailsEnum.class)))
        .thenReturn(new PartialList<>(tasksList, tasksList.size()));

    final AdministrationPatientTaskDto patientTask1 = new AdministrationPatientTaskDto();
    patientTask1.setId("t1");
    patientTask1.setPlannedTime(new DateTime(2015, 9, 20, 0, 0));

    final Locale locale = new Locale("en");
    final DateTime when = new DateTime(2016, 1, 7, 12, 15);

    Mockito
        .when(
            administrationTaskConverter.convertTasksToAdministrationPatientTasks(
                Lists.newArrayList(task1),
                patientWithLocationMap,
                locale,
                when))
        .thenReturn(Lists.newArrayList(patientTask1));

    //END MOCK

    final List<AdministrationPatientTaskDto> results = medicationsTasksProvider.findAdministrationTasks(
        Lists.newArrayList("patient1", "patient2", "patient3"),
        new Interval(new DateTime(2015, 1, 6, 12, 0), new DateTime(2016, 1, 7, 12, 0)),
        50,
        AdministrationTypeEnum.START_OR_ADJUST,
        locale,
        when);
    assertEquals(1, results.size());

    assertEquals("t1", results.get(0).getId());
    assertEquals(TaskTypeEnum.ADMINISTRATION_TASK, results.get(0).getTaskType());
    assertEquals(new DateTime(2015, 9, 20, 0, 0), results.get(0).getPlannedTime());
  }

  @Test
  public void testGetAdministrationTasksLimitSize()
  {
    //MOCK
    Mockito.reset(processService);

    final Map<String, PatientDisplayWithLocationDto> patientWithLocationMap = new HashMap<>();
    patientWithLocationMap.put(
        "patient1",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto(
                "patient1", "patient1", new DateTime(2015, 1, 1, 0, 0), Gender.MALE, null), "cp1", "R1B1"));
    patientWithLocationMap.put(
        "patient3",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto(
                "patient3", "patient3", new DateTime(2015, 1, 1, 0, 0), Gender.MALE, null), "cp2", "R2B2"));
    Mockito
        .when(patientDataProvider.getPatientDisplayWithLocationMap(patientWithLocationMap.keySet()))
        .thenReturn(patientWithLocationMap);

    final TaskDto task1 = new TaskDto();
    task1.setId("t1");
    task1.setDueTime(new DateTime(2015, 9, 20, 0, 0));
    task1.setTaskExecutionStrategyId(TaskTypeEnum.ADMINISTRATION_TASK.getName());
    task1.setVariables(new HashMap<>());
    task1.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");
    task1.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "START");
    task1.getVariables().put(AdministrationTaskDef.PATIENT_ID.getName(), "patient1");

    final TaskDto task2 = new TaskDto();
    task2.setId("t2");
    task2.setDueTime(new DateTime(2015, 9, 23, 0, 0));
    task2.setTaskExecutionStrategyId(TaskTypeEnum.ADMINISTRATION_TASK.getName());
    task2.setVariables(new HashMap<>());
    task2.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");
    task2.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "START");
    task2.getVariables().put(AdministrationTaskDef.PATIENT_ID.getName(), "patient2");

    final TaskDto task3 = new TaskDto();
    task3.setId("t3");
    task3.setDueTime(new DateTime(2015, 9, 22, 0, 0));
    task3.setTaskExecutionStrategyId(TaskTypeEnum.ADMINISTRATION_TASK.getName());
    task3.setVariables(new HashMap<>());
    task3.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");
    task3.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "START");
    task3.getVariables().put(AdministrationTaskDef.PATIENT_ID.getName(), "patient3");

    final List<TaskDto> tasksList = Lists.newArrayList(task1, task2, task3);

    Mockito
        .when(
            processService.findTasks(
                anyString(),
                anyString(),
                anyString(),
                anyBoolean(),
                any(DateTime.class),
                any(DateTime.class),
                anyListOf(String.class),
                anySetOf(TaskDetailsEnum.class)))
        .thenReturn(new PartialList<>(tasksList, tasksList.size()));

    final AdministrationPatientTaskDto patientTask1 = new AdministrationPatientTaskDto();
    patientTask1.setId("t1");
    patientTask1.setPlannedTime(new DateTime(2015, 9, 20, 0, 0));

    final AdministrationPatientTaskDto patientTask3 = new AdministrationPatientTaskDto();
    patientTask3.setId("t3");
    patientTask3.setPlannedTime(new DateTime(2015, 9, 22, 0, 0));

    final Locale locale = new Locale("en");
    final DateTime when = new DateTime(2016, 1, 7, 12, 15);

    Mockito
        .when(
            administrationTaskConverter.convertTasksToAdministrationPatientTasks(
                Lists.newArrayList(task1, task3),
                patientWithLocationMap,
                locale,
                when))
        .thenReturn(Lists.newArrayList(patientTask1, patientTask3));

    //END MOCK

    final List<AdministrationPatientTaskDto> results = medicationsTasksProvider.findAdministrationTasks(
        Lists.newArrayList("patient1", "patient2", "patient3"),
        new Interval(new DateTime(2015, 1, 6, 12, 0), new DateTime(2016, 1, 7, 12, 0)),
        2,
        AdministrationTypeEnum.START_OR_ADJUST,
        locale,
        when);
    assertEquals(2, results.size());

    assertEquals("t1", results.get(0).getId());
    assertEquals(TaskTypeEnum.ADMINISTRATION_TASK, results.get(0).getTaskType());
    assertEquals(new DateTime(2015, 9, 20, 0, 0), results.get(0).getPlannedTime());

    assertEquals("t3", results.get(1).getId());
    assertEquals(TaskTypeEnum.ADMINISTRATION_TASK, results.get(1).getTaskType());
    assertEquals(new DateTime(2015, 9, 22, 0, 0), results.get(1).getPlannedTime());
  }

  @Test
  public void testGetAdministrationTasksEmpty()
  {
    Mockito.reset(processService);

    Mockito
        .when(
            processService.findTasks(
                anyString(),
                anyString(),
                anyString(),
                anyBoolean(),
                any(DateTime.class),
                any(DateTime.class),
                anyListOf(String.class),
                anySetOf(TaskDetailsEnum.class)))
        .thenReturn(new PartialList<>(Collections.emptyList(), 0));

    final List<AdministrationPatientTaskDto> tasks = medicationsTasksProvider.findAdministrationTasks(
        Lists.newArrayList("patient1", "patient2", "patient3"),
        new Interval(new DateTime(2016, 1, 6, 12, 0), new DateTime(2016, 1, 7, 12, 0)),
        50,
        AdministrationTypeEnum.START_OR_ADJUST,
        new Locale("en"),
        new DateTime(2016, 1, 7, 12, 15));
    assertTrue(tasks.isEmpty());
  }

  @Test
  public void testLoadNextAdministrationTask()
  {
    Mockito.reset(processService);

    final TaskDto task1 = new TaskDto();
    task1.setId("t1");
    task1.setDueTime(new DateTime(2015, 9, 20, 0, 0));
    task1.setTaskExecutionStrategyId(TaskTypeEnum.ADMINISTRATION_TASK.getName());
    task1.setVariables(new HashMap<>());
    task1.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");
    task1.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "START");

    final TaskDto task2 = new TaskDto();
    task2.setId("t2");
    task2.setDueTime(new DateTime(2015, 9, 23, 0, 0));
    task2.setTaskExecutionStrategyId(TaskTypeEnum.ADMINISTRATION_TASK.getName());
    task2.setVariables(new HashMap<>());
    task2.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");
    task2.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "START");

    final TaskDto task3 = new TaskDto();
    task3.setId("t3");
    task3.setDueTime(new DateTime(2015, 9, 22, 0, 0));
    task3.setTaskExecutionStrategyId(TaskTypeEnum.ADMINISTRATION_TASK.getName());
    task3.setVariables(new HashMap<>());
    task3.getVariables().put(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName(), "therapy1");
    task3.getVariables().put(AdministrationTaskDef.ADMINISTRATION_TYPE.getName(), "START");

    final List<TaskDto> tasksList = Lists.newArrayList(task1, task2, task3);

    final DateTime fromWhen = new DateTime(2015, 9, 21, 0, 0);
    Mockito
        .when(
            processService.findTasks(
                null,
                null,
                null,
                false,
                fromWhen,
                null,
                Lists.newArrayList("ADMINISTRATION_TASK_PATIENT_ID_1"),
                EnumSet.of(TaskDetailsEnum.VARIABLES)))
        .thenReturn(new PartialList<>(tasksList, tasksList.size()));

    final TaskDto taskDto = medicationsTasksProvider.loadNextAdministrationTask("1", fromWhen);
    assertEquals("t3", taskDto.getId());
    assertEquals(new DateTime(2015, 9, 22, 0, 0), taskDto.getDueTime());
  }

  private Map<String, PatientDisplayWithLocationDto> getTestPatientWithLocationMap()
  {
    final Map<String, PatientDisplayWithLocationDto> patientWithLocationMap = new HashMap<>();

    patientWithLocationMap.put(
        "patient1",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto(
                "patient1", "patient1", new DateTime(2015, 1, 1, 0, 0), Gender.MALE, null), "cp1", "R1B1"));
    patientWithLocationMap.put(
        "patient2",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto(
                "patient2", "patient2", new DateTime(2015, 1, 1, 0, 0), Gender.MALE, null), "cp1", "R1B2"));
    patientWithLocationMap.put(
        "patient3",
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto(
                "patient3", "patient3", new DateTime(2015, 1, 1, 0, 0), Gender.MALE, null), "cp2", "R2B2"));
    return patientWithLocationMap;
  }
}
