package com.marand.thinkmed.medications.api.internal.dto.pharmacist.perfusionSyringe;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class PerfusionSyringeLabelDto extends DataTransferObject
{
  private String patientName;
  private String patientBirthDate;
  private String patientCareProvider;
  private String patientRoomAndBed;

  private String therapyDisplayValue;
  private String prescribedBy;
  private String preparedBy;
  private String preparationStartedTime;

  private String barCode;    //temporary not used

  public String getPatientName()
  {
    return patientName;
  }

  public void setPatientName(final String patientName)
  {
    this.patientName = patientName;
  }

  public String getPatientBirthDate()
  {
    return patientBirthDate;
  }

  public void setPatientBirthDate(final String patientBirthDate)
  {
    this.patientBirthDate = patientBirthDate;
  }

  public String getPatientCareProvider()
  {
    return patientCareProvider;
  }

  public void setPatientCareProvider(final String patientCareProvider)
  {
    this.patientCareProvider = patientCareProvider;
  }

  public String getPatientRoomAndBed()
  {
    return patientRoomAndBed;
  }

  public void setPatientRoomAndBed(final String patientRoomAndBed)
  {
    this.patientRoomAndBed = patientRoomAndBed;
  }

  public String getTherapyDisplayValue()
  {
    return therapyDisplayValue;
  }

  public void setTherapyDisplayValue(final String therapyDisplayValue)
  {
    this.therapyDisplayValue = therapyDisplayValue;
  }

  public String getPrescribedBy()
  {
    return prescribedBy;
  }

  public void setPrescribedBy(final String prescribedBy)
  {
    this.prescribedBy = prescribedBy;
  }

  public String getPreparedBy()
  {
    return preparedBy;
  }

  public void setPreparedBy(final String preparedBy)
  {
    this.preparedBy = preparedBy;
  }

  public String getPreparationStartedTime()
  {
    return preparationStartedTime;
  }

  public void setPreparationStartedTime(final String preparationStartedTime)
  {
    this.preparationStartedTime = preparationStartedTime;
  }

  public String getBarCode()
  {
    return barCode;
  }

  public void setBarCode(final String barCode)
  {
    this.barCode = barCode;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientName", patientName)
        .append("patientBirthDate", patientBirthDate)
        .append("patientCareProvider", patientCareProvider)
        .append("patientRoomAndBed", patientRoomAndBed)
        .append("therapyDisplayValue", therapyDisplayValue)
        .append("prescribedBy", prescribedBy)
        .append("preparedBy", preparedBy)
        .append("preparationStartedTime", preparationStartedTime)
        .append("barCode", barCode);
  }
}
