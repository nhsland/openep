package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.api.internal.dto.OxygenStartingDevice;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public class OxygenTaskDto extends AdministrationTaskDto
{
  private OxygenStartingDevice startingDevice;

  public OxygenStartingDevice getStartingDevice()
  {
    return startingDevice;
  }

  public void setStartingDevice(final OxygenStartingDevice startingDevice)
  {
    this.startingDevice = startingDevice;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("startingDevice", startingDevice);
  }
}
