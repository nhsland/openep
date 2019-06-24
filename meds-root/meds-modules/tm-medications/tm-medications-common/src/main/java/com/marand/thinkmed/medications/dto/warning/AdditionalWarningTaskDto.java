package com.marand.thinkmed.medications.dto.warning;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public abstract class AdditionalWarningTaskDto extends DataTransferObject implements JsonSerializable
{
  private final String taskId;

  protected AdditionalWarningTaskDto(final @NonNull String taskId)
  {
    this.taskId = taskId;
  }

  public String getTaskId()
  {
    return taskId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("taskId", taskId);
  }
}
