package com.marand.meds.app.config;

import com.marand.thinkmed.medications.valueholder.MedicationRoutesValueHolder;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolder;
import com.marand.thinkmed.medications.valueholder.UnitsValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Boris Marn
 */
@Configuration
public class MedsHolderScheduledRun
{
  private final MedicationRoutesValueHolder medicationRoutesValueHolder;
  private final MedicationsValueHolder medicationsValueHolder;
  private final UnitsValueHolder unitsValueHolder;

  @Autowired
  public MedsHolderScheduledRun(
      final MedicationRoutesValueHolder medicationRoutesValueHolder,
      final MedicationsValueHolder medicationsValueHolder,
      final UnitsValueHolder unitsValueHolder)
  {
    this.medicationRoutesValueHolder = medicationRoutesValueHolder;
    this.medicationsValueHolder = medicationsValueHolder;
    this.unitsValueHolder = unitsValueHolder;
  }

  @Scheduled(fixedDelay = 2 * 60 * 1000)
  public void runRoutes()
  {
    medicationRoutesValueHolder.run(false);
  }

  @Scheduled(fixedDelay = 1 * 60 * 1000)
  public void runMeds()
  {
    medicationsValueHolder.run(false);
  }

  @Scheduled(fixedDelay = 1 * 60 * 1000)
  public void runUnits()
  {
    unitsValueHolder.run(false);
  }
}
