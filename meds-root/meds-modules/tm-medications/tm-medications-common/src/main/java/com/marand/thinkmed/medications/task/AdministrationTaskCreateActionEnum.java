package com.marand.thinkmed.medications.task;

/**
 * @author Mitja Lapajne
 */
public enum AdministrationTaskCreateActionEnum
{
  PRESET_TIME_ON_NEW_PRESCRIPTION(true, true, true),
  PRESET_TIME_ON_MODIFY(false, true, true),
  PREVIEW_TIMES_ON_NEW_PRESCRIPTION(true, true, true),
  PRESCRIBE(true, true, false),
  MODIFY(true, true, false),
  MODIFY_BEFORE_START(true, true, false),
  AUTO_CREATE(false, false, false),
  SUSPEND(false, true, false),
  REISSUE(false, true, false),
  ABORT(false, true, false);

  private final boolean createTasksFromTherapyStart;
  private final boolean taskCreationIntervalStartIncluded;

  // for some actions taskCreationDays needs to be at least one week
  // to support calculation of first administration time for therapies that start on a specific day of week
  private final boolean taskCreationDaysMinOneWeek;

  AdministrationTaskCreateActionEnum(
      final boolean createTasksFromTherapyStart,
      final boolean taskCreationIntervalStartIncluded, final boolean taskCreationDaysMinOneWeek)
  {
    this.createTasksFromTherapyStart = createTasksFromTherapyStart;
    this.taskCreationIntervalStartIncluded = taskCreationIntervalStartIncluded;
    this.taskCreationDaysMinOneWeek = taskCreationDaysMinOneWeek;
  }

  public boolean isCreateTasksFromTherapyStart()
  {
    return createTasksFromTherapyStart;
  }

  public boolean isTaskCreationIntervalStartIncluded()
  {
    return taskCreationIntervalStartIncluded;
  }

  public boolean isTaskCreationDaysMinOneWeek()
  {
    return taskCreationDaysMinOneWeek;
  }
}
