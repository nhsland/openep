package com.marand.thinkmed.elmdoc.data;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
public class TokenResponseDo implements JsonSerializable
{
  private String ClientId;
  private String Token;

  public String getClientId()
  {
    return ClientId;
  }

  public String getToken()
  {
    return Token;
  }
}
