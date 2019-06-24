package com.marand.thinkmed.medications;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Klavdij Lapajne
 */
public enum TherapySortTypeEnum
{
  DESCRIPTION_ASC,
  DESCRIPTION_DESC,
  CREATED_TIME_ASC,
  CREATED_TIME_DESC;

  public static final Set<TherapySortTypeEnum> DESCRIPTION = EnumSet.of(DESCRIPTION_ASC, DESCRIPTION_DESC);
  public static final Set<TherapySortTypeEnum> CREATED_TIME = EnumSet.of(CREATED_TIME_ASC, CREATED_TIME_DESC);
}