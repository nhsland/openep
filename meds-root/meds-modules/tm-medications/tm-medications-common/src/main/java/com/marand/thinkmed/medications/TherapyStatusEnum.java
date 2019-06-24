package com.marand.thinkmed.medications;

import java.util.EnumSet;

/**
 * @author Mitja Lapajne
 */
public enum TherapyStatusEnum
{
  NORMAL,
  ABORTED,
  CANCELLED,
  SUSPENDED,
  LATE,
  VERY_LATE,
  FUTURE;

  public static final EnumSet<TherapyStatusEnum> STOPPED = EnumSet.of(ABORTED, CANCELLED);
}
