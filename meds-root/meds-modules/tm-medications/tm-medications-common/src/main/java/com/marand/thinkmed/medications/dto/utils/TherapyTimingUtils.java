package com.marand.thinkmed.medications.dto.utils;

import java.util.List;
import java.util.function.Predicate;

import com.marand.thinkmed.medications.ehr.model.DayOfWeek;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;

/**
 * @author Mitja Lapajne
 */
public final class TherapyTimingUtils
{
  private TherapyTimingUtils()
  {
  }

  public static boolean isInValidDaysOfWeek(final @NonNull DateTime when, final List<String> daysOfWeek)
  {
    if (daysOfWeek == null || daysOfWeek.size() == 7 || daysOfWeek.isEmpty())
    {
      return true;
    }
    final DayOfWeek dayOfWeekEnum = dayOfWeekToEhrEnum(when.withTimeAtStartOfDay());

    return daysOfWeek.stream()
        .map(DayOfWeek::valueOf)
        .anyMatch(Predicate.isEqual(dayOfWeekEnum));
  }

  public static boolean isInValidDaysFrequency(final DateTime start, final DateTime when, final Integer daysFrequency)
  {
    return daysFrequency == null ||
        Days.daysBetween(start.withTimeAtStartOfDay(), when.withTimeAtStartOfDay()).getDays() % daysFrequency == 0;
  }

  public static DayOfWeek dayOfWeekToEhrEnum(final DateTime dateTime)
  {
    if (dateTime.getDayOfWeek() == DateTimeConstants.MONDAY)
    {
      return DayOfWeek.MONDAY;
    }
    if (dateTime.getDayOfWeek() == DateTimeConstants.TUESDAY)
    {
      return DayOfWeek.TUESDAY;
    }
    if (dateTime.getDayOfWeek() == DateTimeConstants.WEDNESDAY)
    {
      return DayOfWeek.WEDNESDAY;
    }
    if (dateTime.getDayOfWeek() == DateTimeConstants.THURSDAY)
    {
      return DayOfWeek.THURSDAY;
    }
    if (dateTime.getDayOfWeek() == DateTimeConstants.FRIDAY)
    {
      return DayOfWeek.FRIDAY;
    }
    if (dateTime.getDayOfWeek() == DateTimeConstants.SATURDAY)
    {
      return DayOfWeek.SATURDAY;
    }
    if (dateTime.getDayOfWeek() == DateTimeConstants.SUNDAY)
    {
      return DayOfWeek.SUNDAY;
    }
    throw new IllegalArgumentException("Day of week conversion error");
  }
}
