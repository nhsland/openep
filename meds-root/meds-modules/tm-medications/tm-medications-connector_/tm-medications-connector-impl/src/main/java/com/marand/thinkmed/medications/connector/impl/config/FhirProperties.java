package com.marand.thinkmed.medications.connector.impl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Boris Marn.
 */
@ConfigurationProperties(prefix = "fhir")
public class FhirProperties
{
  private String patientIdSystem;
  private String encounterIdSystem;
  private String wardIdSystem;
  private String wardLocationPhysicalType;
  private String locationLocationPhysicalType;
  private String doctorParticipantType;

  private String encounterServerUri;
  private String encounterTicketHeaderName;

  private String patientServerUri;
  private String patientTicketHeaderName;

  public String getPatientIdSystem()
  {
    return patientIdSystem;
  }

  public void setPatientIdSystem(final String patientIdSystem)
  {
    this.patientIdSystem = patientIdSystem;
  }

  public String getEncounterIdSystem()
  {
    return encounterIdSystem;
  }

  public void setEncounterIdSystem(final String encounterIdSystem)
  {
    this.encounterIdSystem = encounterIdSystem;
  }

  public String getWardIdSystem()
  {
    return wardIdSystem;
  }

  public void setWardIdSystem(final String wardIdSystem)
  {
    this.wardIdSystem = wardIdSystem;
  }

  public String getWardLocationPhysicalType()
  {
    return wardLocationPhysicalType;
  }

  public void setWardLocationPhysicalType(final String wardLocationPhysicalType)
  {
    this.wardLocationPhysicalType = wardLocationPhysicalType;
  }

  public String getLocationLocationPhysicalType()
  {
    return locationLocationPhysicalType;
  }

  public void setLocationLocationPhysicalType(final String locationLocationPhysicalType)
  {
    this.locationLocationPhysicalType = locationLocationPhysicalType;
  }

  public String getDoctorParticipantType()
  {
    return doctorParticipantType;
  }

  public void setDoctorParticipantType(final String doctorParticipantType)
  {
    this.doctorParticipantType = doctorParticipantType;
  }

  public String getEncounterServerUri()
  {
    return encounterServerUri;
  }

  public void setEncounterServerUri(final String encounterServerUri)
  {
    this.encounterServerUri = encounterServerUri;
  }

  public String getEncounterTicketHeaderName()
  {
    return encounterTicketHeaderName;
  }

  public void setEncounterTicketHeaderName(final String encounterTicketHeaderName)
  {
    this.encounterTicketHeaderName = encounterTicketHeaderName;
  }

  public String getPatientServerUri()
  {
    return patientServerUri;
  }

  public void setPatientServerUri(final String patientServerUri)
  {
    this.patientServerUri = patientServerUri;
  }

  public String getPatientTicketHeaderName()
  {
    return patientTicketHeaderName;
  }

  public void setPatientTicketHeaderName(final String patientTicketHeaderName)
  {
    this.patientTicketHeaderName = patientTicketHeaderName;
  }
}
