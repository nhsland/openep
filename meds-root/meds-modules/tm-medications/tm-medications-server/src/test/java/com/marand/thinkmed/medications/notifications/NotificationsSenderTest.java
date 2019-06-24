package com.marand.thinkmed.medications.notifications;

import java.util.Collections;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.dictionary.MafDictionary;
import com.marand.maf.core.eventbus.Event;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.SaveMedicationOrderDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.notifications.data.NotificationDo;
import com.marand.thinkmed.medications.notifications.data.NotificationEventEnum;
import com.marand.thinkmed.medications.notifications.rest.NotificationsRestService;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AbortTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AdministrationChanged;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ModifyTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.PrescribeTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ReissueTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SaveMedicationsOnAdmission;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SaveMedicationsOnDischarge;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SavePharmacistReview;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SuspendAllTherapies;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SuspendTherapy;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.UserDto;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.mockito.Mockito.when;

/**
 * @author Mitja Lapajne
 */

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class NotificationsSenderTest
{
  @InjectMocks
  private final NotificationsSender notificationsSender = new NotificationsSender();

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Spy
  private final NotificationDataProvider notificationDataProvider = new NotificationDataProvider();

  @Mock
  private NotificationsRestService notificationsRestService;

  @Mock
  private RequestDateTimeHolder requestDateTimeHolder;

  @Before
  public void setUp()
  {
    RequestUser.init(auth -> new UserDto("Test", null, "Test", Collections.emptyList()));
    Dictionary.setDelegate(new MafDictionary("Dictionary", "", new Locale("en")));
    DefinedLocaleHolder.INSTANCE.setLocale(new Locale("en"));

    notificationDataProvider.setMedicationsOpenEhrDao(medicationsOpenEhrDao);
    when(requestDateTimeHolder.getRequestTimestamp()).thenReturn(new DateTime(2017, 2, 16, 12, 0));
  }

  @Test
  public void testNotificationOnNewPrescriptionSimple()
  {
    final PrescribeTherapy event = new PrescribeTherapy();
    event.setState(Event.State.COMPLETED);

    final Object[] callArguments = {"1", Lists.newArrayList(buildSimpleSaveMedicationOrderDto("Aspirin"))};
    event.setCallArguments(callArguments);

    notificationsSender.handle(event);

    final String json = buildNotificationJson(
        "1",
        new DateTime(2017, 2, 16, 12, 0),
        NotificationEventEnum.NEW_INPATIENT_PRESCRIPTION,
        new NamedExternalDto("Test", "Test"),
        "Aspirin added to inpatient prescription list");

    Mockito
        .verify(notificationsRestService, Mockito.timeout(1000))
        .notifications(null, json);
  }

  @Test
  public void testNotificationOnNewPrescriptionComplex()
  {
    final PrescribeTherapy event = new PrescribeTherapy();
    event.setState(Event.State.COMPLETED);

    final Object[] callArguments = {"1", Lists.newArrayList(buildComplexSaveMedicationOrderDto("Dopamin", "Glucose"))};
    event.setCallArguments(callArguments);

    notificationsSender.handle(event);

    final String json = buildNotificationJson(
        "1",
        new DateTime(2017, 2, 16, 12, 0),
        NotificationEventEnum.NEW_INPATIENT_PRESCRIPTION,
        new NamedExternalDto("Test", "Test"),
        "Dopamin + Glucose added to inpatient prescription list");

    Mockito
        .verify(notificationsRestService, Mockito.timeout(10000))
        .notifications(null, json);
  }

  @Test
  public void testNotificationOnNewPrescriptions()
  {
    final PrescribeTherapy event = new PrescribeTherapy();
    event.setState(Event.State.COMPLETED);

    final Object[] callArguments = {
        "1",
        Lists.newArrayList(
            buildComplexSaveMedicationOrderDto("Dopamin", "Glucose"),
            buildSimpleSaveMedicationOrderDto("Aspirin"))};
    event.setCallArguments(callArguments);

    notificationsSender.handle(event);

    final String json = buildNotificationJson(
        "1",
        new DateTime(2017, 2, 16, 12, 0),
        NotificationEventEnum.NEW_INPATIENT_PRESCRIPTIONS,
        new NamedExternalDto("Test", "Test"),
        "New medications added to inpatient prescription list");

    Mockito
        .verify(notificationsRestService, Mockito.timeout(1000))
        .notifications(null, json);
  }

  @Test
  public void testNotificationOnModify()
  {
    final ModifyTherapy event = new ModifyTherapy();
    event.setState(Event.State.COMPLETED);

    final Object[] callArguments = {"1", buildSimpleTherapyDto("Aspirin")};
    event.setCallArguments(callArguments);

    notificationsSender.handle(event);

    final String json = buildNotificationJson(
        "1",
        new DateTime(2017, 2, 16, 12, 0),
        NotificationEventEnum.MODIFIED_INPATIENT_PRESCRIPTION,
        new NamedExternalDto("Test", "Test"),
        "Aspirin prescription changed");

    Mockito
        .verify(notificationsRestService, Mockito.timeout(1000))
        .notifications(null, json);
  }

  @Test
  public void testNotificationOnSuspend()
  {
    final SuspendTherapy event = new SuspendTherapy();
    event.setState(Event.State.COMPLETED);

    final Object[] callArguments = {"1"};
    event.setCallArguments(callArguments);

    event.setResult("uid1");

    mockSimpleEhrMedicationDao("1", "uid1", "Aspirin");

    notificationsSender.handle(event);

    final String json = buildNotificationJson(
        "1",
        new DateTime(2017, 2, 16, 12, 0),
        NotificationEventEnum.SUSPENDED_INPATIENT_PRESCRIPTION,
        new NamedExternalDto("Test", "Test"),
        "Aspirin prescription suspended");

    Mockito
        .verify(notificationsRestService, Mockito.timeout(1000))
        .notifications(null, json);
  }

  @Test
  public void testNotificationOnSuspendAll()
  {
    final SuspendAllTherapies event = new SuspendAllTherapies();
    event.setState(Event.State.COMPLETED);

    final Object[] callArguments = {"1"};
    event.setCallArguments(callArguments);

    event.setResult("uid1");

    notificationsSender.handle(event);

    final String json = buildNotificationJson(
        "1",
        new DateTime(2017, 2, 16, 12, 0),
        NotificationEventEnum.SUSPENDED_ALL_INPATIENT_PRESCRIPTIONS,
        new NamedExternalDto("Test", "Test"),
        "All patient prescriptions suspended");

    Mockito
        .verify(notificationsRestService, Mockito.timeout(1000))
        .notifications(null, json);
  }

  @Test
  public void testNotificationOnReissue()
  {
    final ReissueTherapy event = new ReissueTherapy();
    event.setState(Event.State.COMPLETED);

    final Object[] callArguments = {"1"};
    event.setCallArguments(callArguments);

    event.setResult("uid1");

    mockSimpleEhrMedicationDao("1", "uid1", "Aspirin");

    notificationsSender.handle(event);

    final String json = buildNotificationJson(
        "1",
        new DateTime(2017, 2, 16, 12, 0),
        NotificationEventEnum.REISSUED_INPATIENT_PRESCRIPTION,
        new NamedExternalDto("Test", "Test"),
        "Aspirin prescription reissued");

    Mockito
        .verify(notificationsRestService, Mockito.timeout(1000))
        .notifications(null, json);
  }

  @Test
  public void testNotificationOnStopped()
  {
    final AbortTherapy event = new AbortTherapy();
    event.setState(Event.State.COMPLETED);

    final Object[] callArguments = {"1"};
    event.setCallArguments(callArguments);

    event.setResult("uid1");

    mockComplexEhrMedicationDao("1", "uid1", "Dopamin", "Glucose");

    notificationsSender.handle(event);

    final String json = buildNotificationJson(
        "1",
        new DateTime(2017, 2, 16, 12, 0),
        NotificationEventEnum.STOPPED_INPATIENT_PRESCRIPTION,
        new NamedExternalDto("Test", "Test"),
        "Dopamin + Glucose prescription stopped");

    Mockito
        .verify(notificationsRestService, Mockito.timeout(1000))
        .notifications(null, json);
  }

  @Test
  public void testNotificationOnPharmacistReview()
  {
    final SavePharmacistReview event = new SavePharmacistReview();
    event.setState(Event.State.COMPLETED);

    final Object[] callArguments = {"1"};
    event.setCallArguments(callArguments);

    event.setResult("uid1");

    notificationsSender.handle(event);

    final String json = buildNotificationJson(
        "1",
        new DateTime(2017, 2, 16, 12, 0),
        NotificationEventEnum.NEW_REVIEW_BY_PHARMACIST,
        new NamedExternalDto("Test", "Test"),
        "Inpatient prescription list reviewed by clinical pharmacist");

    Mockito
        .verify(notificationsRestService, Mockito.timeout(1000))
        .notifications(null, json);
  }

  @Test
  public void testNotificationOnUpdateMedicationsOnAdmission()
  {
    final SaveMedicationsOnAdmission event = new SaveMedicationsOnAdmission();
    event.setState(Event.State.COMPLETED);

    final Object[] callArguments = {"1"};
    event.setCallArguments(callArguments);

    event.setResult("uid1");

    notificationsSender.handle(event);

    final String json = buildNotificationJson(
        "1",
        new DateTime(2017, 2, 16, 12, 0),
        NotificationEventEnum.UPDATED_MEDICATION_ON_ADMISSION_LIST,
        new NamedExternalDto("Test", "Test"),
        "Updated list of medications on admission");

    Mockito
        .verify(notificationsRestService, Mockito.timeout(1000))
        .notifications(null, json);
  }

  @Test
  public void testNotificationOnUpdateMedicationsOnDischarge()
  {
    final SaveMedicationsOnDischarge event = new SaveMedicationsOnDischarge();
    event.setState(Event.State.COMPLETED);

    final Object[] callArguments = {"1"};
    event.setCallArguments(callArguments);

    event.setResult("uid1");

    notificationsSender.handle(event);

    final String json = buildNotificationJson(
        "1",
        new DateTime(2017, 2, 16, 12, 0),
        NotificationEventEnum.UPDATED_MEDICATION_ON_DISCHARGE_LIST,
        new NamedExternalDto("Test", "Test"),
        "Updated list of medications on discharge");

    Mockito
        .verify(notificationsRestService, Mockito.timeout(1000))
        .notifications(null, json);
  }

  @Test
  public void testAdministrationChanged()
  {
    final AdministrationChanged event = new AdministrationChanged();
    event.setState(Event.State.COMPLETED);

    final Object[] callArguments = {"1"};
    event.setCallArguments(callArguments);

    event.setResult("uid1");

    notificationsSender.handle(event);

    final String json = buildNotificationJson(
        "1",
        new DateTime(2017, 2, 16, 12, 0),
        NotificationEventEnum.ADMINISTRATIONS_CHANGE,
        new NamedExternalDto("Test", "Test"),
        "Changed administration");

    Mockito
        .verify(notificationsRestService, Mockito.timeout(1000))
        .notifications(null, json);
  }

  private void mockSimpleEhrMedicationDao(final String patientId, final String compositionUid, final String medicationName)
  {
    final MedicationOrder medicationOrder = new MedicationOrder();
    medicationOrder.setMedicationItem(DataValueUtils.getLocalCodedText("111", medicationName));
    mockEhrMedicationDao(patientId, compositionUid, medicationOrder);
  }

  private void mockComplexEhrMedicationDao(
      final String patientId,
      final String compositionUid,
      final String medicationName1,
      final String medicationName2)
  {
    final MedicationOrder medicationOrder = new MedicationOrder();
    medicationOrder.setMedicationItem(DataValueUtils.getText(medicationName1 + " + " + medicationName2));
    mockEhrMedicationDao(patientId, compositionUid, medicationOrder);
  }

  private void mockEhrMedicationDao(
      final String patientId,
      final String compositionUid,
      final MedicationOrder medicationOrder)
  {
    final InpatientPrescription composition = new InpatientPrescription();
    composition.setMedicationOrder(medicationOrder);
    when(medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionUid))
        .thenReturn(composition);
  }

  private SaveMedicationOrderDto buildSimpleSaveMedicationOrderDto(final String medicationName)
  {
    final SaveMedicationOrderDto saveMedicationOrderDto = new SaveMedicationOrderDto();
    final ConstantSimpleTherapyDto therapy = buildSimpleTherapyDto(medicationName);
    saveMedicationOrderDto.setTherapy(therapy);
    return saveMedicationOrderDto;
  }

  private ConstantSimpleTherapyDto buildSimpleTherapyDto(final String medicationName)
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    final MedicationDto medication = new MedicationDto();
    medication.setDisplayName(medicationName);
    therapy.setMedication(medication);
    return therapy;
  }

  private SaveMedicationOrderDto buildComplexSaveMedicationOrderDto(
      final String medicationName1,
      final String medicationName2)
  {
    final SaveMedicationOrderDto saveMedicationOrderDto = new SaveMedicationOrderDto();
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();

    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    final MedicationDto medication1 = new MedicationDto();
    medication1.setDisplayName(medicationName1);
    ingredient1.setMedication(medication1);
    therapy.getIngredientsList().add(ingredient1);

    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    final MedicationDto medication2 = new MedicationDto();
    medication2.setDisplayName(medicationName2);
    ingredient2.setMedication(medication2);
    therapy.getIngredientsList().add(ingredient2);

    saveMedicationOrderDto.setTherapy(therapy);
    return saveMedicationOrderDto;
  }

  private String buildNotificationJson(
      final String patientId,
      final DateTime time,
      final NotificationEventEnum event,
      final NamedExternalDto user,
      final String description)
  {
    final NotificationDo notificationDo = new NotificationDo();
    notificationDo.setPatientId(patientId);
    notificationDo.setTime(time);
    notificationDo.setEvent(event);
    notificationDo.setEventLevel(event.getEventLevel());
    notificationDo.setUser(user);
    notificationDo.setDescription(description);

    return JsonUtil.toJson(notificationDo);
  }
}
