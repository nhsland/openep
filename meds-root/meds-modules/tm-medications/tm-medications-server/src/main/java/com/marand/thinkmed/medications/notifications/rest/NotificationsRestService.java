package com.marand.thinkmed.medications.notifications.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * @author Mitja Lapajne
 */
public interface NotificationsRestService
{
  @POST
  @Path("notifications")
  @Consumes(MediaType.APPLICATION_JSON)
  void notifications(@HeaderParam("Authorization") final String ticket, String input);
}
