package com.marand.thinkmed.medications.api.external.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mitja Lapajne
 */
public class DischargeDetailsDto
{
  private DischargeDurationDto duration;
  private DispenseSourceDto dispenseSource;
  private DischargeQuantitiesDto quantities;
  private List<KeyValueDto> additionalData = new ArrayList<>();

  public DischargeDurationDto getDuration()
  {
    return duration;
  }

  public void setDuration(final DischargeDurationDto duration)
  {
    this.duration = duration;
  }

  public DispenseSourceDto getDispenseSource()
  {
    return dispenseSource;
  }

  public void setDispenseSource(final DispenseSourceDto dispenseSource)
  {
    this.dispenseSource = dispenseSource;
  }

  public DischargeQuantitiesDto getQuantities()
  {
    return quantities;
  }

  public void setQuantities(final DischargeQuantitiesDto quantities)
  {
    this.quantities = quantities;
  }

  public List<KeyValueDto> getAdditionalData()
  {
    return additionalData;
  }

  public void setAdditionalData(final List<KeyValueDto> additionalData)
  {
    this.additionalData = additionalData;
  }
}
