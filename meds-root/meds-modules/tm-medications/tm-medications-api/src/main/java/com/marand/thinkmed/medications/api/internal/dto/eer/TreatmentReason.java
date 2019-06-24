
package com.marand.thinkmed.medications.api.internal.dto.eer;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public enum TreatmentReason
{
  DISEASE("at0124", "Disease"),
  INJURY_OUTSIDE_WORK("at0125", "Injury outside work"),
  INDUSTRIAL_DISEASE("at0126", "Industrial disease"),
  OCCUPATIONAL_INJURY("at0127", "Occupational injury"),
  THIRD_PERSON_INJURY("at0128", "Third - person injury");

  TreatmentReason(final String code, final String text)
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

  public static TreatmentReason valueOf(final DvText dvText)
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