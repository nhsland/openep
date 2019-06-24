
package com.marand.thinkmed.medications.api.internal.dto;

import java.util.Arrays;

import com.marand.thinkehr.util.ConversionUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */

public enum MedicationRole
{
  THERAPEUTIC("at0080", "Therapeutic"),
  EXCIPIENT("at0084", "Excipient");

  MedicationRole(final String code, final String text)
  {
    this.code = code;
    this.text = text;
  }

  private final String code;
  private final String text;

  public String getCode()
  {
    return code;
  }

  public String getText()
  {
    return text;
  }

  public DvCodedText getDvCodedText()
  {
    return ConversionUtils.getLocalCodedText(code, text);
  }

  public static MedicationRole valueOf(final DvText dvText)
  {
    if (dvText instanceof DvCodedText)
    {
      final String codeString = ((DvCodedText)dvText).getDefiningCode().getCodeString();
      return Arrays.stream(values())
          .filter(v -> v.getCode().equals(codeString))
          .findFirst()
          .orElse(null);
    }
    return null;
  }
}