package com.marand.thinkmed.medications.notifications;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.maf.core.StackTraceUtils;
import com.marand.maf.core.eventbus.Event;
import com.marand.maf.core.eventbus.EventDispatcher;
import com.marand.maf.core.security.SecurityUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dto.SaveMedicationOrderDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.notifications.data.NotificationDo;
import com.marand.thinkmed.medications.notifications.data.NotificationEventEnum;
import com.marand.thinkmed.medications.notifications.rest.NotificationsRestService;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AbortAllTherapies;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AbortTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AdministrationChanged;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ConfirmAdministration;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.CreateAdministration;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.DeleteAdministration;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ModifyTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.PrescribeTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ReissueAllTherapiesOnReturnFromTemporaryLeave;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ReissueTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.RescheduleTask;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.RescheduleTasks;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SaveMedicationsOnAdmission;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SaveMedicationsOnDischarge;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SavePharmacistReview;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SuspendAllTherapies;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SuspendAllTherapiesOnTemporaryLeave;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SuspendTherapy;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.UserDto;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public class NotificationsSender implements InitializingBean
{
  private static final Logger LOG = LoggerFactory.getLogger(NotificationsSender.class);

  private final ThreadPoolTaskExecutor executor = buildThreadPoolTaskExecutor();
  private String restUrl;
  private EventDispatcher eventDispatcher;
  private NotificationsRestService restService;
  private RequestDateTimeHolder requestDateTimeHolder;
  private NotificationDataProvider notificationDataProvider;

  @Autowired
  public void setRequestDateTimeHolder(final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  @Autowired
  public void setEventDispatcher(final EventDispatcher eventDispatcher)
  {
    this.eventDispatcher = eventDispatcher;
  }

  @Autowired
  public void setNotificationDataProvider(final NotificationDataProvider notificationDataProvider)
  {
    this.notificationDataProvider = notificationDataProvider;
  }

  @Value("${meds.notifications-url:#{null}}")
  public void setRestUrl(final String restUrl)
  {
    this.restUrl = restUrl;
  }

  public void setRestService(final NotificationsRestService restService)
  {
    this.restService = restService;
  }

  private static ThreadPoolTaskExecutor buildThreadPoolTaskExecutor()
  {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setMaxPoolSize(2);
    executor.initialize();
    return executor;
  }

  @Override
  public void afterPropertiesSet()
  {
    if (StringUtils.isNotBlank(restUrl))
    {
      eventDispatcher.register(this);

      final HttpClientContext context = HttpClientContext.create();
      final ResteasyClient client = new ResteasyClientBuilder().httpEngine(
          new ApacheHttpClient4Engine(
              HttpClientBuilder.create().setConnectionManager(
                  new PoolingHttpClientConnectionManager()).build(), context)).build();
      final ResteasyWebTarget target = client.target(restUrl);
      //target.register(new BasicAuthentication("taavci", "test"));
      restService = target.proxy(NotificationsRestService.class);
    }
  }

  @SuppressWarnings("unchecked")
  @Subscribe
  @AllowConcurrentEvents
  public void handle(final PrescribeTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      final List<SaveMedicationOrderDto> medicationOrders = (List<SaveMedicationOrderDto>)event.getCallArguments()[1];

      if (medicationOrders.size() == 1)
      {
        sendNotification(
            patientId,
            NotificationEventEnum.NEW_INPATIENT_PRESCRIPTION,
            () -> notificationDataProvider.getLocalisedDescription(
                "meds.notifications.new.inpatient.prescription",
                notificationDataProvider.getMedicationName(medicationOrders.get(0).getTherapy())));
      }
      else if (medicationOrders.size() > 1)
      {
        sendNotification(
            patientId,
            NotificationEventEnum.NEW_INPATIENT_PRESCRIPTIONS,
            () -> notificationDataProvider.getLocalisedDescription("meds.notifications.new.inpatient.prescriptions"));
      }
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final ModifyTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      final TherapyDto therapy = (TherapyDto)event.getCallArguments()[1];

      sendNotification(
          patientId,
          NotificationEventEnum.MODIFIED_INPATIENT_PRESCRIPTION,
          () -> notificationDataProvider.getLocalisedDescription(
              "meds.notifications.prescription.changed",
              notificationDataProvider.getMedicationName(therapy)));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final SuspendTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      final String newCompositionUid = (String)event.getResult();

      sendNotification(
          patientId,
          NotificationEventEnum.SUSPENDED_INPATIENT_PRESCRIPTION,
          () -> notificationDataProvider.getLocalisedDescription(
              "meds.notifications.prescription.suspended",
              notificationDataProvider.getMedicationName(patientId, newCompositionUid)));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final SuspendAllTherapies event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];

      sendNotification(
          patientId,
          NotificationEventEnum.SUSPENDED_ALL_INPATIENT_PRESCRIPTIONS,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.prescription.suspended.all"));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final SuspendAllTherapiesOnTemporaryLeave event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];

      sendNotification(
          patientId,
          NotificationEventEnum.SUSPENDED_ALL_INPATIENT_PRESCRIPTIONS,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.prescription.suspended.all"));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final AbortTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      final String newCompositionUid = (String)event.getResult();

      sendNotification(
          patientId,
          NotificationEventEnum.STOPPED_INPATIENT_PRESCRIPTION,
          () -> notificationDataProvider.getLocalisedDescription(
              "meds.notifications.prescription.stopped",
              notificationDataProvider.getMedicationName(patientId, newCompositionUid)));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final AbortAllTherapies event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];

      sendNotification(
          patientId,
          NotificationEventEnum.STOPPED_ALL_INPATIENT_PRESCRIPTIONS,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.prescription.stopped.all"));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final ReissueTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      final String newCompositionUid = (String)event.getResult();

      sendNotification(
          patientId,
          NotificationEventEnum.REISSUED_INPATIENT_PRESCRIPTION,
          () -> notificationDataProvider.getLocalisedDescription(
              "meds.notifications.prescription.reissued",
              notificationDataProvider.getMedicationName(patientId, newCompositionUid)));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final SavePharmacistReview event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];

      sendNotification(
          patientId,
          NotificationEventEnum.NEW_REVIEW_BY_PHARMACIST,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.prescription.reviewed.by.pharmacist"));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final SaveMedicationsOnAdmission event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];

      sendNotification(
          patientId,
          NotificationEventEnum.UPDATED_MEDICATION_ON_ADMISSION_LIST,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.admission.list.updated"));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final SaveMedicationsOnDischarge event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];

      sendNotification(
          patientId,
          NotificationEventEnum.UPDATED_MEDICATION_ON_DISCHARGE_LIST,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.discharge.list.updated"));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final AdministrationChanged event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];

      sendNotification(
          patientId,
          NotificationEventEnum.ADMINISTRATIONS_CHANGE,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.administration.changed"));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final ConfirmAdministration event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[1];

      sendNotification(
          patientId,
          NotificationEventEnum.ADMINISTRATIONS_CHANGE,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.administration.changed"));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final CreateAdministration event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[2];

      sendNotification(
          patientId,
          NotificationEventEnum.ADMINISTRATIONS_CHANGE,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.administration.changed"));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final ReissueAllTherapiesOnReturnFromTemporaryLeave event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];

      sendNotification(
          patientId,
          NotificationEventEnum.ADMINISTRATIONS_CHANGE,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.administration.changed"));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final RescheduleTasks event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];

      sendNotification(
          patientId,
          NotificationEventEnum.ADMINISTRATIONS_CHANGE,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.administration.changed"));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final RescheduleTask event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];

      sendNotification(
          patientId,
          NotificationEventEnum.ADMINISTRATIONS_CHANGE,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.administration.changed"));
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final DeleteAdministration event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];

      sendNotification(
          patientId,
          NotificationEventEnum.ADMINISTRATIONS_CHANGE,
          () -> notificationDataProvider.getLocalisedDescription("meds.notifications.administration.changed"));
    }
  }


  @SuppressWarnings("OverlyBroadCatchBlock")
  private void sendNotification(
      final String patientId,
      final NotificationEventEnum event,
      final Supplier<String> descriptionSupplier)
  {
    final UserDto user = RequestUser.getUser();
    final DateTime requestTimestamp = requestDateTimeHolder.getRequestTimestamp();
    final String authHeader = SecurityUtils.calculateAuthHeader();

    executor.execute(
        () ->
        {
          try
          {
            final NotificationDo notificationDo = new NotificationDo();
            notificationDo.setPatientId(patientId);
            notificationDo.setTime(requestTimestamp);
            notificationDo.setUser(Opt.of(user).map(m -> new NamedExternalDto(m.getId(), m.getFullName())).orElse(null));
            notificationDo.setEvent(event);
            notificationDo.setEventLevel(event.getEventLevel());
            notificationDo.setDescription(descriptionSupplier.get());
            restService.notifications(authHeader, JsonUtil.toJson(notificationDo));
          }
          catch (final Exception e)
          {
            LOG.error("Failed sending event notification to: " + restUrl + " \n" + StackTraceUtils.getStackTraceString(e));
          }
        }
    );
  }
}
