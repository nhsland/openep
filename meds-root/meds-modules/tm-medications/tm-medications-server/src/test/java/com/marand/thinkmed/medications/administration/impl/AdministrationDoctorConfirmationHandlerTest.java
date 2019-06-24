package com.marand.thinkmed.medications.administration.impl;

import com.google.common.collect.Lists;
import com.marand.thinkmed.medications.dto.NotAdministeredReasonEnum;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.times;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class AdministrationDoctorConfirmationHandlerTest
{
  @InjectMocks
  private final AdministrationDoctorConfirmationHandler administrationDoctorConfirmationHandler = new AdministrationDoctorConfirmationHandler();

  @Mock
  private final AdministrationHandlerImpl administrationHandler = new AdministrationHandlerImpl();

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Mock
  private ProcessService processService;

  @Test
  public void testSetDoctorConfirmationTrue()
  {
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setTaskId("t1");

    administrationDoctorConfirmationHandler.setDoctorConfirmation("1", administration, true);

    Mockito.verify(processService, times(1))
        .setVariable("t1", AdministrationTaskDef.DOCTOR_CONFIRMATION.getName(), true);
  }

  @Test
  public void testSetDoctorConfirmationTrueGroup()
  {
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setTaskId("t1");
    administration.setGroupUUId("g1");

    final TaskDto task1 = new TaskDto();
    task1.setId("t1");
    final TaskDto task2 = new TaskDto();
    task2.setId("t2");
    Mockito
        .when(medicationsTasksProvider.findAdministrationTasks("1", null, null, null, "g1", true))
        .thenReturn(Lists.newArrayList(task1, task2));

    administrationDoctorConfirmationHandler.setDoctorConfirmation("1", administration, true);

    Mockito.verify(processService, times(1))
        .setVariable("t1", AdministrationTaskDef.DOCTOR_CONFIRMATION.getName(), true);
    Mockito.verify(processService, times(1))
        .setVariable("t2", AdministrationTaskDef.DOCTOR_CONFIRMATION.getName(), true);
  }

  @Test
  public void testSetDoctorConfirmationChangeFalseToTrue()
  {
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setAdministrationId("a1");
    administration.setTaskId("t1");

    Mockito.when(administrationHandler.uncancelAdministrationTask("1", administration))
        .thenReturn(Lists.newArrayList("t1"));

    administrationDoctorConfirmationHandler.setDoctorConfirmation("1", administration, true);

    Mockito.verify(administrationHandler, times(1))
        .uncancelAdministrationTask("1", administration);
    Mockito.verify(processService, times(1))
        .setVariable("t1", AdministrationTaskDef.DOCTOR_CONFIRMATION.getName(), true);
  }

  @Test
  public void testSetDoctorConfirmationFalse()
  {
    final StartAdministrationDto administration = new StartAdministrationDto();
    administration.setTaskId("t1");

    administrationDoctorConfirmationHandler.setDoctorConfirmation("1", administration, false);

    Mockito.verify(administrationHandler, times(1))
        .cancelAdministrationTask("1", administration, NotAdministeredReasonEnum.DOCTOR_CONFIRMATION_FALSE, null);
  }
}
