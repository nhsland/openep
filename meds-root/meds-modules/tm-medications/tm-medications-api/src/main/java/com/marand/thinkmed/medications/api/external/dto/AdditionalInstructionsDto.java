package com.marand.thinkmed.medications.api.external.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mitja Lapajne
 */
public class AdditionalInstructionsDto
{
  private String display;
  private List<AdditionalInstructionsItemDto> items = new ArrayList<>();

  public String getDisplay()
  {
    return display;
  }

  public void setDisplay(final String display)
  {
    this.display = display;
  }

  public List<AdditionalInstructionsItemDto> getItems()
  {
    return items;
  }

  public void setItems(final List<AdditionalInstructionsItemDto> items)
  {
    this.items = items;
  }
}
