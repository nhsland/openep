package com.marand.meds.events;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.medications.allergies.AllergiesHandler;
import com.marand.thinkmed.request.user.StaticAuth;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nejc Korasa
 */

@RestController
@RequestMapping("/ehrevents")
public class EhrEventsController
{
  private final AllergiesHandler allergiesHandler;
  private static final String STATIC_USER = "EHR_EVENT";

  public EhrEventsController(final AllergiesHandler allergiesHandler)
  {
    this.allergiesHandler = allergiesHandler;
  }


  /*

   Create Allergy event on EHR server admin console to enable sending events to OPENeP server

   Go to <ehr_server_address>/events and create PUSH event

   ### Phase = POST_COMMIT

   ### Type = JSON

   ### DESTINATION = <openep_address>/rest/ehrevents/allergies

   ### AQL =

   SELECT c/uid/value FROM EHR e
   CONTAINS COMPOSITION c
   CONTAINS SECTION c_s[openEHR-EHR-SECTION.allergies_adverse_reactions_rcp.v1]

   */

  @PostMapping(value = "allergies", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void allergyEvent(@RequestBody final AllergyEvent event)
  {
    SecurityContextHolder.getContext().setAuthentication(new StaticAuth(STATIC_USER, STATIC_USER, STATIC_USER));

    final String newCompositionUId;
    final String oldCompositionUId;
    try
    {
      newCompositionUId = event.getNewCompositionUId();
      oldCompositionUId = event.getOldCompositionUId();
    }
    catch (final RuntimeException e)
    {
      throw new IllegalStateException("Could not extract old/new composition UIDs from allergies event", e);
    }

    Preconditions.checkNotNull(event.getEhrUid(), "ehrUID");
    Preconditions.checkArgument(
        oldCompositionUId != null || newCompositionUId != null,
        "Both, old and new composition UIDs from allergies event are null");

    allergiesHandler.handleNewAllergies(event.getEhrUid(), oldCompositionUId, newCompositionUId);
  }
}
