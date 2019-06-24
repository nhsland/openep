package com.marand.thinkmed.medications.reconciliation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.marand.maf.core.Pair;
import com.marand.maf.core.PartialList;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.task.AdmissionReviewTaskDef;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.medications.task.PharmacistReviewTaskDef;
import com.marand.thinkmed.medications.task.ReconciliationReviewTaskDef;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.UserDto;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.times;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ReconciliationReviewHandlerTest
{
  @InjectMocks
  private final ReconciliationReviewHandler reconciliationReviewHandler = new ReconciliationReviewHandler();

  @Mock
  private ProcessService processService;

  @Mock
  private RequestDateTimeHolder requestDateTimeHolder;

  @BeforeClass
  public static void setup()
  {
    RequestUser.init(auth -> new UserDto("Test", "Test", "Test", Collections.emptyList()));
  }

  @Before
  public void resetMocks()
  {
    Mockito.reset(processService);
  }

  @Test
  public void testCreateReviewTaskUpdate()
  {
    mockFindTasks(1);

    final DateTime now = new DateTime(2019, 4, 22, 13, 0);
    Mockito.when(requestDateTimeHolder.getRequestTimestamp())
        .thenReturn(now);

    reconciliationReviewHandler.updateReviewTask("1", TaskTypeEnum.ADMISSION_REVIEW_TASK);

    Mockito.verify(processService, times(1)).setDueDate("t1", now);

    final Map<String, Object> variablesMap = new HashMap<>();
    variablesMap.put(PharmacistReviewTaskDef.LAST_EDITOR_NAME.getName(), "Test");
    variablesMap.put(PharmacistReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS.getName(), now.getMillis());
    Mockito.verify(processService, times(1)).setVariables("t1", variablesMap);
  }

  @Test
  public void testCreateReviewTaskUpdateMultipleTasks()
  {
    mockFindTasks(2);

    final DateTime now = new DateTime(2019, 4, 22, 13, 0);
    Mockito.when(requestDateTimeHolder.getRequestTimestamp())
        .thenReturn(now);

    reconciliationReviewHandler.updateReviewTask("1", TaskTypeEnum.ADMISSION_REVIEW_TASK);

    Mockito.verify(processService, times(1)).setDueDate("t1", now);

    final Map<String, Object> variablesMap = new HashMap<>();
    variablesMap.put(PharmacistReviewTaskDef.LAST_EDITOR_NAME.getName(), "Test");
    variablesMap.put(PharmacistReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS.getName(), now.getMillis());
    Mockito.verify(processService, times(1)).setVariables("t1", variablesMap);
  }

  @Test
  public void testCreateReviewTaskCreate()
  {
    mockFindTasks(0);

    final DateTime now = new DateTime(2019, 4, 22, 13, 0);
    Mockito.when(requestDateTimeHolder.getRequestTimestamp())
        .thenReturn(now);

    reconciliationReviewHandler.updateReviewTask("1", TaskTypeEnum.ADMISSION_REVIEW_TASK);

    final ArgumentCaptor<NewTaskRequestDto> taskRequestCapture = ArgumentCaptor.forClass(NewTaskRequestDto.class);
    Mockito.verify(processService, times(1)).createTasks(taskRequestCapture.capture());

    final NewTaskRequestDto taskRequest = taskRequestCapture.getValue();
    assertEquals(AdmissionReviewTaskDef.INSTANCE, taskRequest.getTaskDefinition());
    assertEquals(AdmissionReviewTaskDef.INSTANCE.buildKey("1"), taskRequest.getName());
    assertEquals(AdmissionReviewTaskDef.INSTANCE.buildKey("1"), taskRequest.getName());
    assertEquals(TaskTypeEnum.ADMISSION_REVIEW_TASK.getName(), taskRequest.getDisplayName());
    assertEquals(TaskTypeEnum.ADMISSION_REVIEW_TASK.getName(), taskRequest.getDescription());
    assertEquals(TherapyAssigneeEnum.PHARMACIST.name(), taskRequest.getAssignee());
    assertEquals(now, taskRequest.getDue());
    assertTrue(taskRequest.getVariables().contains(Pair.of(MedsTaskDef.PATIENT_ID, "1")));
    assertTrue(taskRequest.getVariables().contains(Pair.of(ReconciliationReviewTaskDef.LAST_EDITOR_NAME, "Test")));
    assertTrue(taskRequest.getVariables().contains(
        Pair.of(ReconciliationReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS, now.getMillis())));
  }

  @Test
  public void testCompleteReviewTask()
  {
    mockFindTasks(1);

    reconciliationReviewHandler.completeReviewTask("1", TaskTypeEnum.ADMISSION_REVIEW_TASK);
    Mockito.verify(processService, times(1)).completeTasks("t1");
  }

  @Test
  public void testDeleteReviewTask()
  {
    mockFindTasks(1);
    reconciliationReviewHandler.deleteReviewTask("1", TaskTypeEnum.ADMISSION_REVIEW_TASK);
    Mockito.verify(processService, times(1)).deleteTasks(Lists.newArrayList("t1"));
  }

  @Test
  public void testReviewTaskExists()
  {
    mockFindTasks(1);
    assertTrue(reconciliationReviewHandler.reviewTaskExists("1", TaskTypeEnum.ADMISSION_REVIEW_TASK));
  }

  private void mockFindTasks(final int numberOfTasks)
  {
    final List<TaskDto> tasksList = new ArrayList<>();
    if (numberOfTasks > 0)
    {
      final TaskDto task1 = new TaskDto();
      task1.setId("t1");
      task1.setDueTime(new DateTime(2019, 4, 22, 12, 0));
      task1.setTaskExecutionStrategyId(TaskTypeEnum.ADMISSION_REVIEW_TASK.getName());
      task1.setVariables(new HashMap<>());
      task1.getVariables().put(PharmacistReviewTaskDef.LAST_EDITOR_NAME.getName(), "editor1");
      task1.getVariables()
          .put(PharmacistReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS.getName(), new DateTime(2019, 4, 22, 11, 0).getMillis());
      tasksList.add(task1);
    }
    if (numberOfTasks > 1)
    {
      final TaskDto task2 = new TaskDto();
      task2.setId("t2");
      task2.setDueTime(new DateTime(2019, 4, 22, 12, 0));
      task2.setTaskExecutionStrategyId(TaskTypeEnum.ADMISSION_REVIEW_TASK.getName());
      task2.setVariables(new HashMap<>());
      task2.getVariables().put(PharmacistReviewTaskDef.LAST_EDITOR_NAME.getName(), "editor2");
      task2.getVariables()
          .put(PharmacistReviewTaskDef.LAST_EDIT_TIMESTAMP_MILLIS.getName(), new DateTime(2019, 4, 22, 11, 0).getMillis());
      tasksList.add(task2);
    }

    //noinspection unchecked
    Mockito
        .when(processService.findTasks(
            anyString(),
            anyString(),
            anyString(),
            anyBoolean(),
            any(DateTime.class),
            any(DateTime.class),
            anyListOf(String.class),
            anySetOf(TaskDetailsEnum.class)))
        .thenReturn(new PartialList<>(tasksList, tasksList.size()));
  }
}
