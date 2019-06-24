package com.marand.thinkmed.medications.api.internal.dto;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Nejc Korasa
 */
public enum SelfAdministeringActionEnum
{
  CHARTED_BY_NURSE("Charted by nurse"),
  AUTOMATICALLY_CHARTED("Automatically charted"),
  STOP_SELF_ADMINISTERING("Stop self administering");

  private final String ehrValue;

  SelfAdministeringActionEnum(final String ehrValue)
  {
    this.ehrValue = ehrValue;
  }

  public DvCodedText getDvCodedText()
  {
    return DataValueUtils.getLocalCodedText(ehrValue, ehrValue);
  }

  public String getEhrValue()
  {
    return ehrValue;
  }

  public static SelfAdministeringActionEnum valueOf(final DvCodedText dvCodedText)
  {
    if (dvCodedText == null)
    {
      return null;
    }
    return Arrays.stream(values())
        .filter(e -> e.getEhrValue().equals(dvCodedText.getDefiningCode().getCodeString()))
        .findFirst()
        .orElse(null);
  }
}
