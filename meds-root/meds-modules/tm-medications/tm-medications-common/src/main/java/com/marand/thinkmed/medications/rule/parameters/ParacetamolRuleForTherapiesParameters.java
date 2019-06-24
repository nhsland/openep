package com.marand.thinkmed.medications.rule.parameters;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class ParacetamolRuleForTherapiesParameters extends ParacetamolRuleParameters
{
  private List<TherapyDto> therapies = new ArrayList<>();
  private String patientId;

  public void setTherapies(final List<TherapyDto> therapies)
  {
    this.therapies = therapies;
  }

  public List<TherapyDto> getTherapies()
  {
    return therapies;
  }

  public String getPatientId()
  {
    return patientId;
  }

  public void setPatientId(final String patientId)
  {
    this.patientId = patientId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("therapies", therapies)
        .append("patientId", patientId);
  }
}
