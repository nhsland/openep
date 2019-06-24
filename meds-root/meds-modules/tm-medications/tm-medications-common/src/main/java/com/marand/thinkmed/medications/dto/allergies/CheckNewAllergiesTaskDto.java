package com.marand.thinkmed.medications.dto.allergies;

import java.util.HashSet;
import java.util.Set;

import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningTaskDto;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class CheckNewAllergiesTaskDto extends AdditionalWarningTaskDto
{
  private Set<IdNameDto> allergies = new HashSet<>();

  public CheckNewAllergiesTaskDto(final @NonNull String taskId, final @NonNull Set<IdNameDto> allergies)
  {
    super(taskId);
    this.allergies = allergies;
  }

  public Set<IdNameDto> getAllergies()
  {
    return allergies;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("allergies", allergies);
  }
}
