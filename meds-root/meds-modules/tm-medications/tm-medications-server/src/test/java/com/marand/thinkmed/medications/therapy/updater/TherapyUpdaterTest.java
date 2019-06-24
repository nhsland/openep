package com.marand.thinkmed.medications.therapy.updater;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Iterables;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationOrderActionEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.administration.impl.AdministrationTaskCreator;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.SaveMedicationOrderDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.task.AdministrationTaskCreateActionEnum;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolder;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.UserDto;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TherapyUpdaterTest
{
  @InjectMocks
  private final TherapyUpdater therapyUpdater = new TherapyUpdater();

  @Mock
  private TherapyDisplayProvider therapyDisplayProvider;

  @Mock
  private TherapyConverter therapyConverter;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsBo medicationsBo;

  @Mock
  private AdministrationTaskCreator administrationTaskCreator;

  @Mock
  private ProcessService processService;

  @Mock
  private AdministrationHandler administrationHandler;

  @Mock
  private MedicationsValueHolder medicationsValueHolder;

  @BeforeClass
  public static void initRequest()
  {
    RequestUser.init(auth -> new UserDto("Test", "Test", "Test", Collections.emptyList()));
  }

  @Test
  public void testPrescribeAndAdminister()
  {
    final SaveMedicationOrderDto saveDto = new SaveMedicationOrderDto();
    saveDto.setActionEnum(MedicationOrderActionEnum.PRESCRIBE_AND_ADMINISTER);
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setStart(new DateTime(2019, 4, 16, 12, 0));
    therapy.setMedication(new MedicationDto(100L, "Aspirin"));
    saveDto.setTherapy(therapy);

    // MOCKS
    final MedicationOrder ehrMedicationOrder = new MedicationOrder();
    Mockito.when(therapyConverter.convertToMedicationOrder(therapy))
        .thenReturn(ehrMedicationOrder);

    final InpatientPrescription savedPrescription = new InpatientPrescription();
    savedPrescription.setUid("uid");
    savedPrescription.setMedicationOrder(new MedicationOrder());
    savedPrescription.getActions().add(MedicationsTestUtils.buildMedicationAction(
        MedicationActionEnum.START,
        new DateTime(2013, 5, 10, 12, 0)));

    Mockito.when(medicationsOpenEhrDao.saveNewInpatientPrescription(
        Matchers.anyString(),
        Matchers.any(InpatientPrescription.class)))
        .thenReturn(savedPrescription);

    Mockito.when(medicationsBo.convertMedicationOrderToTherapyDto(
        savedPrescription,
        savedPrescription.getMedicationOrder(),
        null,
        null,
        false,
        null))
        .thenReturn(therapy);

    Mockito.when(medicationsValueHolder.getMedications())
        .thenReturn(Maps.newHashMap(100L, new MedicationDataDto()));

    final NewTaskRequestDto taskRequest = new NewTaskRequestDto(
        AdministrationTaskDef.INSTANCE,
        AdministrationTaskDef.INSTANCE.buildKey("1"),
        "Aspirin 500mg 3x Oral",
        "Aspirin 500mg 3x Oral",
        TherapyAssigneeEnum.NURSE.name(),
        new DateTime(2019, 4, 16, 12, 0),
        null);

    Mockito.when(administrationTaskCreator.createTaskRequests(
        "1",
        therapy,
        AdministrationTaskCreateActionEnum.PRESCRIBE,
        new DateTime(2019, 4, 16, 12, 0),
        null,
        null))
        .thenReturn(Lists.newArrayList(taskRequest));

    final TaskDto createdTask = new TaskDto();
    final TaskDto[] createdTasks = {createdTask};
    Mockito.when(processService.createTasks(taskRequest))
        .thenReturn(createdTasks);

    therapyUpdater.saveTherapies(
        "1",
        Lists.newArrayList(saveDto),
        "cc1",
        "cp1",
        new NamedExternalDto("p1", "Prescriber 1"),
        new DateTime(2019, 4, 16, 12, 0),
        new Locale("en"));

    Mockito.verify(administrationHandler, Mockito.times(1)).confirmAdministrationTask(
        "1", savedPrescription, createdTask, null, null, new DateTime(2019, 4, 16, 12, 0));
  }

  @Test
  public void testCreateTherapyTasks()
  {
    final InpatientPrescription prescription = new InpatientPrescription();
    prescription.setMedicationOrder(new MedicationOrder());
    prescription.setUid("uid1");
    MedicationsEhrUtils.addMedicationActionTo(
        prescription,
        MedicationActionEnum.START,
        new NamedExternalDto("1", "User"),
        new DateTime(2019, 5, 10, 8, 0));

    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    Mockito
        .when(medicationsBo.convertMedicationOrderToTherapyDto(
            prescription,
            prescription.getMedicationOrder(),
            null,
            null,
            false,
            null))
        .thenReturn(therapy);

    final DateTime now = new DateTime(2019, 5, 10, 12, 0);

    Mockito
        .when(medicationsOpenEhrDao.getFutureAdministrationPlannedTime("1", "uid1", now))
        .thenReturn(new DateTime(2019, 5, 10, 14, 0));

    final List<NewTaskRequestDto> taskRequests = Lists.newArrayList(new NewTaskRequestDto(
        AdministrationTaskDef.INSTANCE,
        "administration",
        null,
        null,
        null,
        new DateTime(2019, 5, 10, 20, 0),
        null));

    Mockito
        .when(administrationTaskCreator.createTaskRequests(
            "1",
            therapy,
            AdministrationTaskCreateActionEnum.REISSUE,
            now,
            null,
            new DateTime(2019, 5, 10, 14, 0)))
        .thenReturn(taskRequests);

    final TaskDto task = new TaskDto();
    task.setId("1");
    final List<TaskDto> tasks = Lists.newArrayList(task);
    Mockito
        .when(processService.createTasks(Iterables.toArray(taskRequests, NewTaskRequestDto.class)))
        .thenReturn(Iterables.toArray(tasks, TaskDto.class));

    final List<TaskDto> result = therapyUpdater.createTherapyTasks(
        "1",
        prescription,
        AdministrationTaskCreateActionEnum.REISSUE,
        null,
        now);
    assertTrue(result.contains(task));
  }
}