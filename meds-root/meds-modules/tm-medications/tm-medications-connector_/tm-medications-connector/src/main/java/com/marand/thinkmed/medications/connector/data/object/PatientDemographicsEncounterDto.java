package com.marand.thinkmed.medications.connector.data.object;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class PatientDemographicsEncounterDto extends DataTransferObject implements JsonSerializable
{
  private final PatientDemographicsDto patientDemographics;
  private final EncounterDto encounter;

  public PatientDemographicsEncounterDto(
      final PatientDemographicsDto patientDemographics,
      final EncounterDto encounter)
  {
    this.patientDemographics = patientDemographics;
    this.encounter = encounter;
  }

  public PatientDemographicsDto getPatientDemographics()
  {
    return patientDemographics;
  }

  public EncounterDto getEncounter()
  {
    return encounter;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patient", patientDemographics)
        .append("encounter", encounter);
  }
}
