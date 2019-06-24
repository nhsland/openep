package com.marand.thinkmed.medications.connector.impl.provider.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import com.marand.maf.core.Opt;
import com.marand.thinkmed.request.user.OAuth2UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

/**
 * @author Mitja Lapajne
 * @author Nejc Korasa
 */

public class FhirClientFactory
{
  private Environment environment;
  private FhirContext fhirContext;

  @Value("${fhir.security:}")
  private String fhirSecurity;

  @Value("${security.oauth2.external-ticket-claim:ticket}")
  private String externalTicketClaim;

  @Value("${fhir.security.ticket-header-name:ticket}")
  private String ticketName;

  @Value("${fhir.log-request-summary:false}")
  private Boolean logRequestSummary;

  @Value("${fhir.log-request-body:false}")
  private Boolean logRequestBody;

  @Value("${fhir.log-response-summary:false}")
  private Boolean logResponseSummary;

  @Value("${fhir.log-response-body:false}")
  private Boolean logResponseBody;

  @Autowired
  public void setEnvironment(final Environment environment)
  {
    this.environment = environment;
  }

  @Autowired
  public void setFhirContext(final FhirContext fhirContext)
  {
    this.fhirContext = fhirContext;
  }

  public IGenericClient create(final String fhirServerUri)
  {
    final IGenericClient fhirClient = fhirContext.newRestfulGenericClient(fhirServerUri);

    if (environment.acceptsProfiles("oauth2-auth") && !fhirSecurity.isEmpty())
    {
      if ("BEARER".equals(fhirSecurity))
      {
        fhirClient.registerInterceptor(new BearerTokenAuthInterceptor(extractToken()));
      }
      else if ("TICKET".equals(fhirSecurity))
      {
        fhirClient.registerInterceptor(new FhirTicketAuthInterceptor(extractTicket(), ticketName));
      }
    }

    final LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
    loggingInterceptor.setLogRequestSummary(logRequestSummary);
    loggingInterceptor.setLogRequestBody(logRequestBody);
    loggingInterceptor.setLogResponseSummary(logResponseSummary);
    loggingInterceptor.setLogResponseBody(logResponseBody);
    fhirClient.registerInterceptor(loggingInterceptor);

    return fhirClient;
  }

  private String extractToken()
  {
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof OAuth2Authentication)
    {
      return ((OAuth2AuthenticationDetails)auth.getDetails()).getTokenValue();
    }
    else
    {
      throw new IllegalStateException("Authentication object is not of type OAuth2Authentication!");
    }
  }

  private String extractTicket()
  {
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof OAuth2Authentication)
    {
      return Opt
          .of(OAuth2UserProvider.extractClaims(auth).getOrDefault(externalTicketClaim, null))
          .map(Object::toString)
          .orElseThrow(() -> new IllegalStateException("Could not extract external ticket from JWT!"));
    }
    else
    {
      throw new IllegalStateException("Authentication object is not of type OAuth2Authentication!");
    }
  }
}
