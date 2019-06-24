package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class AdministrationTaskDto extends DataTransferObject
{
  private String taskId;
  private String administrationId;
  private String therapyId;
  private DateTime plannedAdministrationTime;
  private AdministrationTypeEnum administrationTypeEnum;
  private TherapyDoseDto therapyDoseDto;
  private Boolean doctorConfirmation;
  private String doctorsComment;

  /**
   * Links together administration tasks that represent one group/dose
   */
  private String groupUUId;

  public Boolean getDoctorConfirmation()
  {
    return doctorConfirmation;
  }

  public void setDoctorConfirmation(final Boolean doctorConfirmation)
  {
    this.doctorConfirmation = doctorConfirmation;
  }

  public String getTaskId()
  {
    return taskId;
  }

  public void setTaskId(final String taskId)
  {
    this.taskId = taskId;
  }

  public String getAdministrationId()
  {
    return administrationId;
  }

  public void setAdministrationId(final String administrationId)
  {
    this.administrationId = administrationId;
  }

  public String getTherapyId()
  {
    return therapyId;
  }

  public void setTherapyId(final String therapyId)
  {
    this.therapyId = therapyId;
  }

  public DateTime getPlannedAdministrationTime()
  {
    return plannedAdministrationTime;
  }

  public void setPlannedAdministrationTime(final DateTime plannedAdministrationTime)
  {
    this.plannedAdministrationTime = plannedAdministrationTime;
  }

  public AdministrationTypeEnum getAdministrationTypeEnum()
  {
    return administrationTypeEnum;
  }

  public void setAdministrationTypeEnum(final AdministrationTypeEnum administrationTypeEnum)
  {
    this.administrationTypeEnum = administrationTypeEnum;
  }

  public TherapyDoseDto getTherapyDoseDto()
  {
    return therapyDoseDto;
  }

  public void setTherapyDoseDto(final TherapyDoseDto therapyDoseDto)
  {
    this.therapyDoseDto = therapyDoseDto;
  }

  public String getDoctorsComment()
  {
    return doctorsComment;
  }

  public void setDoctorsComment(final String doctorsComment)
  {
    this.doctorsComment = doctorsComment;
  }

  public String getGroupUUId()
  {
    return groupUUId;
  }

  public void setGroupUUId(final String groupUUId)
  {
    this.groupUUId = groupUUId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("taskId", taskId)
        .append("administrationId", administrationId)
        .append("therapyId", therapyId)
        .append("plannedAdministrationTime", plannedAdministrationTime)
        .append("administrationTypeEnum", administrationTypeEnum)
        .append("therapyDoseDto", therapyDoseDto)
        .append("doctorsComment", doctorsComment)
        .append("groupUUId", groupUUId)
        ;
  }
}
