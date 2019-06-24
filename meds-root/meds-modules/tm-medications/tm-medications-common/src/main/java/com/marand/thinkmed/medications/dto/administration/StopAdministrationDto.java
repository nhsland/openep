package com.marand.thinkmed.medications.dto.administration;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class StopAdministrationDto extends AdministrationDto
{
  public StopAdministrationDto()
  {
    super(AdministrationTypeEnum.STOP);
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
  }
}
