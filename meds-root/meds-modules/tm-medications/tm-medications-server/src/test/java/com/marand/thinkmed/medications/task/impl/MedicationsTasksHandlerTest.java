package com.marand.thinkmed.medications.task.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.administration.impl.AdministrationTaskCreator;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.process.definition.TaskVariable;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.times;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class MedicationsTasksHandlerTest
{
  @InjectMocks
  private MedicationsTasksHandler medicationsTasksHandler = new MedicationsTasksHandlerImpl();

  @Mock
  private AdministrationTaskCreator administrationTaskCreator;

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Mock
  private ProcessService processService;

  @Test
  public void testSetAdministrationTitratedDose()
  {
    final List<Pair<TaskVariable, Object>> variables = new ArrayList<>();
    variables.add(Pair.of(AdministrationTaskDef.DOSE_TYPE, TherapyDoseTypeEnum.QUANTITY));
    variables.add(Pair.of(AdministrationTaskDef.DOSE_NUMERATOR, 500.0));
    variables.add(Pair.of(AdministrationTaskDef.DOSE_NUMERATOR_UNIT, "mg"));
    Mockito
        .when(administrationTaskCreator.getDoseTaskVariables(any(TherapyDoseDto.class)))
        .thenReturn(variables);

    final List<TaskDto> tasks = new ArrayList<>();
    final TaskDto task2 = new TaskDto();
    task2.setId("task2");
    tasks.add(task2);
    final TaskDto task3 = new TaskDto();
    task3.setId("task3");
    tasks.add(task3);
    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks(
            anyString(), anyListOf(String.class), any(DateTime.class), any(DateTime.class), isNull(String.class), anyBoolean()))
        .thenReturn(tasks);

    medicationsTasksHandler.setAdministrationTitratedDose(
        "patient1",
        "therapy1",
        "task1",
        new TherapyDoseDto(),
        null,
        new DateTime(),
        new DateTime());

    final Map<String, Object> variablesMap = new HashMap<>();
    variablesMap.put(AdministrationTaskDef.DOSE_TYPE.getName(), TherapyDoseTypeEnum.QUANTITY);
    variablesMap.put(AdministrationTaskDef.DOSE_NUMERATOR.getName(), 500.0);
    variablesMap.put(AdministrationTaskDef.DOSE_NUMERATOR_UNIT.getName(), "mg");
    Mockito.verify(processService, times(1)).setVariables("task1", variablesMap);
    Mockito.verify(processService, times(1)).setVariables("task2", variablesMap);
    Mockito.verify(processService, times(1)).setVariables("task3", variablesMap);
  }
}
