package com.marand.thinkmed.medications.connector.data.object;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class QuantityWithTimeDto extends DataTransferObject implements JsonSerializable
{
  private final DateTime time;
  private final Double quantity;
  private final String comment;

  public QuantityWithTimeDto(final DateTime time, final Double quantity, final String comment)
  {
    this.time = time;
    this.quantity = quantity;
    this.comment = comment;
  }

  public DateTime getTime()
  {
    return time;
  }

  public Double getQuantity()
  {
    return quantity;
  }

  public String getComment()
  {
    return comment;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("time", time)
        .append("quantity", quantity)
        .append("comment", comment)
    ;
  }
}
