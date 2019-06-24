package com.marand.meds.app.config.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class OAuth2ClientConfigProvider
{
  private Environment environment;
  private String tokenUri;
  private String clientId;

  @Value("${security.oauth2.client.access-token-uri:}")
  public void setTokenUri(final String tokenUri)
  {
    this.tokenUri = tokenUri;
  }

  @Value("${security.oauth2.client.id:}")
  public void setClientId(final String clientId)
  {
    this.clientId = clientId;
  }

  @Autowired
  public void setEnvironment(final Environment environment)
  {
    this.environment = environment;
  }

  public OAuth2ClientConfig get()
  {
    if (isProfileActive("oauth2-auth"))
    {
      return new OAuth2ClientConfig(clientId, tokenUri);
    }
    return null;
  }

  private boolean isProfileActive(final String s)
  {
    return Arrays.stream(environment.getActiveProfiles()).anyMatch(s::equals);
  }

  public static class OAuth2ClientConfig
  {
    private final String clientId;
    private final String tokenUri;

    public OAuth2ClientConfig(final String clientId, final String tokenUri)
    {
      this.clientId = clientId;
      this.tokenUri = tokenUri;
    }

    public String getClientId()
    {
      return clientId;
    }

    public String getTokenUri()
    {
      return tokenUri;
    }
  }
}
