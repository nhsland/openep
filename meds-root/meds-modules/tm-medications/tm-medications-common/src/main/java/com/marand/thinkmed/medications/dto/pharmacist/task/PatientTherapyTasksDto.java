package com.marand.thinkmed.medications.dto.pharmacist.task;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class PatientTherapyTasksDto<T extends TherapyTaskSimpleDto> extends DataTransferObject
{
  private PatientDisplayDto patientDisplayDto;
  private List<T> tasksList = new ArrayList<>();

  public PatientDisplayDto getPatientDisplayDto()
  {
    return patientDisplayDto;
  }

  public void setPatientDisplayDto(final PatientDisplayDto patientDisplayDto)
  {
    this.patientDisplayDto = patientDisplayDto;
  }

  public List<T> getTasksList()
  {
    return tasksList;
  }

  public void setTasksList(final List<T> tasksList)
  {
    this.tasksList = tasksList;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientDisplayDto", patientDisplayDto)
        .append("tasksList", tasksList);
  }
}
