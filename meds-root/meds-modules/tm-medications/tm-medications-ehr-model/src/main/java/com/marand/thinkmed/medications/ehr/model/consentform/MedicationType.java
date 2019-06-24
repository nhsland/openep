package com.marand.thinkmed.medications.ehr.model.consentform;

import java.util.Arrays;

import com.marand.thinkehr.util.ConversionUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Vid Kumse
 */
public enum MedicationType
{
  MEDICATION("at0009", "Medication"),
  MEDICATION_GROUP("at0011", "Medication group");

  private final String code;
  private final String text;

  MedicationType(final String code, final String text)
  {
    this.code = code;
    this.text = text;
  }

  public DvCodedText getDvCodedText()
  {
    return ConversionUtils.getLocalCodedText(code, text);
  }

  public String getCode()
  {
    return code;
  }

  public static MedicationType valueOf(final DvText dvText)
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
