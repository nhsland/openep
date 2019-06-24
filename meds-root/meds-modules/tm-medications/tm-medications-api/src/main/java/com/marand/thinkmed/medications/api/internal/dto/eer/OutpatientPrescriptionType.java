
package com.marand.thinkmed.medications.api.internal.dto.eer;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public enum OutpatientPrescriptionType
{
  BRAND_NAME("at0092", "Brand name"),
  MAGISTRAL("at0093", "Magistral"),
  INN("at0094", "INN");

  OutpatientPrescriptionType(final String code, final String text)
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

  public static OutpatientPrescriptionType valueOf(final DvText dvText)
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