package com.marand.thinkmed.fdb.rest;

import javax.persistence.Convert;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * @author Mitja Lapajne
 */
public interface FdbRestService
{
  @POST
  @Path("/api/Screening/reports")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  String scanForWarnings(
      @QueryParam("checkAllDrugs") boolean checkAllDrugs,
      @QueryParam("minimumConditionAlertSeverity") String minimumConditionAlertSeverity,
      @QueryParam("minimumInteractionAlertSeverity") String minimumInteractionAlertSeverity,
      String input);

  @GET
  @Path("/api/Navigation/SNOMEDCT/omed")
  @Consumes(MediaType.APPLICATION_JSON)
  String getOrderableMedicine(
      @QueryParam("name") String name,
      @QueryParam("drugId") String drugId,
      @QueryParam("routeId") String routeId
  );
}
