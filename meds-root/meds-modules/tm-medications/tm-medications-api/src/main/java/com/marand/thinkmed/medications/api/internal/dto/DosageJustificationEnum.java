package com.marand.thinkmed.medications.api.internal.dto;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public enum DosageJustificationEnum
{
  ADJUST_TO_FLUID_BALANCE("Adjust to fluid balance"),
  TITRATION_APTTR("Titration APTT Ratio"),
  TITRATION_INR("Titration INR"),
  TITRATION_MAP("Titration Mean Arterial Pressure"),
  TITRATION_BLOOD_SUGAR("Titration Blood Sugar");

  private final String ehrValue;

  DosageJustificationEnum(final String ehrValue)
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

  public boolean matches(final DvText dvText)
  {
    return dvText instanceof DvCodedText && ehrValue.equals(((DvCodedText)dvText).getDefiningCode().getCodeString());
  }

  public static DosageJustificationEnum valueOf(final DvText dvText)
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