package com.marand.thinkmed.medications.api.internal.dto;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Klavdij Lapajne
 */
public enum TherapyDoseTypeEnum
{
  RATE, QUANTITY, VOLUME_SUM, RATE_QUANTITY, RATE_VOLUME_SUM;

  public static final Set<TherapyDoseTypeEnum> WITH_RATE = EnumSet.of(RATE, RATE_QUANTITY, RATE_VOLUME_SUM);
}