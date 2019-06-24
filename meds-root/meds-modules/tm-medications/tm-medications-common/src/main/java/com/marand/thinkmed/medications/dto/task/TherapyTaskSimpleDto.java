package com.marand.thinkmed.medications.dto.task;

import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.process.dto.AbstractTaskDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyTaskSimpleDto extends AbstractTaskDto
{
  private TaskTypeEnum taskType;

  public TaskTypeEnum getTaskType()
  {
    return taskType;
  }

  public void setTaskType(final TaskTypeEnum taskType)
  {
    this.taskType = taskType;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("taskType", taskType)
    ;
  }
}
