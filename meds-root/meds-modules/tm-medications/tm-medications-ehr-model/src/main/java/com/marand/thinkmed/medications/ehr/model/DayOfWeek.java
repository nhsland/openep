
package com.marand.thinkmed.medications.ehr.model;

import java.util.Arrays;

import com.marand.thinkehr.util.ConversionUtils;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public enum DayOfWeek
{
  MONDAY("at0007", "Monday"),
  TUESDAY("at0008", "Tuesday"),
  WEDNESDAY("at0016", "Wednesday"),
  THURSDAY("at0017", "Thursday"),
  FRIDAY("at0018", "Friday"),
  SATURDAY("at0019", "Saturday"),
  SUNDAY("at0020", "Sunday");

  DayOfWeek(final String code, final String text)
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

  public static DayOfWeek valueOf(final DateTime dateTime)
  {
    return valueOf(java.time.DayOfWeek.of(dateTime.getDayOfWeek()).name());
  }

  public static DayOfWeek valueOf(final DvText dvText)
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