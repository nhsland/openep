package com.marand.thinkmed.medications;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Mitja Lapajne
 */
public enum MedicationStartCriterionEnum
{
  BY_DOCTOR_ORDERS("Bt doctors orders");

  private final String ehrValue;

  MedicationStartCriterionEnum(final String ehrValue)
  {
    this.ehrValue = ehrValue;
  }

  public String getEhrValue()
  {
    return ehrValue;
  }

  public DvCodedText getDvCodedText()
  {
    return DataValueUtils.getLocalCodedText(ehrValue, ehrValue);
  }

}