package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.api.internal.dto.OxygenStartingDevice;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public class StartOxygenAdministrationDto extends StartAdministrationDto implements OxygenAdministration
{
  private OxygenStartingDevice plannedStartingDevice;
  private OxygenStartingDevice startingDevice;

  public StartOxygenAdministrationDto()
  {
    super(StartAdministrationSubtype.OXYGEN);
  }

  @Override
  public OxygenStartingDevice getPlannedStartingDevice()
  {
    return plannedStartingDevice;
  }

  @Override
  public void setPlannedStartingDevice(final OxygenStartingDevice plannedStartingDevice)
  {
    this.plannedStartingDevice = plannedStartingDevice;
  }

  @Override
  public OxygenStartingDevice getStartingDevice()
  {
    return startingDevice;
  }

  @Override
  public void setStartingDevice(final OxygenStartingDevice startingDevice)
  {
    this.startingDevice = startingDevice;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("plannedStartingDevice", plannedStartingDevice)
        .append("startingDevice", startingDevice)
    ;
  }
}
