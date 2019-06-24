package com.marand.thinkmed.medications.dto;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Nejc Korasa
 */

public enum SubstitutionType
{
  PERFORMED("at0138", "Performed"),
  NOT_PERFORMED("at0139", "Not performed");

  private final String ehrCode;
  private final String ehrName;

  SubstitutionType(final String ehrCode, final String ehrName)
  {
    this.ehrCode = ehrCode;
    this.ehrName = ehrName;
  }

  public String getEhrCode()
  {
    return ehrCode;
  }

  public String getEhrName()
  {
    return ehrName;
  }

  public DvCodedText toDvCodedText()
  {
    return DataValueUtils.getLocalCodedText(ehrCode, ehrName);
  }

  public static SubstitutionType valueOf(final DvText dvText)
  {
    if (dvText instanceof DvCodedText)
    {
      final String codeString = ((DvCodedText)dvText).getDefiningCode().getCodeString();
      return Arrays.stream(values())
          .filter(v -> v.getEhrCode().equals(codeString))
          .findFirst()
          .orElse(null);
    }
    return null;
  }

  public boolean matches(final DvText dvText)
  {
    return dvText != null
        && dvText instanceof DvCodedText
        && ehrCode.equals(((DvCodedText)dvText).getDefiningCode().getCodeString());
  }
}
