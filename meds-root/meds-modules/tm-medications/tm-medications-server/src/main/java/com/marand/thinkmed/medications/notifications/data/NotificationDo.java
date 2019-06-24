package com.marand.thinkmed.medications.notifications.data;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class NotificationDo
{
  private NotificationEventEnum event;
  private EventLevelEnum eventLevel;
  private String patientId;
  private NamedExternalDto user;
  private DateTime time;
  private String description;

  public EventLevelEnum getEventLevel()
  {
    return eventLevel;
  }

  public void setEventLevel(final EventLevelEnum eventLevel)
  {
    this.eventLevel = eventLevel;
  }

  public NotificationEventEnum getEvent()
  {
    return event;
  }

  public void setEvent(final NotificationEventEnum event)
  {
    this.event = event;
  }

  public String getPatientId()
  {
    return patientId;
  }

  public void setPatientId(final String patientId)
  {
    this.patientId = patientId;
  }

  public NamedExternalDto getUser()
  {
    return user;
  }

  public void setUser(final NamedExternalDto user)
  {
    this.user = user;
  }

  public DateTime getTime()
  {
    return time;
  }

  public void setTime(final DateTime time)
  {
    this.time = time;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(final String description)
  {
    this.description = description;
  }
}
