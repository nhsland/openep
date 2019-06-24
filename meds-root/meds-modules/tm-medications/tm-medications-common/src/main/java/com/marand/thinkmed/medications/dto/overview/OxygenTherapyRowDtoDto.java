package com.marand.thinkmed.medications.dto.overview;

import com.marand.thinkmed.medications.api.internal.dto.OxygenStartingDevice;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public class OxygenTherapyRowDtoDto extends RateTherapyRowDto
{
  private OxygenStartingDevice currentStartingDevice;
  private OxygenStartingDevice startingDeviceAtIntervalStart;

  public OxygenStartingDevice getCurrentStartingDevice()
  {
    return currentStartingDevice;
  }

  public void setCurrentStartingDevice(final OxygenStartingDevice currentStartingDevice)
  {
    this.currentStartingDevice = currentStartingDevice;
  }

  public OxygenStartingDevice getStartingDeviceAtIntervalStart()
  {
    return startingDeviceAtIntervalStart;
  }

  public void setStartingDeviceAtIntervalStart(final OxygenStartingDevice startingDeviceAtIntervalStart)
  {
    this.startingDeviceAtIntervalStart = startingDeviceAtIntervalStart;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("currentStartingDevice", currentStartingDevice)
        .append("startingDeviceAtIntervalStart", startingDeviceAtIntervalStart);
  }
}
