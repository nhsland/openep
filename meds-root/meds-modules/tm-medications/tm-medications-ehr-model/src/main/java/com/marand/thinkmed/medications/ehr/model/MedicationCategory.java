
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.util.ConversionUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public enum MedicationCategory
{
  AD_HOC_MIXTURE("at0143", "Ad-hoc mixture");

  MedicationCategory(final String code, final String text)
  {
    this.code = code;
    this.text = text;
  }

  private final String code;
  private final String text;

  public DvCodedText getDvCodedText()
  {
    return ConversionUtils.getLocalCodedText(code, text);
  }

  public String getCode()
  {
    return code;
  }

  public boolean matches(final DvText dvText)
  {
    return dvText instanceof DvCodedText && code.equals(((DvCodedText)dvText).getDefiningCode().getCodeString());
  }
}