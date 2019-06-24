package com.marand.thinkmed.medications.connector.impl.provider.fhir;

import ca.uhn.fhir.rest.client.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import lombok.NonNull;

/**
 * @author Nejc Korasa
 */

public class FhirTicketAuthInterceptor implements IClientInterceptor
{
  private final String ticket;
  private final String headerName;

  public FhirTicketAuthInterceptor(final @NonNull String ticket, final @NonNull String headerName)
  {
    this.ticket = ticket;
    this.headerName = headerName;
  }

  @Override
  public void interceptRequest(final IHttpRequest req)
  {
    req.addHeader(headerName, ticket);
  }

  @Override
  public void interceptResponse(final IHttpResponse res) { }
}
