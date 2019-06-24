package com.marand.thinkmed.medications.api.internal.dto;

import java.util.Arrays;

import com.marand.openehr.util.DataValueUtils;
import org.openehr.jaxb.rm.DvCodedText;

/**
 * @author Mitja Lapajne
 */
public enum DosingFrequencyTypeEnum
{
  BETWEEN_DOSES("Between doses"), //time between doses in hours
  DAILY_COUNT("Daily count"), //number of administrations per day
  MORNING("Morning"), //once per day in the morning
  NOON("Noon"), //once per day at noon
  EVENING("Evening"), //once per day in the evening
  ONCE_THEN_EX("Once then ex"); //only once

  private final String ehrValue;

  DosingFrequencyTypeEnum(final String ehrValue)
  {
    this.ehrValue = ehrValue;
  }

  public String getEhrValue()
  {
    return ehrValue;
  }

  public DvCodedText getDvCodedText()
  {
    return DataValueUtils.getLocalCodedText(ehrValue, ehrValue);
  }

  public static DosingFrequencyTypeEnum valueOf(final DvCodedText dvCodedText)
  {
    return Arrays.stream(values())
        .filter(e -> e.getEhrValue().equals(dvCodedText.getDefiningCode().getCodeString()))
        .findFirst()
        .orElse(null);
  }
}