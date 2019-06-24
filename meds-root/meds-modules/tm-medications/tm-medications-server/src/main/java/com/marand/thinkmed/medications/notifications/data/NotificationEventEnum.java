package com.marand.thinkmed.medications.notifications.data;

/**
 * @author Mitja Lapajne
 */
public enum NotificationEventEnum
{
  NEW_INPATIENT_PRESCRIPTION(EventLevelEnum.AUDIT_TRAIL),
  NEW_INPATIENT_PRESCRIPTIONS(EventLevelEnum.AUDIT_TRAIL),
  MODIFIED_INPATIENT_PRESCRIPTION(EventLevelEnum.AUDIT_TRAIL),
  STOPPED_INPATIENT_PRESCRIPTION(EventLevelEnum.AUDIT_TRAIL),
  STOPPED_ALL_INPATIENT_PRESCRIPTIONS(EventLevelEnum.AUDIT_TRAIL),
  SUSPENDED_INPATIENT_PRESCRIPTION(EventLevelEnum.AUDIT_TRAIL),
  SUSPENDED_ALL_INPATIENT_PRESCRIPTIONS(EventLevelEnum.AUDIT_TRAIL),
  REISSUED_INPATIENT_PRESCRIPTION(EventLevelEnum.AUDIT_TRAIL),
  NEW_REVIEW_BY_PHARMACIST(EventLevelEnum.AUDIT_TRAIL),
  UPDATED_MEDICATION_ON_ADMISSION_LIST(EventLevelEnum.AUDIT_TRAIL),
  UPDATED_MEDICATION_ON_DISCHARGE_LIST(EventLevelEnum.AUDIT_TRAIL),
  ADMINISTRATIONS_CHANGE(EventLevelEnum.SYSTEM);

  private final EventLevelEnum eventLevel;

  NotificationEventEnum(final EventLevelEnum eventLevel)
  {
    this.eventLevel = eventLevel;
  }

  public EventLevelEnum getEventLevel()
  {
    return eventLevel;
  }
}
