package com.marand.thinkmed.medications;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Mitja Lapajne
 */
public enum HeparinEnum
{
  HEPARIN_1("Heparin 1"),
  HEPARIN_05("Heparin 0.5");

  private final String ehrValue;

  HeparinEnum(final String ehrValue)
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

  public static HeparinEnum valueOf(final DvCodedText dvCodedText)
  {
    return Arrays.stream(values())
        .filter(e -> e.getEhrValue().equals(dvCodedText.getDefiningCode().getCodeString()))
        .findFirst()
        .orElse(null);
  }
}