package com.marand.thinkmed.medications.dto.task;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.TaskTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

public class PatientTaskDto extends DataTransferObject
{
  private String id;
  private PatientDisplayDto patientDisplayDto;
  private TaskTypeEnum taskType;
  private String careProviderName;

  public String getId()
  {
    return id;
  }

  public void setId(final String id)
  {
    this.id = id;
  }

  public PatientDisplayDto getPatientDisplayDto()
  {
    return patientDisplayDto;
  }

  public void setPatientDisplayDto(final PatientDisplayDto patientDisplayDto)
  {
    this.patientDisplayDto = patientDisplayDto;
  }

  public TaskTypeEnum getTaskType()
  {
    return taskType;
  }

  public void setTaskType(final TaskTypeEnum taskType)
  {
    this.taskType = taskType;
  }

  public String getCareProviderName()
  {
    return careProviderName;
  }

  public void setCareProviderName(final String careProviderName)
  {
    this.careProviderName = careProviderName;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("id", id)
        .append("patientDisplayDto", patientDisplayDto)
        .append("taskType", taskType)
        .append("careProviderName", careProviderName)
    ;
  }
}
