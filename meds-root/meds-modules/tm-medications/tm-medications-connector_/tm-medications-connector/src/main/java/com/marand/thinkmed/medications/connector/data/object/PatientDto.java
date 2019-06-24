package com.marand.thinkmed.medications.connector.data.object;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class PatientDto extends DataTransferObject implements JsonSerializable
{
  private PatientDemographicsDto demographics;
  private EncounterDto encounter;
  private Double weight;
  private Double height;
  private AllergiesDto allergies;
  private List<DiseaseDto> diseases = new ArrayList<>();

  public PatientDemographicsDto getDemographics()
  {
    return demographics;
  }

  public void setDemographics(final PatientDemographicsDto demographics)
  {
    this.demographics = demographics;
  }

  public EncounterDto getEncounter()
  {
    return encounter;
  }

  public Double getWeight()
  {
    return weight;
  }

  public void setWeight(final Double weight)
  {
    this.weight = weight;
  }

  public Double getHeight()
  {
    return height;
  }

  public void setHeight(final Double height)
  {
    this.height = height;
  }

  public AllergiesDto getAllergies()
  {
    return allergies;
  }

  public void setAllergies(final AllergiesDto allergies)
  {
    this.allergies = allergies;
  }

  public List<DiseaseDto> getDiseases()
  {
    return diseases;
  }

  public void setDiseases(final List<DiseaseDto> diseases)
  {
    this.diseases = diseases;
  }

  public void setEncounter(final EncounterDto encounter)
  {
    this.encounter = encounter;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientDemographics", demographics)
        .append("encounter", encounter)
        .append("weight", weight)
        .append("height", height)
        .append("allergies", allergies)
        .append("diseases", diseases);
  }
}
