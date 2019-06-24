package com.marand.thinkmed.medications.connector.data.object;

import com.marand.thinkmed.api.core.JsonSerializable;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */

public class IntervalDto implements JsonSerializable
{
  private DateTime start;
  private DateTime end;

  public IntervalDto() { }

  public IntervalDto(final @NonNull DateTime start, final @NonNull DateTime end)
  {
    this.start = start;
    this.end = end;
  }

  public static IntervalDto fromInterval(final @NonNull Interval interval)
  {
    return new IntervalDto(interval.getStart(), interval.getEnd());
  }

  public Interval toInterval()
  {
    return new Interval(start, end);
  }

  public DateTime getStart()
  {
    return start;
  }

  public void setStart(final @NonNull DateTime start)
  {
    this.start = start;
  }

  public DateTime getEnd()
  {
    return end;
  }

  public void setEnd(final @NonNull DateTime end)
  {
    this.end = end;
  }

  @Override
  public String toString()
  {
    return String.format("IntervalDto{from=%s, to=%s}", start, end);
  }
}
