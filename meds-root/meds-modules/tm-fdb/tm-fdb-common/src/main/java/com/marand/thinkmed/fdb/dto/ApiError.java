package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Vid Kumse
 */
public class ApiError implements JsonSerializable
{
  private String Message;

  public String getMessage()
  {
    return Message;
  }

  public void setMessage(final String message)
  {
    Message = message;
  }
}
