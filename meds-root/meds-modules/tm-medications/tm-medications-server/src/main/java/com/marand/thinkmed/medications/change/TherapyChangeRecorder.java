package com.marand.thinkmed.medications.change;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.marand.maf.core.eventbus.Event;
import com.marand.maf.core.eventbus.EventDispatcher;
import com.marand.thinkmed.medications.model.impl.TherapyChangedImpl;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AbortAllTherapies;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AbortTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AllergiesChanged;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ConfirmAdministration;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.DeleteAdministration;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ModifyTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.PrescribeTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ReissueAllTherapiesOnReturnFromTemporaryLeave;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ReissueTherapy;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.ReviewPharmacistReview;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SavePharmacistReview;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SuspendAllTherapies;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SuspendAllTherapiesOnTemporaryLeave;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.SuspendTherapy;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class TherapyChangeRecorder implements InitializingBean
{
  private EventDispatcher eventDispatcher;
  private RequestDateTimeHolder requestDateTimeHolder;
  private SessionFactory sessionFactory;

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
  public void setSessionFactory(final SessionFactory sessionFactory)
  {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    eventDispatcher.register(this);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final PrescribeTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final ModifyTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final SuspendTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final SuspendAllTherapies event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final SuspendAllTherapiesOnTemporaryLeave event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final AbortTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final AbortAllTherapies event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final ReissueTherapy event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final ReissueAllTherapiesOnReturnFromTemporaryLeave event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final ConfirmAdministration event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[1];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final DeleteAdministration event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final ReviewPharmacistReview event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final SavePharmacistReview event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handle(final AllergiesChanged event)
  {
    if (event.getState() == Event.State.COMPLETED)
    {
      final String patientId = (String)event.getCallArguments()[0];
      handleChange(patientId);
    }
  }

  private void handleChange(final String patientId)
  {
    final TherapyChangedImpl therapyChanged = new TherapyChangedImpl();
    therapyChanged.setChangeTime(requestDateTimeHolder.getRequestTimestamp());
    therapyChanged.setPatientId(patientId);
    sessionFactory.getCurrentSession().save(therapyChanged);
  }
}
