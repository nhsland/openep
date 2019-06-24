package com.marand.thinkmed.request.auth;

import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * Extends Spring's central class for client-side HTTP access, {@link RestTemplate}, adding
 * automatic authentication for service to service calls using the currently authenticated oauth2 principal.
 *
 * <p>
 *     The main advantage to using this class over Spring's <code>RestTemplate</code> is that authentication
 *     is handled automatically, oauth2 token is taken from Spring's security context.
 * </p>
 *
 * @see RestOperations
 * @see RestTemplate
 *
 * @author Nejc Korasa
 */
public class TokenRelayOAuth2RestTemplate extends RestTemplate
{
  private TokenRelayOAuth2RestTemplate(final OAuth2ClientRequestFactory factory)
  {
    super(factory);
  }

  /**
   * Create a new instance
   */
  public static TokenRelayOAuth2RestTemplate build()
  {
    return new TokenRelayOAuth2RestTemplate(new OAuth2ClientRequestFactory());
  }
}