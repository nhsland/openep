package com.marand.thinkmed.medications;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public enum MedicationDeliveryMethodEnum
{
  RECURRING_CONTINUOUS_INFUSION("Recurring continuous infusion"),
  CONTINUOUS_INFUSION("Continuous infusion"),
  BOLUS("Bolus");

  private final String ehrValue;

  MedicationDeliveryMethodEnum(final String ehrValue)
  {
    this.ehrValue = ehrValue;
  }

  public DvCodedText getDvCodedText()
  {
    return DataValueUtils.getLocalCodedText(ehrValue, ehrValue);
  }

  public String getEhrValue()
  {
    return ehrValue;
  }

  public static boolean isContinuousInfusion(final DvText deliveryMethod)
  {
    return RECURRING_CONTINUOUS_INFUSION.matches(deliveryMethod) || CONTINUOUS_INFUSION.matches(deliveryMethod);
  }

  public static MedicationDeliveryMethodEnum valueOf(final DvText dvText)
  {
    if (dvText instanceof DvCodedText)
    {
      return Arrays.stream(values())
          .filter(e -> e.getEhrValue().equals(((DvCodedText)dvText).getDefiningCode().getCodeString()))
          .findFirst()
          .orElse(null);
    }
    return null;
  }

  public boolean matches(final DvText dvText)
  {
    return dvText instanceof DvCodedText && ehrValue.equals(((DvCodedText)dvText).getDefiningCode().getCodeString());
  }
}