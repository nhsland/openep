
package com.marand.thinkmed.medications.api.internal.dto.eer;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public enum OutpatientPrescriptionDocumentType
{
  GREEN("at0080", "Green"), //"Prescription paid by health insurance."
  WHITE("at0081", "White"); //"The patient pays for the prescription."

  OutpatientPrescriptionDocumentType(final String code, final String text)
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


  public static OutpatientPrescriptionDocumentType valueOf(final DvText dvText)
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