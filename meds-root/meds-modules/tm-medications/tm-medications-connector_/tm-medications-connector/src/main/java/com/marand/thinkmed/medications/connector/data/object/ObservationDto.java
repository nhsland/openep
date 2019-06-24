package com.marand.thinkmed.medications.connector.data.object;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class ObservationDto extends DataTransferObject implements JsonSerializable
{
  private final DateTime timestamp;
  private final Double value;
  private final String comment;

  public ObservationDto(final DateTime timestamp, final Double value)
  {
    this(timestamp, value, null);
  }

  public ObservationDto(final DateTime timestamp, final Double value, final String comment)
  {
    this.timestamp = timestamp;
    this.value = value;
    this.comment = comment;
  }

  public DateTime getTimestamp()
  {
    return timestamp;
  }

  public Double getValue()
  {
    return value;
  }

  public String getComment()
  {
    return comment;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("timestamp", timestamp)
        .append("value", value)
        .append("comment", comment)
    ;
  }
}
