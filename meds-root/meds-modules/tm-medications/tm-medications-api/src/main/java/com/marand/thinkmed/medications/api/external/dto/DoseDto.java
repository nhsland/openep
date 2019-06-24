package com.marand.thinkmed.medications.api.external.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mitja Lapajne
 */
public class DoseDto
{
  private List<DoseItemDto> items = new ArrayList<>();

  public List<DoseItemDto> getItems()
  {
    return items;
  }

  public void setItems(final List<DoseItemDto> items)
  {
    this.items = items;
  }
}
