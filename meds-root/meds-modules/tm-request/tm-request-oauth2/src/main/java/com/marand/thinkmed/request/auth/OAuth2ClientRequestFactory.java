package com.marand.thinkmed.request.auth;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

/**
 * Factory for {@link ClientHttpRequest} objects created for server to server secured
 * communication using OAuth2 bearer tokens.
 *
 * @author Nejc Korasa
 */
class OAuth2ClientRequestFactory extends HttpComponentsClientHttpRequestFactory
{
  public static final String AUTHORIZATION_HEADER = "Authorization";

  OAuth2ClientRequestFactory()
  {
    super(HttpClients.custom().disableCookieManagement().build());
  }

  @Override
  protected void postProcessHttpRequest(final HttpUriRequest request)
  {
    final OAuth2AuthenticationDetails oAuth2AuthenticationDetails = getOAuth2AuthenticationDetails();
    request.setHeader(AUTHORIZATION_HEADER, "Bearer " + oAuth2AuthenticationDetails.getTokenValue());
  }

  protected OAuth2AuthenticationDetails getOAuth2AuthenticationDetails()
  {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null)
    {
      throw new IllegalStateException("Cannot set authorization header because there is no authenticated principal");
    }

    return (OAuth2AuthenticationDetails)authentication.getDetails();
  }
}