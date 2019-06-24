package com.marand.thinkmed.medications.dto.administration;

import java.util.EnumSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * @author Mitja Lapajne
 */
public enum AdministrationStatusEnum
{
  PLANNED, DUE, LATE, COMPLETED, COMPLETED_LATE, COMPLETED_EARLY, FAILED;

  public static final Set<AdministrationStatusEnum> PENDING = EnumSet.of(PLANNED, DUE, LATE);

  public static AdministrationStatusEnum build(final DateTime planned, final DateTime completed, final DateTime when)
  {
    if (completed == null)
    {
      if (isAdministrationEarly(planned, when))
      {
        return PLANNED;
      }
      if (isAdministrationLate(planned, when))
      {
        return LATE;
      }

      return DUE;
    }
    else
    {
      if (isAdministrationEarly(planned, completed))
      {
        return COMPLETED_EARLY;
      }
      if (isAdministrationLate(planned, completed))
      {
        return COMPLETED_LATE;
      }

      return COMPLETED;
    }
  }

  public static AdministrationStatusEnum build(final DateTime planned, final DateTime completed)
  {
    if (planned == null || completed == null)
    {
      return COMPLETED;
    }

    if (isAdministrationEarly(planned, completed))
    {
      return COMPLETED_EARLY;
    }
    if (isAdministrationLate(planned, completed))
    {
      return COMPLETED_LATE;
    }

    return COMPLETED;
  }

  private static boolean isAdministrationLate(final DateTime planned, final DateTime compare)
  {
    return getOffsetInMinutes(planned, compare) > 30;
  }

  private static boolean isAdministrationEarly(final DateTime planned, final DateTime compare)
  {
    return getOffsetInMinutes(planned, compare) < -30;
  }

  private static long getOffsetInMinutes(final DateTime planned, final DateTime compare)
  {
    return new Duration(planned, compare).getStandardMinutes();
  }

}
