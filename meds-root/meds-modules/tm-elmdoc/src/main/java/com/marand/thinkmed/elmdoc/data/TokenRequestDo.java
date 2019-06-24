package com.marand.thinkmed.elmdoc.data;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
public class TokenRequestDo implements JsonSerializable
{
  private final String ClientId;
  private final String ClientSecret;

  public TokenRequestDo(final String clientId, final String clientSecret)
  {
    ClientId = clientId;
    ClientSecret = clientSecret;
  }
}
