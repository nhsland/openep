package com.marand.thinkmed.medications.dto.mentalHealth;

import com.marand.thinkmed.medications.dto.warning.AdditionalWarningTaskDto;
import lombok.NonNull;

/**
 * @author Nejc Korasa
 */
public class CheckMentalHealthMedsTaskDto extends AdditionalWarningTaskDto
{
  public CheckMentalHealthMedsTaskDto(final @NonNull String taskId)
  {
    super(taskId);
  }
}
