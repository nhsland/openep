package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.dto.TherapyActionHistoryDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class AdministrationPatientTaskDto extends PatientTaskDto
{
  private DateTime plannedTime;
  private String plannedDose;
  private AdministrationStatusEnum administrationStatus;
  private TherapyDayDto therapyDayDto;
  private TherapyActionHistoryDto therapyChange;
  private String roomAndBed;

  public AdministrationPatientTaskDto()
  {
    setTaskType(TaskTypeEnum.ADMINISTRATION_TASK);
  }

  public DateTime getPlannedTime()
  {
    return plannedTime;
  }

  public void setPlannedTime(final DateTime plannedTime)
  {
    this.plannedTime = plannedTime;
  }

  public String getPlannedDose()
  {
    return plannedDose;
  }

  public void setPlannedDose(final String plannedDose)
  {
    this.plannedDose = plannedDose;
  }

  public AdministrationStatusEnum getAdministrationStatus()
  {
    return administrationStatus;
  }

  public void setAdministrationStatus(final AdministrationStatusEnum administrationStatus)
  {
    this.administrationStatus = administrationStatus;
  }

  public TherapyDayDto getTherapyDayDto()
  {
    return therapyDayDto;
  }

  public void setTherapyDayDto(final TherapyDayDto therapyDayDto)
  {
    this.therapyDayDto = therapyDayDto;
  }

  public String getRoomAndBed()
  {
    return roomAndBed;
  }

  public void setRoomAndBed(final String roomAndBed)
  {
    this.roomAndBed = roomAndBed;
  }

  public TherapyActionHistoryDto getTherapyChange()
  {
    return therapyChange;
  }

  public void setTherapyChange(final TherapyActionHistoryDto therapyChange)
  {
    this.therapyChange = therapyChange;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("plannedTime", plannedTime)
        .append("plannedDose", plannedDose)
        .append("administrationStatus", administrationStatus)
        .append("therapyDayDto", therapyDayDto)
        .append("roomAndBed", roomAndBed)
        .append("therapyChange", therapyChange)
    ;
  }
}
