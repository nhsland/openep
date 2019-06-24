package com.marand.thinkmed.medications.event;

/**
 * @author Bostjan Vester
 */
public abstract class PatientEvent extends Event
{
  private final String patientId;

  protected PatientEvent(final String patientId)
  {
    this.patientId = patientId;
  }

  public String getPatientId()
  {
    return patientId;
  }
}
