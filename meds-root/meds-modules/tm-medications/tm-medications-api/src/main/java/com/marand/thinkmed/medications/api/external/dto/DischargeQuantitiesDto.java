package com.marand.thinkmed.medications.api.external.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mitja Lapajne
 */
public class DischargeQuantitiesDto
{
  private List<DischargeQuantityItemDto> items = new ArrayList<>();

  public List<DischargeQuantityItemDto> getItems()
  {
    return items;
  }

  public void setItems(final List<DischargeQuantityItemDto> items)
  {
    this.items = items;
  }
}
