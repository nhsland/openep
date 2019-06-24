package com.marand.thinkmed.medications.api.internal.dto;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Nejc Korasa
 */

public enum ReleaseType
{
  MODIFIED_RELEASE("Modified release"),
  GASTRO_RESISTANT("Gastro resistant");

  private final String ehrValue;

  ReleaseType(final String ehrValue)
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

  public static ReleaseType valueOf(final DvText dvText)
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
