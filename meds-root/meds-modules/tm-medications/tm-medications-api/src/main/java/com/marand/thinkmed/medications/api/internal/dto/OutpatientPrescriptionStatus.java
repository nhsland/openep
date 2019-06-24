
package com.marand.thinkmed.medications.api.internal.dto;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public enum OutpatientPrescriptionStatus
{
  PRESCRIBED("at0130", "Prescribed"),
  PARTIALLY_USED("at0131", "Partially used"),
  PARTIALLY_USED_AND_CANCELLED("at0132", "Partially used and cancelled"),
  CANCELLED("at0133", "Cancelled"),
  USED("at0134", "Used"),
  IN_PREPARATION("at0135", "In preparation"),
  IN_DISPENSE("at0136", "In dispense"),
  REJECTED("at0141", "Rejected"),
  WITHDRAWN("at0142", "Withdrawn"),
  PARTIALLY_USED_AND_REJECTED("at0143", "Partially used and rejected");

  OutpatientPrescriptionStatus(final String code, final String text)
  {
    this.code = code;
    this.text = text;
  }

  private final String code;
  private final String text;

  public DvCodedText getDvCodedText()
  {
    return DataValueUtils.getLocalCodedText(code, text);
  }

  public String getCode()
  {
    return code;
  }

  public static OutpatientPrescriptionStatus valueOf(final DvText dvText)
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