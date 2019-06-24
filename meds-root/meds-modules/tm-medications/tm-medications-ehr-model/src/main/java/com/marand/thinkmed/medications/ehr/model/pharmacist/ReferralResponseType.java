package com.marand.thinkmed.medications.ehr.model.pharmacist;

import com.marand.thinkehr.util.ConversionUtils;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Nejc Korasa
 */

public enum ReferralResponseType
{
  ACCEPTED_IN_FULL("at0006", "Accepted in full"),
  PARTIALLY_ACCEPTED("at0007", "Partially accepted"),
  REJECTED("at0008", "Rejected"),
  FURTHER_DISCUSSION_REQUIRED("at0009", "Further discussion required");

  private final String code;
  private final String text;

  ReferralResponseType(final String code, final String text)
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

  public String getText()
  {
    return text;
  }
}
