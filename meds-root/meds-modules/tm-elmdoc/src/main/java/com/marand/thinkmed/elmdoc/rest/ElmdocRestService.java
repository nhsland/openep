package com.marand.thinkmed.elmdoc.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * @author Mitja Lapajne
 */
public interface ElmdocRestService
{
  @POST
  @Path("/v1/account/token")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  String generateToken(String input);

  @POST
  @Path("/v1/screening")
  @Consumes("application/json; charset=UTF8")
  @Produces(MediaType.APPLICATION_JSON)
  String screening(@HeaderParam("X-RxAdvice-Token") final String token, String input);

  @GET
  @Path("/v1/concepts/list/{type}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  String concepts(
      @HeaderParam("X-RxAdvice-Token") final String token,
      @PathParam("type") final String type,
      @QueryParam("start") String start,
      @QueryParam("pageSize") String pageSize);
}
