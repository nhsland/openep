package com.marand.thinkmed.medications.ehr.model.consentform;

import com.marand.thinkehr.util.ConversionUtils;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Vid Kumse
 */
public enum ConsentType
{
  T2("at0005", "Form T2"),
  T3("at0006", "Form T3");

  private final String code;
  private final String text;

  ConsentType(final String code, final String text)
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
}
