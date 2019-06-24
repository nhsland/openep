package com.marand.thinkmed.medications.api.internal.dto;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */

public enum InfusionSetChangeEnum
{
  INFUSION_SYSTEM_CHANGE("Infusion system change"),
  INFUSION_SYRINGE_CHANGE("Infusion syringe change");

  private final String ehrValue;

  InfusionSetChangeEnum(final String ehrValue)
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

  public static InfusionSetChangeEnum valueOf(final DvText dvText)
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
}