package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Amadej Bukorović
 */
public class WebAppErrorDto extends DataTransferObject implements JsonSerializable
{
  private String loggerName;
  private String message;
  private String details;

  public String getLoggerName()
  {
    return loggerName;
  }

  public void setLoggerName(final String loggerName)
  {
    this.loggerName = loggerName;
  }

  public String getMessage()
  {
    return message;
  }

  public void setMessage(final String message)
  {
    this.message = message;
  }

  public String getDetails()
  {
    return details;
  }

  public void setDetails(final String details)
  {
    this.details = details;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .appendToString(loggerName)
        .appendToString(message)
        .appendToString(details)
    ;
  }
}
