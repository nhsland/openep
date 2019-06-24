package com.marand.thinkmed.elmdoc.data;

import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Vid Kumse
 */
@SuppressWarnings("InstanceVariableNamingConvention")
public class AlertDo implements JsonSerializable
{
  private SeverityDo Severity;
  private String Alert;
  private String ProfessionalMonograph;
  private List<ScreenableDrugDo> Drugs;

  public AlertDo(
      final String alert,
      final String professionalMonograph,
      final List<ScreenableDrugDo> drugs)
  {
    Alert = alert;
    ProfessionalMonograph = professionalMonograph;
    Drugs = drugs;
  }

  public AlertDo(
      final SeverityDo severity,
      final String alert,
      final String professionalMonograph,
      final List<ScreenableDrugDo> drugs)
  {
    this(alert, professionalMonograph, drugs);
    Severity = severity;
  }

  public SeverityDo getSeverity()
  {
    return Severity;
  }

  public void setSeverity(final SeverityDo severity)
  {
    Severity = severity;
  }

  public String getAlert()
  {
    return Alert;
  }

  public void setAlert(final String alert)
  {
    Alert = alert;
  }

  public String getProfessionalMonograph()
  {
    return ProfessionalMonograph;
  }

  public void setProfessionalMonograph(final String professionalMonograph)
  {
    ProfessionalMonograph = professionalMonograph;
  }

  public List<ScreenableDrugDo> getDrugs()
  {
    return Drugs;
  }

  public void setDrugs(final List<ScreenableDrugDo> drugs)
  {
    Drugs = drugs;
  }
}
