package com.marand.thinkmed.medications.event;

import com.marand.maf.core.eventbus.EventProducer;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AdministrationChanged;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AllergiesChanged;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 * EventProducer only produces events when a method is called from external class. When a method with annotation
 * EventProducer is called from the same class nothing happens. This class provides an easy system of triggering events.
 */
@Component
public class MedsEventProducer
{

  @SuppressWarnings("unused")
  @EventProducer(AdministrationChanged.class)
  public void triggerAdministrationChanged(final String patientId)
  {
  }

  @SuppressWarnings("unused")
  @EventProducer(AllergiesChanged.class)
  public void triggerAllergiesChanged(final String patientId)
  {
  }
}
